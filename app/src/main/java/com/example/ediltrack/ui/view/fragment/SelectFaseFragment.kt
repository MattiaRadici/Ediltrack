package com.example.ediltrack.ui.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ediltrack.R
import com.example.ediltrack.databinding.FragmentSelezionaFasiBinding
import com.example.ediltrack.ui.adapter.SelezionaFaseAdapter
import com.example.ediltrack.ui.viewmodel.HomeDipendenteViewModel

class SelectFaseFragment : Fragment() {

    private var _binding : FragmentSelezionaFasiBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: SelezionaFaseAdapter
    private val viewModel: HomeDipendenteViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSelezionaFasiBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        viewModel.getFase()

        adapter = SelezionaFaseAdapter(onSelectClick = {
            faseId -> viewModel.insertFase(faseId)

            findNavController().popBackStack()
        })

        binding.recyclerFasi.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerFasi.adapter = adapter

        viewModel.listaFasi.observe(viewLifecycleOwner) { lista ->
            adapter.submitList(lista)
        }

        viewModel.message.observe(viewLifecycleOwner) { msg ->
            msg?.let { Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show() }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}