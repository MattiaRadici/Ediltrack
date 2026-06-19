package com.example.ediltrack.ui.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ediltrack.util.ConnectDB
import kotlinx.coroutines.launch

class ImpostazioniViewModel : ViewModel() {

    //valori di input e text

    val nome = MutableLiveData<String>()
    val cognome = MutableLiveData<String>()

    val imageUrl = MutableLiveData<String?>()

    private val _imageUri = MutableLiveData<Uri?>()
    val imageUri: LiveData<Uri?> get() = _imageUri

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> get() = _message

    fun caricaDatiUtente() {
        viewModelScope.launch {
            try {
                val utente = ConnectDB.getAccountDet()

                if (utente != null) {
                    nome.value = utente.nome ?: ""
                    cognome.value = utente.cognome ?: ""

                    if (!utente.img.isNullOrEmpty()) {
                        imageUrl.value = ConnectDB.getImageUrl(utente.img, "utenti")
                    } else {
                        imageUrl.value = null
                    }
                } else {
                    Log.e("ImpostazioniVM", "Utente non trovato o non loggato")
                }
            } catch (e: Exception) {
                Log.e("ImpostazioniVM", "Errore caricamento utente", e)
            }
        }
    }

    fun setImageUri(uri: Uri) {
        _imageUri.value = uri
    }

    fun salvaProfilo(context: Context) {
        if (nome.value.isNullOrEmpty() || cognome.value.isNullOrEmpty()) {
            _message.postValue("Nome e cognome sono obbligatori")
            return
        }

        viewModelScope.launch {
            try {
                // Recupero i dati attuali dell'utente
                val utenteCorrente = ConnectDB.getAccountDet()

                if (utenteCorrente == null) {
                    _message.postValue("Utente non loggato o non trovato")
                    return@launch
                }

                // Carico la nuova immagine se presente, altrimenti mantengo la vecchia
                var fileName: String? = utenteCorrente.img

                _imageUri.value?.let { uri ->
                    val nuovoFileName = ConnectDB.carica_immagine(uri, context, "utenti")
                    if (nuovoFileName != null) {
                        fileName = nuovoFileName
                    } else {
                        _message.postValue("Errore durante il caricamento dell'immagine")
                        return@launch
                    }
                }

                // Creo l'oggetto Utente aggiornato per passarlo a modificaUtente
                val utenteAggiornato = utenteCorrente.copy(
                    nome = nome.value,
                    cognome = cognome.value,
                    img = fileName
                )

                // Uso la funzione esistente in ConnectDB (NOTA: la tua funzione modificaUtente
                // in ConnectDB attualmente aggiorna solo nome, cognome e ruolo. Dovrai
                // aggiornarla per includere anche l'immagine).
                ConnectDB.modificaUtente(utenteAggiornato)

                _message.postValue("Profilo aggiornato con successo")

            } catch (e: Exception) {
                Log.e("ImpostazioniVM", "Errore salvataggio profilo", e)
                _message.postValue("Errore durante il salvataggio")
            }
        }
    }

    fun aggiornaPassword(nuovaPassword: String) {
        viewModelScope.launch {
            val successo = ConnectDB.aggiornaPassword(nuovaPassword)
            if (successo) {
                _message.postValue("Password aggiornata con successo")
            } else {
                _message.postValue("Errore durante l'aggiornamento della password")
            }
        }
    }

    fun eseguiLogout(onComplete: () -> Unit) {
        viewModelScope.launch {
            ConnectDB.eseguiLogout()
            onComplete()
        }
    }

    fun eliminaAccount(onComplete: () -> Unit) {
        viewModelScope.launch {
            val successo = ConnectDB.eliminaProfiloAccount()
            if (successo) {
                onComplete()
            } else {
                _message.postValue("Errore durante l'eliminazione dell'account")
            }
        }
    }
}