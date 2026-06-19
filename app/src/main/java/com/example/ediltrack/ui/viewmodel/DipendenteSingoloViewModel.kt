package com.example.ediltrack.ui.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ediltrack.model.Utente
import com.example.ediltrack.util.ConnectDB
import kotlinx.coroutines.launch
import com.example.ediltrack.util.UserRole

class DipendenteSingoloViewModel : ViewModel() {
    var nome = MutableLiveData<String>() // Nome dell'utente
    var cognome = MutableLiveData<String>() // Cognome dell'utente
    var ruolo = MutableLiveData<UserRole>() // Ruolo dell'utente
    var url = MutableLiveData<String?>()
    
    private var originalImg: String? = null // Salvo il nome file originale dell'immagine

    fun carica(dipendenteId: String) {
        try {
            viewModelScope.launch {
                val utente = ConnectDB.getOperatoreDati(dipendenteId)

                nome.value = utente.nome
                cognome.value = utente.cognome
                ruolo.value = UserRole.fromCode(utente.ruolo)
                originalImg = utente.img // Mi salvo l'immagine originale per non sovrascriverla al salvataggio
                
                if (!utente.img.isNullOrEmpty()) {
                    // Calcoliamo l'URL qui
                    val fullLink = ConnectDB.getImageUrl(utente.img, "utenti")
                    url.value = fullLink
                    Log.d("DipendenteSingoloViewModel", "URL caricato: $fullLink")
                } else {
                    url.value = null
                }

                Log.d("DipendenteSingoloViewModel", UserRole.fromCode(utente.ruolo).toString())
            }
        }
        catch (e:Exception){
            Log.e("DipendenteSingoloViewModel", "errore caricamento dati dipendente", e)
        }
    }

    suspend fun salva(us: String): Boolean {
        return try {
            val currentNome = nome.value ?: ""
            val currentCognome = cognome.value ?: ""
            val currentRuolo = ruolo.value?.code ?: 2 // Default ad OPERAIO se nullo

            // Ricostruisco l'oggetto Utente completo
            val utenteAggiornato = Utente(
                uid = us,
                nome = currentNome,
                cognome = currentCognome,
                ruolo = currentRuolo,
                img = originalImg // Passo l'immagine originale così non la cancella dal DB
            )

            ConnectDB.modificaUtente(utenteAggiornato)
            
            Log.d("DipendenteSingoloViewModel", "Dati dipendente aggiornati con successo!")
            true
        }
        catch (e:Exception){
            Log.e("DipendenteSingoloViewModel", "Errore salvataggio dati dipendente", e)
            false
        }
    }
}