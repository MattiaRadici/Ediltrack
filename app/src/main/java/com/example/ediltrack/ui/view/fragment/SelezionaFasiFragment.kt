package com.example.ediltrack.ui.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ediltrack.R
import com.example.ediltrack.databinding.FragmentAssociaFasiBinding
import com.example.ediltrack.model.uimodel.FaseUI
import com.example.ediltrack.ui.adapter.FaseAdapter
import com.example.ediltrack.ui.viewmodel.NuovoCantiereViewModel

class SelezionaFasiFragment : Fragment() {

    private lateinit var _binding: FragmentAssociaFasiBinding
    private val binding get() = _binding
    private val viewModel: NuovoCantiereViewModel by navGraphViewModels(R.id.nav_graph_nuovo_cantiere)
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

        binding.lifecycleOwner = viewLifecycleOwner

        adapter = FaseAdapter(
            onRemoveClick = { faseId ->
                viewModel.removeFase(faseId)
            },
            onFieldChanged = { updatedFase ->
                viewModel.updateSingleFase(updatedFase)
            }

        )

        binding.recyclerFasi.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerFasi.adapter = adapter

        // Usa updateList() che gestisce intelligentemente gli aggiornamenti
        viewModel.fasi.observe(viewLifecycleOwner) { fasi ->
            adapter.submitList(fasi)
        }

        binding.btnAggiungiFase.setOnClickListener {
            val currentFasi = viewModel.fasi.value ?: emptyList()
            val newNumeroFase = (currentFasi.size + 1).toString()
            val newId = (currentFasi.maxOfOrNull { it.id ?: 0 } ?: 0) + 1
            val nuovaFase = FaseUI(
                id = newId,
                titolo = "",
                numeroFase = newNumeroFase,
                descrizione = ""
            )
            viewModel.addFase(nuovaFase)
        }

        binding.btnSalva.setOnClickListener {
            findNavController().popBackStack()
        }
    }

}