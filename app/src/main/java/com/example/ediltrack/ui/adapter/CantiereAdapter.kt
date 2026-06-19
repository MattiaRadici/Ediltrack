package com.example.ediltrack.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.ediltrack.R
import com.example.ediltrack.databinding.CardCantiereBinding
import com.example.ediltrack.model.uimodel.CantiereUI

class CantiereAdapter(
    private val onDettagliClick: (cantiereId: Int) -> Unit
    //miserve la lista dei cantieri da far vedere
) : RecyclerView.Adapter<CantiereAdapter.CantiereViewHolder>() {
    private var cantieri = listOf<CantiereUI>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CantiereViewHolder {
        // Crea un ViewHolder con il layout inflazionato
        val binding = CardCantiereBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CantiereViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CantiereViewHolder, position: Int) {
        // Associa i dati ai view holder
        holder.bind(cantieri[position])
    }

    override fun getItemCount(): Int {
        return cantieri.size
    }

    fun updateData(newList: List<CantiereUI>) {
        cantieri = newList
        notifyDataSetChanged()
    }

    //mappo ogni singola classe
    inner class CantiereViewHolder (private val binding: CardCantiereBinding): RecyclerView.ViewHolder(binding.root){
        fun bind (cantiere: CantiereUI){
            binding.nomeCantiere.text = cantiere.nome
            binding.luogoCantiere.text = cantiere.luogo
            binding.dipendenti.text = cantiere.numeroDipendenti.toString()
            binding.capocantiere.text = cantiere.capocantiere

            binding.stato.text = if (cantiere.dismesso) "DISMESSO" else "ATTIVO"
            binding.stato.setBackgroundColor(
                if (cantiere.dismesso) ContextCompat.getColor(binding.root.context, R.color.red_background)
                else ContextCompat.getColor(binding.root.context, R.color.green_background)
            )

            //binding.imgCantiere.setImageResource(cantiere.img)
            //implementare anche l'immagine
            binding.btnDettagli.setOnClickListener {
                onDettagliClick(cantiere.id) // ← qui passi solo l'ID
            }
        }
    }

}