package com.example.ediltrack.ui.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.ediltrack.databinding.CardProblematicaListDipBinding
import com.example.ediltrack.model.Problematica
import com.example.ediltrack.util.ProblemaStato

class ProblematicaListAdapter(
    private var listaProblematiche: List<Problematica>,
    private val onItemClick: (Problematica) -> Unit
) : RecyclerView.Adapter<ProblematicaListAdapter.ProblematicaViewHolder>() {

    inner class ProblematicaViewHolder(val binding: CardProblematicaListDipBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Problematica) {
            binding.problematica = item

            // Gestione dello STATO (Enum)
            val stato = ProblemaStato.fromCode(item.validazione)
            binding.tvStato.text = stato.displayed

            // colore stato
            try {
                val color = Color.parseColor(stato.colorHex)
                binding.tvStato.setTextColor(color)

                binding.cardProblematica.strokeColor = color
            } catch (e: Exception) {
                e.printStackTrace()
            }

            //Gestione del Click su tutta la card
            binding.root.setOnClickListener {
                onItemClick(item)
            }
            //Aggiornamento immediato binding
            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProblematicaViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = CardProblematicaListDipBinding.inflate(inflater, parent, false)
        return ProblematicaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProblematicaViewHolder, position: Int) {
        val item = listaProblematiche[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int = listaProblematiche.size

    fun updateData(nuovaLista: List<Problematica>) {
        listaProblematiche = nuovaLista
        notifyDataSetChanged()
    }
}