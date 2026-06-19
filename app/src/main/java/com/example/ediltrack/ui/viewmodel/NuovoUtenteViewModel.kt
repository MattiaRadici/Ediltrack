package com.example.ediltrack.ui.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ediltrack.model.Utente
import com.example.ediltrack.util.ConnectDB
import kotlinx.coroutines.launch
import com.example.ediltrack.util.UserRole
import com.example.ediltrack.util.UtenteMode

class NuovoUtenteViewModel : ViewModel() {

    var immagineUri: Uri? = null
    var immagine: String? = null


    private val _message = MutableLiveData<String>()
    val message: LiveData<String> get() = _message

    fun salvaUtente(context: Context,mail:String,pw:String,nome: String, cognome: String, ruolo: String){
        viewModelScope.launch {

            var fileName: String? = immagineUri?.let { uri ->
                    ConnectDB.carica_immagine(uri, context)
                }
            Log.d("fileName",fileName.toString())
            val rr : Int = UserRole.fromDisplayName(ruolo).code
            val uid = ConnectDB.creaUser(mail,pw)
            if (uid.isNullOrEmpty()) {
                _message.value = "Errore nella creazione dell'utente"
                return@launch
            }
            val utente = Utente(
                uid = uid,
                nome = nome,
                cognome = cognome,
                ruolo = rr,
                img = fileName
            )
            ConnectDB.insertUtenteDati(utente)
            _message.value = "Utente salvato con successo"
        }
    }

    //non dovrebbe più servire
    fun carica_immagine(context: Context) {
        viewModelScope.launch {
            val result = ConnectDB.carica_immagine(immagineUri!!, context)
            immagine = result
        }
    }


}