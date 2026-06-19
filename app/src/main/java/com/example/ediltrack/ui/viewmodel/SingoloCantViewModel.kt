package com.example.ediltrack.ui.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ediltrack.model.Cantiere
import com.example.ediltrack.model.uimodel.DipendentiUI
import com.example.ediltrack.model.uimodel.FaseUI
import com.example.ediltrack.util.ConnectDB
import com.example.ediltrack.util.UserRole
import com.example.ediltrack.util.UtenteMode
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch

class SingoloCantViewModel : ViewModel() {

    // --- DATI CANTIERE ---
    var nome = MutableLiveData<String>()
    var nomeCapocant = MutableLiveData<String>()
    var luogo = MutableLiveData<String>()
    var operai = MutableLiveData<String>()
    val imageUrl = MutableLiveData<String?>()

    // --- DATI FASI ---
    val fasiList = MutableLiveData<List<FaseUI>>()
    val progresso = MutableLiveData<Int>(0)

    // --- GESTIONE UTENTI ---

    // Lista paginata per la UI
    private val _utentiUI = MutableLiveData<List<DipendentiUI>>(emptyList())
    val utentiUI: LiveData<List<DipendentiUI>> get() = _utentiUI

    // ID già selezionati (per le checkbox)
    private val _assignedIds = MutableLiveData<List<String>>(emptyList())
    val assignedIds: LiveData<List<String>> get() = _assignedIds

    // Paginazione
    private val pageSize : Long = 20
    private var currentIndex :Long = 0
    private var currentQuery: String? = null
    private var isLoading = false
    private var currentRoleFilter: Int? = null



    // SEZIONE 1: CANTIERE E FASI

    fun carica(cantID: Int) {
        viewModelScope.launch {
            try {
                // Info base cantiere
                val cant: Cantiere? = ConnectDB.getInfoCant(cantID)

                if (cant != null) {
                    // Recupero operatori e capo
                    val op = ConnectDB.getOperatori(cantID)
                    val capocant = ConnectDB.getCapocantNome(op)

                    nome.value = cant.nome
                    luogo.value = cant.luogo
                    nomeCapocant.value = capocant
                    operai.value = op.size.toString()

                    // Gestione immagine
                    if (!cant.img_cantiere.isNullOrEmpty()) {
                        imageUrl.value = ConnectDB.getImageUrl(cant.img_cantiere, "cantieri")
                    } else {
                        imageUrl.value = null
                    }
                }
            } catch (e: Exception) {
                Log.e("SingoloCantVM", "Errore load cantiere", e)
            }
        }
    }

    fun caricaFasi(cantiereId: Int) {
        viewModelScope.launch {
            try {
                val listaGrezza = ConnectDB.getFasiCant(cantiereId)

                // Mappo DB -> UI
                val listaConvertita = listaGrezza.map { faseDb ->
                    FaseUI(
                        id = faseDb.id?.toInt(),
                        cantiere = cantiereId,
                        numeroFase = faseDb.numeroFase?.toString() ?: "",
                        titolo = faseDb.titolo ?: "",
                        descrizione = faseDb.descrizione ?: "",
                        terminata = faseDb.terminata ?: false
                    )
                }

                // Calcolo progress bar
                val totale = listaConvertita.size
                if (totale > 0) {
                    val completate = listaConvertita.count { it.terminata }
                    progresso.value = (completate * 100) / totale
                } else {
                    progresso.value = 0
                }

                fasiList.value = listaConvertita
            } catch (e: Exception) {
                Log.e("SingoloCantVM", "Errore fasi", e)
            }
        }
    }

    fun eliminaFase(faseId: Int, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val successo = ConnectDB.eliminaFase(faseId)
            onResult(successo)
        }
    }

    fun salvaTutteLeFasi(listaDaSalvare: List<FaseUI>, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                // Aggiorno in parallelo
                val lavori = listaDaSalvare.map { fase ->
                    async { ConnectDB.updateFaseCompleta(fase) }
                }
                val risultati = lavori.awaitAll()

                // Aggiorno UI se tutto ok
                if (risultati.all { it }) {
                    fasiList.value = listaDaSalvare
                    onResult(true)
                } else {
                    onResult(false)
                }
            } catch (e: Exception) {
                Log.e("SingoloCantVM", "Crash salvataggio fasi", e)
                onResult(false)
            }
        }
    }

    fun aggiungiFaseVuotaAlDb(cantiereId: Int) {
        viewModelScope.launch {
            // Creo vuota e ricarico lista
            val nuovaFase = FaseUI(cantiere = cantiereId, numeroFase = "0", titolo = "", descrizione = "")
            ConnectDB.insertFase(nuovaFase)
            caricaFasi(cantiereId)
        }
    }


    // SEZIONE 2: MODIFICA UTENTI

    //reset ricerca e carico preselezionati
    fun initSelezionaUtenti(cantiereId: Int, mode: String) {
        val ruoloCode = if (mode == UtenteMode.CAPOCANTIERE.name) UserRole.CAPOCANTIERE.code else UserRole.OPERAIO.code

        //Reset e prima pagina
        startNewSearch(null, ruoloCode)

        //Controllo chi c'è già
        viewModelScope.launch {
            try {
                val ids = if (mode == UtenteMode.CAPOCANTIERE.name) {
                    val capoId = ConnectDB.getCapoUid(cantiereId)
                    if (capoId.isNotEmpty()) listOf(capoId) else emptyList()
                } else {
                    ConnectDB.getOperatori(cantiereId)
                }
                _assignedIds.postValue(ids)
            } catch (e: Exception) {
                Log.e("SingoloCantVM", "Errore check assegnati", e)
            }
        }
    }

    fun startNewSearch(query: String?, ruolo: Int? = null) {
        // Nuova ricerca: azzero tutto
        currentQuery = query
        currentRoleFilter = ruolo
        currentIndex = 0
        _utentiUI.value = emptyList()
        loadNextPage()
    }

    fun loadNextPage() {
        if (isLoading) return
        isLoading = true

        viewModelScope.launch {
            try {
                // Chiamo DB paginato
                val utentiPage = ConnectDB.getUtentiPage(currentIndex, pageSize, currentQuery, currentRoleFilter)

                // Mappo in DipendentiUI
                val pageUI = utentiPage.map { utente ->
                    DipendentiUI(
                        uid = utente.uid ?: "",
                        nome = "${utente.nome ?: ""} ${utente.cognome ?: ""}",
                        ruolo = UserRole.fromCode(utente.ruolo ?: 2).displayed,
                        img = utente.img
                    )
                }

                // Accodo risultati
                val currentList = _utentiUI.value ?: emptyList()
                _utentiUI.value = currentList + pageUI
                currentIndex += pageUI.size

            } catch (e: Exception) {
                Log.e("SingoloCantVM", "Errore paginazione", e)
            } finally {
                isLoading = false
            }
        }
    }

    //SALVATAGGIO

    fun aggiornaCapocantiere(cantiereId: Int, nuovoCapoId: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val successo = ConnectDB.aggiornaCapocantiere(cantiereId, nuovoCapoId)
            onResult(successo)
        }
    }

    fun aggiornaListaOperai(cantiereId: Int, listaIds: List<String>, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val successo = ConnectDB.aggiornaListaOperaiCantiere(cantiereId, listaIds)
            onResult(successo)
        }
    }
}