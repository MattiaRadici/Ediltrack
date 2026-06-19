package com.example.ediltrack.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import com.example.ediltrack.model.Cantiere
import com.example.ediltrack.model.Fase
import com.example.ediltrack.model.Problematica
import com.example.ediltrack.model.Utente
import com.example.ediltrack.model.UtenteCantiere
import com.example.ediltrack.model.uimodel.FaseUI
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.*
import io.github.jan.supabase.auth.status.SessionSource
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.*
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import io.ktor.http.auth.HttpAuthHeader
import kotlinx.serialization.json.JsonObject
import io.github.jan.supabase.storage.*
import kotlinx.serialization.json.jsonPrimitive
import java.util.UUID
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

import java.io.File


object ConnectDB {

    val supabase: SupabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = "https://watdqglokjhebzqexevx.supabase.co",
            supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6IndhdGRxZ2xva2poZWJ6cWV4ZXZ4Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTM4OTAzNjcsImV4cCI6MjA2OTQ2NjM2N30.ZvwoCvGyPK2X0HLW_uirEoDw8G-k8LG_N8Gavrh4xRM"
        ) {
            install(Auth)
            install(Postgrest)
            install(Storage)
        }
    }

    /**
     * Crea un nuovo utente usando un client temporaneo ("Ghost Client").
     * Questo evita di disconnettere l'Admin attualmente loggato.
     */
    suspend fun creaUser(mail: String, pw: String): String? = withContext(Dispatchers.IO) {
        //  client "Usa e Getta"
        val tempClient = createSupabaseClient(
            supabaseUrl = "https://watdqglokjhebzqexevx.supabase.co",
            supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6IndhdGRxZ2xva2poZWJ6cWV4ZXZ4Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTM4OTAzNjcsImV4cCI6MjA2OTQ2NjM2N30.ZvwoCvGyPK2X0HLW_uirEoDw8G-k8LG_N8Gavrh4xRM"
        ) {
            install(Auth)
        }
        try {
            Log.d("ConnectDB", "Tentativo registrazione utente fantasma: $mail")
            //Registrazione sul client temporaneo
            tempClient.auth.signUpWith(Email) {
                email = mail
                password = pw
            }

            //Recupera l'ID del nuovo utente
            val nuovoId = tempClient.auth.currentUserOrNull()?.id

            if (nuovoId != null) {
                Log.d("ConnectDB", "Successo! Nuovo ID creato: $nuovoId")
                return@withContext nuovoId
            } else {
                Log.w("ConnectDB", "Registrazione inviata, ma ID null (Verifica Email richiesta?)")
                return@withContext null
            }

        } catch (e: Exception) {
            Log.e("ConnectDB", "ERRORE CREAZIONE UTENTE: ${e.message}")
            return@withContext null
        } finally {
            //libero la memoria
            tempClient.close()
        }
    }

    suspend fun getAccountDet(): Utente?{
        val userId = supabase.auth.currentUserOrNull()?.id ?: return null
        val utente = supabase.postgrest["utente"].select{filter{
            eq("uid",userId)
        }}.decodeSingleOrNull<Utente>()
        return utente
    }


    //cantieri totali legati a un utente
    //funzione utilizzata su selCant
    suspend fun getCantieri(): List<Cantiere> {

        val userId = supabase.auth.currentUserOrNull()?.id ?: return emptyList()

        val mapping = supabase
            .postgrest["utente_cantiere"]
            .select {
                filter {
                    eq("uid", userId)
                }
            }
            .decodeList<UtenteCantiere>()

        val ids = mapping.map { it.id_cantiere }.distinct()
        if (ids.isEmpty()) return emptyList()

        val a =  supabase
            .postgrest["cantiere"]
            .select {
                filter {
                    isIn("id", ids)
                }
            }
            .decodeList<Cantiere>()
        return a
    }
    suspend fun getCantieriPage(offset: Long, limit: Long, query: String? = null): List<Cantiere> {
        val userId = supabase.auth.currentUserOrNull()?.id ?: return emptyList()

        // 1. Trovo gli ID dei cantieri associati all'utente
        val mapping = supabase
            .postgrest["utente_cantiere"]
            .select {
                filter {
                    eq("uid", userId)
                }
            }
            .decodeList<UtenteCantiere>()

        val cantieriIds = mapping.map { it.id_cantiere }.distinct()
        if (cantieriIds.isEmpty()) return emptyList()

        // 2. Prendo i dati dei cantieri, applico ricerca e paginazione
        val response = supabase
            .postgrest["cantiere"]
            .select {
                filter {
                    //filtro solo per i cantieri interessati
                    isIn("id", cantieriIds)
                    if (!query.isNullOrBlank()) {
                        or {
                            ilike("nome", "%$query%")
                            ilike("luogo", "%$query%")
                        }
                    }
                }
                order(column = "id", order = Order.ASCENDING) // Ordine stabile per la paginazione
                range(offset..(offset + limit - 1)) // Paginazione
            }
            .decodeList<Cantiere>()

        return response
    }

    //prendo gli operatori di un cantiere, ritorno la lista con tutti gli uid
    suspend fun getOperatori(intCant: Int): List<String> {
        var a = supabase.postgrest["utente_cantiere"].select(columns = Columns.list("uid")) {
            filter {
                eq(
                    "id_cantiere",
                    intCant
                )
            }
        }.decodeList<JsonObject>()
        return a.map { it["uid"].toString() }
    }

    suspend fun getOperatoreDati(uid: String): Utente {
        var a = supabase.postgrest["utente"].select() {
            filter {
                eq("uid", uid.trim('"'))
            }
        }

        if (a.decodeList<Utente>().isEmpty()) return Utente()
        return a.decodeSingle<Utente>()
    }


    suspend fun getCapocantNome(lavoratori: List<String>): String {
        val a = supabase.postgrest["utente"].select() {
            filter {
                isIn("uid", lavoratori)
                eq("ruolo", "1")
            }
        }


        if (a.decodeList<Utente>().isEmpty()) return ""
        val nome = a.decodeSingle<Utente>().nome ?: ""
        val cognome = a.decodeSingle<Utente>().cognome ?: ""
        return "$nome $cognome"
    }

    suspend fun getCapoUid(cantId: Int): String {
        try {
            //Recupero gli UID in quel cantiere
            val utentiAssociati = supabase.postgrest["utente_cantiere"]
                .select(columns = Columns.list("uid")) {
                    filter {
                        eq("id_cantiere", cantId)
                    }
                }.decodeList<JsonObject>()

            // Converto il risultato JSON in una lista semplice di Stringhe (List<String>)
            val listaUid = utentiAssociati.mapNotNull { it["uid"]?.jsonPrimitive?.content }
            //nel caso non ho nulla
            if (listaUid.isEmpty()) return ""

            //Cerco chi tra questi è il Capocantiere
            val capoResult = supabase.postgrest["utente"]
                .select(columns = Columns.list("uid")) {
                    filter {
                        //filtro tra gli id che ho
                        isIn("uid", listaUid)
                        // capocant
                        eq("ruolo", "1")
                    }
                    //nel caso ci sia un errore per cui ce ne siano 2
                    limit(1)
                }.decodeSingleOrNull<JsonObject>()
            // Ritorno l'UID
            return capoResult?.get("uid")?.jsonPrimitive?.content ?: ""

        } catch (e: Exception) {
            Log.e("ConnectDB", "Errore getCapoUid: ${e.message}")
            return ""
        }
    }


    suspend fun modificaUtente(utente: Utente) {
        val updateMap = buildJsonObject {
            put("nome", utente.nome)
            put("cognome", utente.cognome)
            put("ruolo", utente.ruolo)
            put("img", utente.img)
        }

        supabase.postgrest["utente"].update(updateMap) {
            filter {
                eq("uid", utente.uid)
            }
        }
    }

    //funzione per inserire un cantiere nel db
    suspend fun insertCantiere(nome: String, luogo: String, img: String? = null, dismesso: Boolean): Int? {
        val cantiereJson = buildJsonObject {
            put("nome", nome)
            put("luogo", luogo)
            put("img_cantiere", img) // Gestisce i null automaticamente
            put("dismesso", dismesso)
        }

        try {
            val response = supabase
                .postgrest["cantiere"]
                .insert(cantiereJson){
                    select()
                }
                .decodeSingle<Cantiere>()
            return response.id // restituisco l’ID appena creato
        }
        catch (e: Exception){
            Log.e("dbErrorInserimento","errore${e.message}")
        }


        return null
    }

    suspend fun associaUtenteACantiere(utenteId: String, cantiereId: Int) {
        val associazione = buildJsonObject {
            put("uid", utenteId)
            put("id_cantiere", cantiereId)
            put("operativo", true)
        }
        supabase.postgrest["utente_cantiere"].insert(associazione)
    }

    //prende in pasto un id, e restituisce un cantiere
    suspend fun getInfoCant(cantId: Int): Cantiere?{
        supabase.auth.currentUserOrNull()?.id ?: return null
        val cant = supabase.postgrest["cantiere"].select {
            filter {
                eq("id", cantId)
            }
        }.decodeSingle<Cantiere>()
        return cant
    }

    //funzione per caricamento utenti lazyload
    suspend fun getUtentiPage(offset: Long, limit: Long, query: String? = null, ruoloFilter:Int? = null): List<Utente> {
        Log.d("query string", query.toString())
        val response = supabase.postgrest["utente"].select() {
            if (!query.isNullOrBlank()) {
                 filter{
                     or {
                         //cerco per nome o cognome nel caso la barra di ricerca non sia vuota
                         ilike("nome", "%$query%") // ricerca non case-insensitive
                         ilike("cognome", "%$query%")
                     }
                }
                order(column = "uid", order = Order.ASCENDING)
            }
            if (ruoloFilter != null) {
                filter {
                    eq("ruolo", ruoloFilter) // filtro per il ruolo
                }
            }
            range(offset..(offset + limit - 1)) // paginazione
        }.decodeList<Utente>()

        return response
    }

    suspend fun inviaEmailRecupero(email: String): Boolean {
        return try {
            // Invia mail di reset
            supabase.auth.resetPasswordForEmail(email)
            true
        } catch (e: Exception) {
            android.util.Log.e("ConnectDB", "Errore reset psw", e)
            false
        }
    }

    suspend fun carica_immagine(uri: Uri, context: Context, bucketName: String = "utenti"): String? = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val bytes = inputStream?.readBytes() ?: return@withContext null
            inputStream.close()

            // Nome univoco file
            val fileName = "${UUID.randomUUID()}.jpg"
            Log.d("fileName", fileName)

            val bucket = supabase.storage.from(bucketName)

            // Carica su Supabase Storage
            val risposta = bucket.upload(fileName,bytes){upsert = false}

            Log.d("carica immagine", "Upload riuscito: ${risposta.path}")

            // Ottieni URL pubblico
            val publicUrl = bucket.publicUrl(fileName)
            Log.d("carica immagine", "URL pubblico: $publicUrl")

            // Ottieni URL pubblico
            fileName
        }
        catch (e:Exception){
            Log.d("carica immagine",e.toString())
            null
        }
    }
    suspend fun insertUtenteDati(utente:Utente){
        try {
            supabase.postgrest["utente"].insert(utente)
        }catch (e : Exception){
            Log.e("insertUtenteDati", "Errore durante l'inserimento dei dati dell'utente", e)
            e.printStackTrace()
        }

    }
    suspend fun insertFase(fase:FaseUI){
        val faseMap = buildJsonObject{
            put("cantiere", fase.cantiere)
            put("numeroFase", fase.numeroFase)
            put("titolo", fase.titolo)
            put("descrizione", fase.descrizione)
        }
        try {
            supabase.postgrest["fase"].insert(faseMap)
        }catch (e:Exception){
            Log.e("insertFasi", "Errore durante l'inserimento delle fasi", e)
            e.printStackTrace()
        }
    }
    suspend fun insertProblematica(problematica: Problematica) : Int {
        try {
            var item = supabase.postgrest["problematica"].insert(problematica){
                select()
            }.decodeSingle<Problematica>()
            Log.d("insertProblematica","id = ${item}")
            return item.id ?: 0
        }
        catch (e : Exception){
            Log.e("insertProblematica", "Errore durante l'inserimento della problematica", e)
            e.printStackTrace()
        }
            return 0
    }

    suspend fun getProblematiche(
        searchQuery: String? = null,  // Opzionale: testo da cercare
        stato: Int? = null,           // Opzionale: codice dello stato (0, 1, 2, 3)
        isMittente: Boolean = false,  // Indica se cercare per emittente o destinatario
        cantiereId: Int? = null
    ): List<Problematica> {
        // Eseguiamo la query sulla tabella "problematica"
        val userId = supabase.auth.currentUserOrNull()?.id ?: return emptyList()
        if (cantiereId != null) {
            // Filtro per cantiere, nel caso lo chiamo da cantiereDettaglio
            val a=  supabase.postgrest["problematica"].select{
                filter {
                    eq("cantiere", cantiereId)
                }
                order("id", Order.DESCENDING)
            }.decodeList<Problematica>()
            Log.d("problematiche", a.toString())
            return a;
        }
        if (!isMittente){
            return supabase.postgrest["problematica"].select {
                // Filtri
                filter {
                    //Filtro per Mittente
                    ilike("emittente", userId)

                    //Filtro per Testo (Se la searchQuery non è null o vuota)
                    if (!searchQuery.isNullOrBlank()) {
                        ilike("descrizione", "%$searchQuery%")
                    }

                    //Filtro per Stato (Se lo stato non è null)
                    if (stato != null) {
                        // 'eq' sta per EQuals (Uguale a)
                        eq("validazione", stato)
                    }
                }
                order("id", Order.DESCENDING)
            }.decodeList<Problematica>()
        }else{
            val cant = getCantieri()
            return supabase.postgrest["problematica"].select {
                // Filtri
                filter {
                    // Ottimizzazione futura: scaricare solo gli ID dei cantieri per ridurre il traffico dati
                    isIn("cantiere", cant.map { it.id })
                    //Filtro per Testo (Se la searchQuery non è null o vuota)
                    if (!searchQuery.isNullOrBlank()) {
                        ilike("descrizione", "%$searchQuery%")
                    }
                    //Filtro per Stato (Se lo stato non è null)
                    if (stato != null) {
                        // 'eq' sta per EQuals (Uguale a)
                        eq("validazione", stato)
                    }
                }
                order("id", Order.DESCENDING)
            }.decodeList<Problematica>()
        }
    }


    suspend fun getFasiCant(cantId: Int) : List<Fase> {
        val response = supabase.postgrest["fase"].select() {
            filter {
                eq("cantiere", cantId)
            }
        }
        return response.decodeList<Fase>()
    }

    suspend fun getNomeUtente(uuid: String): String? {
        return try {
            // Pcolumns dentro il select come parametro
            val result = supabase.postgrest["utente"]
                .select(columns = Columns.list("nome", "cognome")) {
                    filter {
                        eq("uid", uuid)
                    }
                }.decodeSingleOrNull<JsonObject>()

            if (result != null) {
                // Estrai i dati dal JSON
                val nome = result["nome"]?.jsonPrimitive?.content ?: ""
                val cognome = result["cognome"]?.jsonPrimitive?.content ?: ""
                "$nome $cognome"
            } else {
                "Utente non trovato"
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun getNomeCantiere(idCantiere: Int): String? {
        return try {
            // 'columns' va tra parentesi tonde
            val result = supabase.postgrest["cantiere"]
                .select(columns = Columns.list("nome")) {
                    filter {
                        eq("id", idCantiere)
                    }
                }.decodeSingleOrNull<JsonObject>()

            result?.get("nome")?.jsonPrimitive?.content
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun updateStatoProblematica(idProblematica: Int, nuovoStato: Int, commento: String): Boolean {
        return try {

            val datiDaAggiornare = buildJsonObject {
                put("validazione", nuovoStato)
                put("commento", commento)
            }
            // Aggiorniamo le colonne "validazione" e "commento"
            supabase.postgrest["problematica"].update(datiDaAggiornare) {
                filter {
                    eq("id", idProblematica)
                }
            }
            true // Successo
        } catch (e: Exception) {
            android.util.Log.e("ConnectDB", "Errore update: ${e.message}")
            false // Errore
        }
    }


    suspend fun eliminaFase(faseId: Int): Boolean {
        return try {
            supabase.postgrest["fase"].delete {
                filter { eq("id", faseId) }
            }
            true
        } catch (e: Exception) {
            Log.e("ConnectDB", "Errore eliminaFase", e)
            false
        }
    }

    suspend fun updateFaseCompleta(fase: FaseUI): Boolean = withContext(Dispatchers.IO) {
        //nel caso di qualche bug non arrivasse l'id
        val validId = fase.id ?: return@withContext false

        return@withContext try {
            val updateMap = buildJsonObject {
                put("titolo", fase.titolo)
                put("descrizione", fase.descrizione)
                put("numeroFase", fase.numeroFase)
                put("terminata", fase.terminata)
            }

            supabase.postgrest["fase"].update(updateMap) {
                filter {
                    eq("id", validId)
                }
            }
            true
        } catch (e: Exception) {
            Log.e("ConnectDB", "Errore updateFaseCompleta: ${e.message}", e)
            false
        }
    }

    suspend fun aggiornaStatoFase(faseId: Int, terminata: Boolean): Boolean {
        return try {
            supabase.postgrest["fase"].update(
                mapOf("terminata" to terminata)
            ) {
                filter { eq("id", faseId) }
            }
            true
        } catch (e: Exception) {
            Log.e("ConnectDB", "Errore aggiornaStatoFase", e)
            false
        }
    }

    fun getImageUrl(fileName: String, bucketName: String = "utenti"): String {
        return supabase.storage.from(bucketName).publicUrl(fileName)
    }


    suspend fun rimuoviUtenteDaCantiere(utenteId: String, cantiereId: Int) {
        try {
            supabase.postgrest["utente_cantiere"].delete {
                filter {
                    eq("uid", utenteId)
                    eq("id_cantiere", cantiereId)
                }
            }
        } catch (e: Exception) { Log.e("ConnectDB", "Errore rimozione utente", e) }
    }


    suspend fun aggiornaCapocantiere(cantiereId: Int, nuovoCapoId: String): Boolean {
        return try {
            val vecchioCapo = getCapoUid(cantiereId)

            // Rimozione vecchio capo
            if (vecchioCapo.isNotEmpty() && vecchioCapo != nuovoCapoId) {
                rimuoviUtenteDaCantiere(vecchioCapo, cantiereId)
            }

            // Aggiungo il nuovo
            val operatori = getOperatori(cantiereId)
            if (!operatori.contains(nuovoCapoId)) {
                associaUtenteACantiere(nuovoCapoId, cantiereId)
            }
            true
        } catch (e: Exception) {
            Log.e("ConnectDB", "Errore aggiornaCapocantiere", e)
            false
        }
    }


    suspend fun aggiornaListaOperaiCantiere(cantiereId: Int, nuovaListaIds: List<String>): Boolean {
        return try {
            val operatoriAttuali = getOperatori(cantiereId)
            val idCapo = getCapoUid(cantiereId)

            // Lavoriamo solo sugli operai
            val operaiAttuali = operatoriAttuali.filter { it != idCapo }

            // Calcolo differenze
            val daAggiungere = nuovaListaIds.filter { !operaiAttuali.contains(it) }
            val daRimuovere = operaiAttuali.filter { !nuovaListaIds.contains(it) }

            //rimozione e associazione ottimizzata
            daRimuovere.forEach { rimuoviUtenteDaCantiere(it, cantiereId) }
            daAggiungere.forEach { associaUtenteACantiere(it, cantiereId) }
            true
        } catch (e: Exception) {
            Log.e("ConnectDB", "Errore aggiornaListaOperaiCantiere", e)
            false
        }
    }

    suspend fun aggiornaPassword(nuovaPassword: String): Boolean {
        return try {
            supabase.auth.updateUser {
                password = nuovaPassword
            }
            true
        } catch (e: Exception) {
            Log.e("ConnectDB", "Errore aggiornamento password: ${e.message}")
            false
        }
    }

    suspend fun eseguiLogout() {
        try {
            supabase.auth.signOut()
        } catch (e: Exception) {
            Log.e("ConnectDB", "Errore durante il logout: ${e.message}")
        }
    }

    suspend fun eliminaProfiloAccount(): Boolean {
        return try {
            val userId = supabase.auth.currentUserOrNull()?.id ?: return false

            // 1. Elimina i dati dalla tabella pubblica 'utente'
            supabase.postgrest["utente"].delete {
                filter {
                    eq("uid", userId)
                }
            }
            // 2. Disconnette l'utente
            supabase.auth.signOut()
            true
        } catch (e: Exception) {
            Log.e("ConnectDB", "Errore eliminazione account: ${e.message}")
            false
        }
    }
}
