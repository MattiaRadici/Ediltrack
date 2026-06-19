package com.example.ediltrack.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.ediltrack.databinding.CardSelFaseBinding
import com.example.ediltrack.model.Fase
import com.example.ediltrack.model.uimodel.CantiereUI

class SelezionaFaseAdapter(private val onSelectClick: (faseId: Int) -> Unit) :
    ListAdapter<Fase, SelezionaFaseAdapter.ViewHolder>(FaseDiffCallback()) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SelezionaFaseAdapter.ViewHolder {
        val binding = CardSelFaseBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding, onSelectClick)
    }

    override fun onBindViewHolder(holder: SelezionaFaseAdapter.ViewHolder, position: Int){
        val fase = getItem(position)
        holder.bind(fase)
    }

    class ViewHolder(private val binding: CardSelFaseBinding,
                     private val onSelectClick: (faseId: Int) -> Unit)
        : RecyclerView.ViewHolder(binding.root) {
        fun bind(fase: Fase){
            binding.fase = fase
            binding.executePendingBindings()
            binding.btnSelect.setOnClickListener {
                onSelectClick(fase.id) // ← qui passi solo l'ID
            }
        }
    }

    class FaseDiffCallback : DiffUtil.ItemCallback<Fase>() {
        override fun areItemsTheSame(oldItem: Fase, newItem: Fase): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Fase, newItem: Fase): Boolean {
            return oldItem == newItem
        }
    }


}