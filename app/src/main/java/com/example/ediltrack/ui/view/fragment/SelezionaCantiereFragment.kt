package com.example.ediltrack.ui.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ediltrack.databinding.FragmentSelezionaCantiereBinding
import com.example.ediltrack.ui.adapter.SelezioneCantiereAdapter
import com.example.ediltrack.ui.viewmodel.HomeDipendenteViewModel

class SelezionaCantiereFragment : Fragment() {

    private var _binding : FragmentSelezionaCantiereBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: SelezioneCantiereAdapter
    private val viewModel : HomeDipendenteViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSelezionaCantiereBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        adapter = SelezioneCantiereAdapter(onSelectClick = {
            cant -> viewModel.selCant(cant)
            findNavController().popBackStack()
        })

        //Configurazione RecyclerView
        binding.recyclerCantieri.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerCantieri.adapter = adapter

        //observe lista viewmodel
        viewModel.listaCantieri.observe(viewLifecycleOwner) { lista ->
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