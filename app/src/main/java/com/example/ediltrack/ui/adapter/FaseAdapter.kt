package com.example.ediltrack.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.ediltrack.databinding.CardFaseBinding
import com.example.ediltrack.model.uimodel.FaseUI

class FaseAdapter(
    private val onRemoveClick: (faseId: Int) -> Unit,
    private val onFieldChanged: (fase: FaseUI) -> Unit
) : ListAdapter<FaseUI, FaseAdapter.FaseViewHolder>(FaseDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FaseViewHolder {
        // Corretto per l'inflate: passa parent e attachToRoot=false
        val binding = CardFaseBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FaseViewHolder(binding, onRemoveClick, onFieldChanged)
    }

    override fun onBindViewHolder(holder: FaseViewHolder, position: Int) {
        val fase = getItem(position)
        holder.bind(fase)
    }

    // Classe per il calcolo delle differenze
    class FaseDiffCallback : DiffUtil.ItemCallback<FaseUI>() {
        override fun areItemsTheSame(oldItem: FaseUI, newItem: FaseUI): Boolean {
            return oldItem.id == newItem.id
        }
        override fun areContentsTheSame(oldItem: FaseUI, newItem: FaseUI): Boolean {
            return oldItem == newItem
        }
    }

    inner class FaseViewHolder(
        private val binding: CardFaseBinding,
        onRemoveClick: (faseId: Int) -> Unit,
        onFieldChanged: (fase: FaseUI) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(fase: FaseUI) {
            binding.fase = fase
            binding.executePendingBindings()

            val focusListener = { view: View, hasFocus: Boolean ->
                if (!hasFocus) {
                    onFieldChanged(fase)
                }
            }

            // Applica il Focus Listener ai campi di input
            binding.titolo.setOnFocusChangeListener(focusListener)
            binding.descrizione.setOnFocusChangeListener(focusListener)
            binding.numero.setOnFocusChangeListener(focusListener)

            binding.checkTerminata.setOnCheckedChangeListener(null)

            //Forziamo lo stato visivo (per sicurezza, casomai il binding avesse lag)
            binding.checkTerminata.isChecked = fase.terminata

            //listener che salva SUBITO al click
            binding.checkTerminata.setOnCheckedChangeListener { _, isChecked ->
                fase.terminata = isChecked
                onFieldChanged(fase)
            }

            //LISTENER PER LA RIMOZIONE
            binding.btnEliminaFase.setOnClickListener {
                fase.id?.let { onRemoveClick(it) }
            }
        }
    }
}