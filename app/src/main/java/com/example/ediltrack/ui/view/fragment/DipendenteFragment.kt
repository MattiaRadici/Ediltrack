package com.example.ediltrack.ui.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ediltrack.databinding.FragmentDipendentiBinding
import com.example.ediltrack.ui.adapter.AdapterMode
import com.example.ediltrack.ui.adapter.DipendenteAdapter
import com.example.ediltrack.ui.viewmodel.DipendentiViewModel

class DipendenteFragment : Fragment() {

    private lateinit var adapter: DipendenteAdapter
    private lateinit var _binding: FragmentDipendentiBinding
    private lateinit var _viewModel: DipendentiViewModel
    private val binding get() = _binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _viewModel = ViewModelProvider(this)[DipendentiViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDipendentiBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        // RecyclerView
        val recyclerView = binding.recyclerViewOperatore
        val layoutManager = LinearLayoutManager(context)
        recyclerView.layoutManager = layoutManager

        // Adapter
        adapter = DipendenteAdapter(
            mode = AdapterMode.LISTA,
            onItemClick = { uid ->
                val action = DipendenteFragmentDirections
                    .actionNavigationListopToDettagliOperai(uid)
                findNavController().navigate(action)
            },
            onCapoCheck = null,
            attuali = emptyList()
        )
        recyclerView.adapter = adapter

        // Scroll listener per caricamento pagine successive
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(rv, dx, dy)
                val totalItemCount = layoutManager.itemCount
                val lastVisible = layoutManager.findLastVisibleItemPosition()
                if (lastVisible >= totalItemCount - 5) { // se siamo vicini al fondo
                    _viewModel.loadNextPage()
                }
            }
        })

        // SearchView (addTextChangedListener)
        binding.searchView.setOnQueryTextListener(object : android.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                _viewModel.startNewSearch(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                _viewModel.startNewSearch(newText)
                return true
            }
        })

        binding.fabNuovoUtente.setOnClickListener{
            val action = DipendenteFragmentDirections.actionNavigationListoToNuovo()
            findNavController().navigate(action)
        }

        // Osserva i dati del ViewModel
        _viewModel.dipendentiUI.observe(viewLifecycleOwner) { lista ->
            adapter.updateData(lista)
        }

        _viewModel.message.observe(viewLifecycleOwner) { msg ->
            msg?.let { Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show() }
        }

        // Carica la prima pagina
        _viewModel.startNewSearch(null)
    }
}