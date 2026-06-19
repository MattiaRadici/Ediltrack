package com.example.ediltrack.ui.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.ediltrack.databinding.CardOperaioBinding
import com.example.ediltrack.model.uimodel.DipendentiUI
import com.example.ediltrack.util.UserRole

class DipendenteAdapter(

    //variabili controllo visuali e di click listener.
    //mi servono per riutilizzare le card
    private val mode: AdapterMode,
    private val onCapoCheck: ((String, Boolean) -> Unit)? = null, //funzione di callback specifica per capocantiere (rimanda a nuovocant creazione)
    private val onItemClick: ((String) -> Unit)? = null,
    internal var attuali: List<DipendentiUI>,
    preselezionati: List<String>? = emptyList() //serve a carocare la pagina con le spunte nel caso ci siano già
): RecyclerView.Adapter<DipendenteAdapter.DipendenteViewHolder>() {

    private val selectedIds = preselezionati?.toMutableSet() ?: mutableSetOf()

    fun updateData(dipendenti: List<DipendentiUI>) {
        // Aggiorna la lista dei dipendenti e notifica i cambiamenti
        attuali = dipendenti
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DipendenteViewHolder {
        val binding = CardOperaioBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DipendenteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DipendenteViewHolder, position: Int) {
        val item = attuali[position]
        holder.bind(item, selectedIds.contains(item.uid))
    }

    override fun getItemCount(): Int {
        return attuali.size
    }


    inner class DipendenteViewHolder(private val binding: CardOperaioBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(utente: DipendentiUI, isSelected: Boolean) {
            binding.operatore = utente
            binding.executePendingBindings()

            when (mode) {
                AdapterMode.LISTA -> {
                    // Modalità lista normale ->niente checkbox, card cliccabile
                    binding.checkOperaio.visibility = View.GONE
                    binding.root.setOnClickListener {
                        onItemClick?.invoke(utente.uid)
                    }
                }
                AdapterMode.OPERAIO -> {
                    // Modalità selezione operai, solo check
                    binding.checkOperaio.visibility = View.VISIBLE
                    binding.checkOperaio.isChecked = isSelected

                    binding.checkOperaio.setOnCheckedChangeListener { _, checked ->
                        if (checked) {
                            selectedIds.add(utente.uid)
                        } else {
                            selectedIds.remove(utente.uid)
                        }
                    }

                    // disabilito click card
                    binding.root.setOnClickListener(null)
                }
                AdapterMode.CAPOCANTIERE -> {
                    //Modalità selezione capocantiere -> deve rimandare a nuovocant con salvando su viewmodel
                    binding.checkOperaio.visibility = View.VISIBLE
                    //disabilito il listener
                    binding.checkOperaio.setOnCheckedChangeListener(null)
                    //nel caso è selezionato lo metto
                    binding.checkOperaio.isChecked = isSelected

                    binding.checkOperaio.setOnCheckedChangeListener { _, checked ->
                        if (checked) {
                            if(utente.uid != selectedIds.firstOrNull()){
                                selectedIds.clear()
                                selectedIds.add(utente.uid)
                            }
                        } else {
                            selectedIds.remove(utente.uid)
                        }

                        // callback specifica per capocantiere
                        onCapoCheck?.invoke(utente.uid, checked)
                    }

                    // disabilito click card
                    binding.root.setOnClickListener(null)
                }
            }
        }
    }
    fun getSelectedIds(): List<String> = selectedIds.toList()
}

enum class AdapterMode {
    LISTA,       // card normale cliccabile
    OPERAIO,  // checkbox semplice
    CAPOCANTIERE // checkbox con callback custom
}