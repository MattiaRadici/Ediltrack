package com.example.ediltrack.ui.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ediltrack.model.Problematica
import com.example.ediltrack.util.ConnectDB
import com.example.ediltrack.util.ProblemaStato
import kotlinx.coroutines.launch
import com.example.ediltrack.util.UserRole

class ProblematicheListViewModel : ViewModel() {

    // 1. LiveData per la UI
    private val _problematiche = MutableLiveData<List<Problematica>>()
    val problematiche: LiveData<List<Problematica>> get() = _problematiche

    private val _isEmpty = MutableLiveData<Boolean>(true)
    val isEmpty: LiveData<Boolean> get() = _isEmpty

    private val _message = MutableLiveData<String?>()
    val message: LiveData<String?> get() = _message

    // 4. Variabili per i filtri correnti
    private var currentSearchText: String? = null
    private var currentStatusFilter: ProblemaStato? = null

    var cantId :Int? =null


    fun init(cantId: Int?){
        this.cantId = cantId
        refreshData()
    }

    fun onSearchQueryChanged(query: String) {
        val newQuery = if (query.isBlank()) null else query.trim()
        if (currentSearchText != newQuery) {
            currentSearchText = newQuery
            refreshData()
        }
    }

    fun onStatusFilterChanged(status: ProblemaStato?) {
        if (currentStatusFilter != status) {
            currentStatusFilter = status
            refreshData()
        }
    }

    fun refreshData() {
        _message.value = null // Reset del messaggio precedente

        viewModelScope.launch {
            try {
                val user = ConnectDB.getAccountDet()
                val ruolo = user?.ruolo

                val result = if (ruolo == UserRole.CAPOCANTIERE.code) { // Assumo che .code sia es. "1"

                    // CASO FALSE: Passa il suo UID e isMittente=true
                    ConnectDB.getProblematiche(
                        searchQuery = currentSearchText,
                        stato = currentStatusFilter?.code,
                        isMittente = true,
                        cantiereId = cantId
                    )
                } else {

                    // CASO CAPOCANT (Else): Passa il suo UID ma isMittente=false
                    ConnectDB.getProblematiche(
                        searchQuery = currentSearchText,
                        stato = currentStatusFilter?.code,
                        isMittente = false,
                        cantiereId = cantId
                    )
                }

                _problematiche.value = result
                _isEmpty.value = result.isEmpty()

            } catch (e: Exception) {
                e.printStackTrace()
                _message.value = "Errore caricamento: ${e.message}"
                Log.e("ProblematicheListViewModel", "Errore caricamento dati", e)
                _problematiche.value = emptyList()
                _isEmpty.value = true
            }
        }
    }
}
