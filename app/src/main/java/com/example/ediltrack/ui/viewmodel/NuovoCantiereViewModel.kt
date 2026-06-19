package com.example.ediltrack.ui.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ediltrack.model.Fase
import com.example.ediltrack.model.uimodel.DipendentiUI
import com.example.ediltrack.util.ConnectDB
import com.example.ediltrack.util.UserRole
import kotlinx.coroutines.launch
import android.content.Context
import androidx.lifecycle.map
import com.example.ediltrack.model.Cantiere
import com.example.ediltrack.model.uimodel.FaseUI
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll

class NuovoCantiereViewModel: ViewModel() {

    var nome = MutableLiveData<String>()
    var luogo = MutableLiveData<String>()
    private var utenti = MutableLiveData<List<String>>()
    var dismesso = MutableLiveData(false)
    private var capocant = MutableLiveData<String>()

    // Lista privata modificabile
    private var _fasi = MutableLiveData<List<FaseUI>>(emptyList())
    // LiveData pubblico
    val fasi: LiveData<List<FaseUI>> get() = _fasi


    private var _cantiereImageUri = MutableLiveData<Uri?>()
    val cantiereImageUri: LiveData<Uri?> get() = _cantiereImageUri


    private var _message = MutableLiveData<String>()
    val message : LiveData<String> get() = _message

    private val _utentiUI = MutableLiveData<List<DipendentiUI>>(emptyList())
    val utentiUI: LiveData<List<DipendentiUI>> get() = _utentiUI

    private val pageSize : Long = 20
    private var currentIndex :Long = 0
    private var currentQuery: String? = null
    private var isLoading = false
    private var currentRoleFilter: Int? = null

    fun startNewSearch(query: String?, ruolo: Int? = null) {
        currentQuery = query
        currentIndex = 0
        _utentiUI.value = emptyList()
        //setto il current filter in base alla shermata in cui sono
        loadNextPage()
    }

    /** Carica la prossima pagina rispettando currentQuery */
    fun loadNextPage() {
        if (isLoading) return
        isLoading = true

        viewModelScope.launch {
            try {
                val utentiPage = ConnectDB.getUtentiPage(currentIndex, pageSize, currentQuery,currentRoleFilter)
                val pageUI = utentiPage.map { utente ->
                    DipendentiUI(
                        uid = utente.uid ?: "",
                        nome = "${utente.nome ?: ""} ${utente.cognome ?: ""}",
                        ruolo = UserRole.fromCode(utente.ruolo ?: 2).displayed
                    )
                }
                val updatedList = (_utentiUI.value ?: emptyList()) + pageUI
                _utentiUI.value = updatedList
                currentIndex += pageUI.size
            } catch (e: Exception) {
                Log.e("SelezionaUtentiVM", "Errore caricamento utenti: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }


    fun salvaCantiere(context: Context) {
        if (nome.value.isNullOrEmpty() || luogo.value.isNullOrEmpty()) {
            _message.postValue("Inserire nome e luogo")
            return
        }
        if (fasi.value.isNullOrEmpty()) {
            _message.postValue("Inserire almeno una fase")
            return
        }
        if (utenti.value.isNullOrEmpty()) {
            _message.postValue("Inserire almeno un operatore")
            return
        }
        if (capocant.value.isNullOrEmpty()) {
            _message.postValue("Inserire almeno un capocantiere")
            return
        }
            viewModelScope.launch {
                try {
                    //carico immagine se presente
                    var fileName: String? = cantiereImageUri.value?.let { uri ->
                        ConnectDB.carica_immagine(uri, context,"cantieri")
                    }
                    val id_can =
                        ConnectDB.insertCantiere(nome.value, luogo.value, fileName, dismesso.value)
                    //controllo che abbia salvato correttamente
                    if (id_can == null) {
                        _message.postValue("Errroe durante il caricamento")
                        return@launch
                    }

                    // Salvataggio in parallelo usando async e awaitAll come descritto nella tesi
                    val capoJob = async { ConnectDB.associaUtenteACantiere(capocant.value!!, id_can) }

                    val utentiJobs = utenti.value!!.map { uid ->
                        async { ConnectDB.associaUtenteACantiere(uid, id_can) }
                    }

                    val currentFasiList = _fasi.value ?: emptyList()
                    val fasiJobs = currentFasiList.map { fase ->
                        async { ConnectDB.insertFase(fase.copy(cantiere = id_can)) }
                    }

                    // Attende che tutte le operazioni asincrone in parallelo finiscano
                    capoJob.await()
                    utentiJobs.awaitAll()
                    fasiJobs.awaitAll()
                    
                    _message.postValue("Cantiere salvato con successo")
                } catch (e: Exception) {
                    Log.e(
                        "salvaCantiere",
                        "Errore durante il caricamento del cantiere: ${e.message}"
                    )
                }
            }

        }

        fun setUtenti(utenti: List<String>) {
            this.utenti.value = utenti
        }
        fun getDip(): List<String>?{
            return this.utenti.value
        }

        fun setCapocant(capocant: String) {
            this.capocant.postValue(capocant)
        }

        fun getCapocant(): String? {
            return this.capocant.value
        }


        fun setCantiereImageUri(uri: Uri) {
            _cantiereImageUri.value = uri
        }

        fun addFase(nuovaFase: FaseUI) {
            val currentList = _fasi.value?.toMutableList() ?: mutableListOf()
            currentList.add(nuovaFase)
            _fasi.value = currentList
        }

        // Metodo per rimuovere una fase
        fun removeFase(faseId: Int) {
            val currentList = _fasi.value?.toMutableList() ?: return
            // Rimuovi la fase con l'ID specificato
            currentList.removeAll { it.id == faseId }

            // Riordina i numeri delle fasi dopo la rimozione
            currentList.forEachIndexed { index, fase ->
                currentList[index] = fase.copy(numeroFase = (index + 1).toString())
            }
            //aggiorno il liveData
            _fasi.value = currentList
        }

        // Metodo per aggiornare una singola fase
        fun updateSingleFase(updatedFase: FaseUI) {

            val currentList = _fasi.value?.toMutableList() ?: return
            val index = currentList.indexOfFirst { it.id == updatedFase.id }

            if (index != -1) {
                currentList[index] = updatedFase
                _fasi.value = currentList
            }
        }

        // Metodo per aggiornare l'intera lista (se serve)
        fun updateFasiList(newFasi: List<FaseUI>) {
            _fasi.value = newFasi
        }

        // Metodo per ottenere tutte le fasi (utile per salvare)
        fun getAllFasi(): List<FaseUI> {
            return _fasi.value ?: emptyList()
        }

        // Metodo per pulire le fasi (utile per reset)
        fun clearFasi() {
            _fasi.value = emptyList()
        }

        fun resetmess(){
            _message.value = ""
        }
        fun setRole(role: Int) {

            if (currentRoleFilter != role || _utentiUI.value.isNullOrEmpty()) {
                currentRoleFilter = role

                //AZZERO I VALORI QUANDO PASSO TRA LE DUE SCHERMATE
                _utentiUI.value = emptyList()
                currentIndex = 0
                currentQuery  = null
                isLoading = false
            }

        }

}