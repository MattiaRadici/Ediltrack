package com.example.ediltrack.ui.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ediltrack.databinding.FragmentAssociaFasiBinding
import com.example.ediltrack.ui.adapter.FaseAdapter
import com.example.ediltrack.ui.viewmodel.SingoloCantViewModel

class FasiEditFragment : Fragment() {

    // Riutilizziamo il layout grafico esistente
    private var _binding: FragmentAssociaFasiBinding? = null
    private val binding get() = _binding!!

    // Usiamo il ViewModel del singolo cantiere
    private val viewModel: SingoloCantViewModel by viewModels()

    // Recuperiamo l'ID cantiere dagli argomenti
    private val args: FasiEditFragmentArgs by navArgs()

    private lateinit var adapter: FaseAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAssociaFasiBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val cantiereId = args.cantiereId

        adapter = FaseAdapter(
            onRemoveClick = { faseId ->
                eliminaFase(faseId, cantiereId)
            },
            onFieldChanged = {}
        )

        binding.recyclerFasi.layoutManager = LinearLayoutManager(context)
        binding.recyclerFasi.adapter = adapter

        binding.btnAggiungiFase.setOnClickListener {
            viewModel.aggiungiFaseVuotaAlDb(cantiereId)
        }

        binding.btnSalva.setOnClickListener {
            viewModel.salvaTutteLeFasi(adapter.currentList) { successo ->
                if (successo) {
                    Toast.makeText(context, "Salvato con successo!", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                } else {
                    Toast.makeText(context, "Errore salvataggio", Toast.LENGTH_SHORT).show()
                }
            }
        }

        viewModel.caricaFasi(cantiereId)

        viewModel.fasiList.observe(viewLifecycleOwner) { listaFasiUI ->
            adapter.submitList(listaFasiUI)
        }
    }

    private fun eliminaFase(faseId: Int, cantiereId: Int) {
        viewModel.eliminaFase(faseId) { successo ->
            if (successo) {
                Toast.makeText(context, "Fase eliminata", Toast.LENGTH_SHORT).show()
                // Ricarichiamo la lista per vedere le modifiche
                viewModel.caricaFasi(cantiereId)
            } else {
                Toast.makeText(context, "Errore durante l'eliminazione", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}