package com.example.ediltrack.ui.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ediltrack.model.Cantiere
import com.example.ediltrack.model.uimodel.CantiereUI
import com.example.ediltrack.util.ConnectDB
import com.example.ediltrack.util.UserRole
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel(){

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> get() = _message

    private var _ruolo = MutableLiveData<UserRole>(UserRole.OPERAIO)
    val ruolo: LiveData<UserRole> get() = _ruolo


    //livedata cantiere
    private val _cantieri = MutableLiveData<List<Cantiere>>()
    val cantieri: LiveData<List<Cantiere>> get() = _cantieri

    private val _cantieriUI = MutableLiveData<List<CantiereUI>>()
    val cantieriUI: LiveData<List<CantiereUI>> get() = _cantieriUI

    private val allCantieriCache = mutableListOf<CantiereUI>()

    // --- Logica di Paginazione e Ricerca ---
    private var currentQuery: String? = null
    private var currentIndex = 0
    private val pageSize = 20
    private var isLastPage = false
    private var isLoading = false

    fun loadRole(){
        viewModelScope.launch {
            delay(100)
            val user = ConnectDB.getAccountDet()
            if (user == null) {
                _message.postValue("Errore caricamento ruolo")
                return@launch
            }
            _ruolo.postValue(UserRole.fromCode(user.ruolo))
        }
    }

    fun startNewSearch(query: String?) {
        currentQuery = query
        currentIndex = 0
        isLastPage = false
        _cantieriUI.value = emptyList() // Svuoto la lista per la nuova ricerca
        loadNextPage()
    }

    /** Carica la prossima pagina rispettando currentQuery */
    fun loadNextPage() {
        if (isLoading || isLastPage) return
        isLoading = true
            //LAZYLOAD
        viewModelScope.launch {
            try {
                val newCantieri = ConnectDB.getCantieriPage(
                    offset = currentIndex.toLong(),
                    limit = pageSize.toLong(),
                    query = currentQuery
                )
                if (newCantieri.isEmpty()) {
                    isLastPage = true // Se non ricevo nulla, sono arrivato alla fine
                }
                val deferredUiList = newCantieri.map { cantiere ->
                    //non uso launch perché chiama un cantiere alla volta,
                    //facendo così invece chiamo tutti i cantieri e aspetto dopo
                    viewModelScope.async {
                        // Queste chiamate (getOperatori e getCapocantNome)
                        // vengono eseguite in parallelo per tutti i cantieri della pagina.
                        val operatori = ConnectDB.getOperatori(cantiere.id)
                        val capocant = ConnectDB.getCapocantNome(operatori)

                        // Costruisce l'oggetto UI
                        CantiereUI(
                            id = cantiere.id,
                            nome = cantiere.nome,
                            luogo = cantiere.luogo,
                            img = cantiere.img_cantiere,
                            numeroDipendenti = operatori.size,
                            capocantiere = capocant,
                            dismesso = cantiere.dismesso
                        )
                    }
                }

                val listaCompleta = deferredUiList.awaitAll()
                val currentList = (_cantieriUI.value ?: emptyList()).toMutableList()
                currentList.addAll(listaCompleta)
                _cantieriUI.postValue(currentList)
                currentIndex += newCantieri.size

            } catch (e: Exception) {
                Log.e("HomeViewModel", "Errore caricamento cantieri paginato", e)
                _message.postValue("Errore caricamento cantieri")
                isLastPage = true // Per evitare loop di caricamento in caso di errore
            } finally {
                isLoading = false
            }

        }
    }
}