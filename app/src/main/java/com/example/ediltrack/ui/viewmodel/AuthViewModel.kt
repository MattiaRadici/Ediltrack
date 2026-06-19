package com.example.ediltrack.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.ediltrack.R
import com.example.ediltrack.util.ConnectDB
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    //dati sull'interfaccia
    val emailV = MutableLiveData<String>("")
    val passwordV = MutableLiveData<String>("")

    // LiveData per mostrare messaggi all'utente
    val _mess = MutableLiveData<String?>()
    val mess: LiveData<String?> = _mess

    val isRecoveryVisible = MutableLiveData(false)
    val recoveryEmail = MutableLiveData<String>("")
    val isSendingRecovery = MutableLiveData(false)

    // Funzione chiamata dal click
    fun onForgotPasswordClick() {
        // Inverte lo stato attuale
        isRecoveryVisible.value = !(isRecoveryVisible.value ?: false)
    }

    fun loginClick(){
        Log.d("LOGIN", "Funzione loginClick chiamata")
            if (emailV.value.isEmpty() || passwordV.value.isEmpty()) {
                // Notifica errore all’utente (tipo con un LiveData di errore)
                _mess.value = "Compila tutti i campi"
                return
            }
                //provo a fare l'auth con em e pw
            viewModelScope.launch {
                try{
                    val session = ConnectDB.supabase.auth.signInWith(Email){
                        email = emailV.value
                        password = passwordV.value
                    }
                    _mess.value = "Autenticazione riuscita"
                }catch (e:Exception){
                    Log.e("Login","${e.message}")
                    _mess.value = "Errore di autenticazione"
                }
            }
        }

    fun recupera() {
        val email = recoveryEmail.value?.trim() ?: ""
        val context = getApplication<Application>().applicationContext

        if (email.isNotEmpty()) {
            isSendingRecovery.value = true // Disabilita bottone e cambia testo

            viewModelScope.launch {
                val successo = ConnectDB.inviaEmailRecupero(email)

                if (successo) {
                    _mess.value = context.getString(R.string.auth_success_sent)
                    // Chiudi il box e pulisci
                    isRecoveryVisible.value = false
                    recoveryEmail.value = ""
                } else {
                    _mess.value = context.getString(R.string.auth_error_generic)
                }

                // Riabilita il bottone
                isSendingRecovery.value = false
            }
        } else {
            _mess.value = context.getString(R.string.auth_error_empty_email)
        }
    }
}


