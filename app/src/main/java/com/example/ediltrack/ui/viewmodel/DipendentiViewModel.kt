package com.example.ediltrack.ui.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ediltrack.model.uimodel.DipendentiUI
import com.example.ediltrack.util.ConnectDB
import com.example.ediltrack.util.UserRole
import kotlinx.coroutines.launch

class DipendentiViewModel : ViewModel() {

    private val _dipendentiUI = MutableLiveData<List<DipendentiUI>>(emptyList())
    val dipendentiUI: LiveData<List<DipendentiUI>> get() = _dipendentiUI

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> get() = _message

    // cache completa
    private val allDipendentiCache = mutableListOf<DipendentiUI>()

    private var currentQuery: String? = null
    private var currentIndex = 0
    private val pageSize = 20
    private var isLoading = false
    private var isLastPage = false

    /** Avvia una nuova ricerca */
    fun startNewSearch(query: String?) {
        currentQuery = query
        currentIndex = 0
        isLastPage = false
        _dipendentiUI.value = emptyList()
        loadNextPage()
    }

    /** Carica la prossima pagina rispettando currentQuery */
    fun loadNextPage() {
        if (isLoading || isLastPage ) return
        isLoading = true

        viewModelScope.launch {
            try {
                //Chiamata filtrata e paginata
                val newUtenti = ConnectDB.getUtentiPage(
                    offset = currentIndex.toLong(),
                    limit = pageSize.toLong(),
                    query = currentQuery
                )
                if (newUtenti.isEmpty()){
                    isLastPage = true
                }
                //Mappatura
                val newUiList = newUtenti.map { utente ->
                    DipendentiUI(
                        uid = utente.uid ?: "",
                        // Unisci nome e cognome per una visualizzazione completa
                        nome = "${utente.nome ?: ""} ${utente.cognome ?: ""}",
                        ruolo = UserRole.fromCode(utente.ruolo ?: 2).displayed
                    )
                }

                if(newUiList.isNotEmpty()){
                    //Aggiorno livedata
                    var currentList = (_dipendentiUI.value ?: emptyList()).toMutableList()
                    currentList.addAll(newUiList)
                    _dipendentiUI.value = currentList
                    //agigorno index
                    currentIndex += newUtenti.size
                }


            } catch (e: Exception) {
                Log.e("DipendentiVM", "Errore caricamento utenti: ${e.message}")
                _message.value = "Errore caricamento utenti"
            } finally {
                isLoading = false
            }
        }
    }

    /** Resetta paginazione e query */
    fun resetPagination() {
        currentQuery = null
        currentIndex = 0
        _dipendentiUI.value = emptyList()
    }
}