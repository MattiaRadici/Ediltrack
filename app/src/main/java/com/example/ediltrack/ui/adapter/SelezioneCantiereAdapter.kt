package com.example.ediltrack.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.ediltrack.databinding.CardSelCantBinding
import com.example.ediltrack.databinding.CardSelFaseBinding
import com.example.ediltrack.model.Cantiere
import com.example.ediltrack.model.Fase
import com.example.ediltrack.ui.adapter.SelezionaFaseAdapter.FaseDiffCallback
import com.example.ediltrack.ui.adapter.SelezionaFaseAdapter.ViewHolder
import com.example.ediltrack.ui.view.fragment.SelezionaCantiereFragment

class SelezioneCantiereAdapter(private val onSelectClick: (cantId: Int) -> Unit) :
    ListAdapter<Cantiere, SelezioneCantiereAdapter.SelCantViewHolder>(SelCantDiffCallback()) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SelezioneCantiereAdapter.SelCantViewHolder {
        val binding = CardSelCantBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SelCantViewHolder(binding, onSelectClick)
    }

    override fun onBindViewHolder(holder: SelezioneCantiereAdapter.SelCantViewHolder, position: Int){
        val cant = getItem(position)
        holder.bind(cant)
    }

    class SelCantViewHolder(private val binding: CardSelCantBinding,
                     private val onSelectClick: (cantId: Int) -> Unit)
        : RecyclerView.ViewHolder(binding.root) {
        fun bind(cant: Cantiere){
            binding.cantiere = cant
            binding.executePendingBindings()
            binding.btnSelect.setOnClickListener {
                onSelectClick(cant.id) // ← qui passi solo l'ID
            }
        }
    }

    class SelCantDiffCallback : DiffUtil.ItemCallback<Cantiere>() {
        override fun areItemsTheSame(oldItem: Cantiere, newItem: Cantiere): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Cantiere, newItem: Cantiere): Boolean {
            return oldItem == newItem
        }
    }
}