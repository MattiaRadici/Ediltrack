package com.example.ediltrack.ui.viewmodel

import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ediltrack.model.Problematica
import com.example.ediltrack.util.ConnectDB
import com.example.ediltrack.util.UserRole
import kotlinx.coroutines.launch

class ProblematicaDetailViewModel : ViewModel() {

    private val _problematica = MutableLiveData<Problematica>()
    val problematica: LiveData<Problematica> get() = _problematica

    private val _nomeCantiere = MutableLiveData<String>("Caricamento...")
    val nomeCantiere: LiveData<String> get() = _nomeCantiere

    private val _nomeMittente = MutableLiveData<String>("Caricamento...")
    val nomeMittente: LiveData<String> get() = _nomeMittente

    private val _imageUrl = MutableLiveData<String?>()
    val imageUrl: LiveData<String?> get() = _imageUrl

    // Risultato dell'operazione di salvataggio
    private val _updateResult = MutableLiveData<Boolean?>()
    val updateResult: LiveData<Boolean?> = _updateResult

    private val _ruolo = MutableLiveData<Int?>()
    val ruolo: LiveData<Int?> = _ruolo

    fun loadRole() {
        viewModelScope.launch {
            val acc = ConnectDB.getAccountDet()
            _ruolo.value = acc?.ruolo
        }
    }

    // Funzione chiamata appena il fragment si apre
    fun initData(item: Problematica) {
        _problematica.value = item

        //  l'URL dell'immagine
        if (!item.img_cantiere.isNullOrEmpty()) {
            val url = ConnectDB.getImageUrl(item.img_cantiere,"problematica")
            _imageUrl.value = url
        }

        viewModelScope.launch {
            //Nome Cantiere
            item.cantiere?.let { id ->
                val nome = ConnectDB.getNomeCantiere(id)
                _nomeCantiere.value = nome ?: "Cantiere non trovato"
            }
            // Nome Mittente
            item.emittente?.let { uuid ->
                val nome = ConnectDB.getNomeUtente(uuid)
                _nomeMittente.value = nome ?: "Utente non trovato"
            }
            if (ruolo.value == UserRole.CAPOCANTIERE.code && item.validazione == 0){
                //notifica di lettura
                ConnectDB.updateStatoProblematica(
                    idProblematica = item.id ?: 0,
                    nuovoStato = 1,
                    commento = item.commento ?: ""
                )
                val itemAggiornato = item.copy(validazione = 1)
                _problematica.postValue(itemAggiornato)
            }
        }
    }

    fun salvaModificheProblematica(nuovoStato: Int, testoCommento: String) {
        val currentId = _problematica.value?.id ?: return
        viewModelScope.launch{
            val successo = ConnectDB.updateStatoProblematica(currentId, nuovoStato, testoCommento)
            _updateResult.value = successo
        }
    }
    fun resetUpdateResult() {
        _updateResult.value = null
    }
}