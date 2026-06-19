package com.example.ediltrack.ui.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ediltrack.R
import com.example.ediltrack.databinding.FragmentProblematicheListBinding
import com.example.ediltrack.ui.adapter.ProblematicaListAdapter
import com.example.ediltrack.ui.viewmodel.ProblematicheListViewModel
import com.example.ediltrack.util.ProblemaStato
import com.example.ediltrack.ui.view.fragment.ProblematicheListFragment

class ProblematicheListFragment : Fragment() {

    // Gestione del ViewBinding
    private var _binding: FragmentProblematicheListBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProblematicheListViewModel by viewModels()
    private lateinit var adapter: ProblematicaListAdapter

    private val args: ProblematicheListFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProblematicheListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner


        val id = args.cantiereIdFilter
        val cantId = if (id != -1) id else null
        viewModel.init(cantId)
        setupRecyclerView()
        setupFilters()
        setupObservers()



    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshData()
    }

    private fun setupRecyclerView() {
        adapter = ProblematicaListAdapter(emptyList()) { problematica ->
            val action = ProblematicheListFragmentDirections
                .actionListaProblematicheToDettaglio(problematicaKey = problematica)
            findNavController().navigate(action)
        }

        binding.rvProblematiche.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@ProblematicheListFragment.adapter
        }
    }

    private fun setupFilters() {
        //avviso il VM per la ricerca
        binding.etSearch.addTextChangedListener { text ->
            viewModel.onSearchQueryChanged(text.toString())
        }

        //LISTENER CHIPS
        binding.chipGroupFiltri.setOnCheckedStateChangeListener { _, checkedIds ->
            val selectedStatus: ProblemaStato? = if (checkedIds.isEmpty()) {
                null
            } else {
                when (checkedIds[0]) {
                    R.id.chipTutti -> null
                    R.id.chipNonLetto -> ProblemaStato.NON_LETTO
                    R.id.chipDaControllare -> ProblemaStato.DA_CONTROLLARE
                    R.id.chipErrore -> ProblemaStato.ERRORE
                    R.id.chipApprovato -> ProblemaStato.APPROVATO
                    else -> null
                }
            }
            viewModel.onStatusFilterChanged(selectedStatus)
        }
    }

    private fun setupObservers() {
        //aggiorno l'adapter con nuovi dati dal DB
        viewModel.problematiche.observe(viewLifecycleOwner) { lista ->
            adapter.updateData(lista)
        }

        //"nessuna problematica" se la lista è vuota
        viewModel.isEmpty.observe(viewLifecycleOwner) { isEmpty ->
            binding.tvEmptyList.visibility = if (isEmpty) View.VISIBLE else View.GONE
            binding.rvProblematiche.visibility = if (isEmpty) View.GONE else View.VISIBLE
        }

        // mostriamo un Toast se c'è un messaggio
        viewModel.message.observe(viewLifecycleOwner) { msg ->
            if (!msg.isNullOrEmpty()) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show()
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}