package com.example.ediltrack.ui.viewmodel

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ediltrack.R
import com.example.ediltrack.model.Cantiere
import com.example.ediltrack.model.Fase
import com.example.ediltrack.model.Problematica
import com.example.ediltrack.util.ConnectDB
import kotlinx.coroutines.launch

class HomeDipendenteViewModel : ViewModel(){

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> get() = _message


    //inizialmente era privato poi non privato perché usato in fragment
    val _descrizione = MutableLiveData<String>()
    val descrizione : LiveData<String> get() = _descrizione

    private var _imageUri = MutableLiveData<Uri?>()
    val cantiereImageUri: LiveData<Uri?> get() = _imageUri

    private val _cantiere = MutableLiveData<Int>()
    val cantiere : LiveData<Int> get() = _cantiere

    private val _fase = MutableLiveData<Int>()
    val fase : LiveData<Int> get() = _fase

    private val _listaCantieri = MutableLiveData<List<Cantiere>>()
    val listaCantieri: LiveData<List<Cantiere>> get() = _listaCantieri

    private val _listaFasi = MutableLiveData<List<Fase>>()
    val listaFasi: LiveData<List<Fase>> get() = _listaFasi

    init {
        getCant()
    }

    fun setMessage(mess : String){
        _message.value = mess
    }

    fun setDescrizione(desc : String){
        _descrizione.value = desc
    }

    fun setImageUri(uri: Uri){
        _imageUri.value = uri
    }

    fun insertFase(fase : Int){
        _fase.value = fase
    }

    fun selCant(cant : Int){
        _cantiere.value = cant
    }

    fun getCant(){
        viewModelScope.launch {
            try {
                val result = ConnectDB.getCantieri()

                if (result.isEmpty()) {
                    _message.value = "Nessun cantiere trovato per questo utente."
                }
                _listaCantieri.value = result

            } catch (e: Exception) {
                e.printStackTrace()
                _message.value = "Errore: ${e.message}"
            }
        }
    }

    fun getFase(){
        if (cantiere.value == null){
            _message.value = "Selezionare un cantiere"
            return
        }
        viewModelScope.launch {
            val result = ConnectDB.getFasiCant(cantiere.value)
            if(result == null){
                _message.value = "Nessuna fase trovata"
                return@launch
            }
            _listaFasi.value = result
        }
    }


    fun salva(context: Context){
        if (descrizione.value.isNullOrBlank()){
            _message.value = "Inserire descrizione"
            return
        }
        if (fase.value == 0|| fase.value == null ) {
            _message.value = "Selezionare una fase"
            return
        }
        if (cantiere.value == 0|| cantiere.value == null ) {
            _message.value = "Selezionare una cantiere"
            return
        }
        if (cantiereImageUri.value == null){
            _message.value = "Inserire almeno un immagine"
            return
        }
        //Log.d("salva","check campi ok")
        viewModelScope.launch {
            try {
                //carico immagine se presente
                var fileName: String? = cantiereImageUri.value?.let { uri ->
                    ConnectDB.carica_immagine(uri, context,"problematica")
                }
                //Log.d("salva","filename = $fileName")
                if (fileName.isNullOrEmpty()){
                    _message.value = "Errore durante il caricamento dell'immagine"
                    return@launch
                }
                val mittente = ConnectDB.getAccountDet()
                //Log.d("salva","mittente = $mittente")
                var problematica = Problematica(
                    id = null,
                    descrizione = descrizione.value,
                    img_cantiere = fileName,
                    fase = fase.value,
                    cantiere = cantiere.value,
                    emittente = mittente?.uid
                )
                var id = ConnectDB.insertProblematica(problematica)
                //Log.d("salva","id = $id")
                if(id == 0){
                    _message.value = "Errore durante il caricamento della Problematica"
                    return@launch
                }
                _message.value = "Salvataggio salvato con successo"
            }
            catch (e:Exception){
                e.printStackTrace()
                _message.value = "Errore durante il caricamento"
            }

        }
    }

    fun reset(){
        _message.value = ""
        _descrizione.value = ""
        _imageUri.value = null
        _cantiere.value = 0
        _fase.value = 0
        _listaFasi.value = emptyList()
    }



}