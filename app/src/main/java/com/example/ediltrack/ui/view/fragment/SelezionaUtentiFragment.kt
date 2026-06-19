package com.example.ediltrack.ui.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ediltrack.R
import com.example.ediltrack.databinding.FragmentSelezionaUtentiBinding
import com.example.ediltrack.ui.adapter.AdapterMode
import com.example.ediltrack.ui.adapter.DipendenteAdapter
import com.example.ediltrack.ui.viewmodel.NuovoCantiereViewModel
import com.example.ediltrack.util.UserRole
import com.example.ediltrack.util.UtenteMode


class SelezionaUtentiFragment : Fragment() {

    private lateinit var _binding: FragmentSelezionaUtentiBinding
    private val binding get() = _binding

    private val viewModel: NuovoCantiereViewModel by navGraphViewModels(R.id.nav_graph_nuovo_cantiere)
    private lateinit var adapter: DipendenteAdapter
    private lateinit var mode: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Prendo gli args
        val args = SelezionaUtentiFragmentArgs.fromBundle(requireArguments())
        mode = args.mode
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSelezionaUtentiBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding.viewModel = viewModel
        _binding.lifecycleOwner = viewLifecycleOwner
        binding.recyclerUtenti.layoutManager = LinearLayoutManager(requireContext())
        viewModel.setRole(UserRole.valueOf(mode).code)

        // Configura adapter con modalità scelta
        adapter = DipendenteAdapter(
            mode = when (mode) {
                UtenteMode.OPERAIO.name -> AdapterMode.OPERAIO
                UtenteMode.CAPOCANTIERE.name -> AdapterMode.CAPOCANTIERE
                else -> {
                    throw IllegalArgumentException("Modalità non supportata: $mode")
                }
            },
            onCapoCheck = { id, checked ->
                val idsSelezionati = adapter.getSelectedIds()
                //associa ID capocant, messo first or nulla nel caso deselezionasse il capocantiere
                viewModel.setCapocant(idsSelezionati.firstOrNull()?:"")
                // naviga al fragment nuovo cantiere con l'id del capocantiere selezionato
                if (checked) {
                    //solo nel caso di selezione è true
                    findNavController().popBackStack()
                }
            },
            onItemClick = {}, // non serve in selezione
            attuali = emptyList(),
            preselezionati = when (mode) {
                UtenteMode.CAPOCANTIERE.name -> viewModel.getCapocant()?.let { listOf(it) } ?: emptyList()    //passo il valore del capocant
                UtenteMode.OPERAIO.name -> viewModel.getDip() ?: emptyList()   //passo la lista dei dip selezionati
                else -> {
                    emptyList()
                }
            }
        )

        binding.recyclerUtenti.adapter = adapter


        //nel caso io stia nell'interfaccia operatore faccio apparire il FAB
        if (mode == UtenteMode.OPERAIO.name) {
            binding.fabSalvaOperatori.visibility = View.VISIBLE
            //listener per il bottone, salvo gli operatori selezionati e navigo indietro
            binding.fabSalvaOperatori.setOnClickListener {
                val selectedIds = adapter.getSelectedIds()
                viewModel.setUtenti(selectedIds)
                findNavController().popBackStack()
            }
        } else {
            binding.fabSalvaOperatori.visibility = View.GONE
        }

        // Osserva LiveData dal ViewModel
        viewModel.utentiUI.observe(viewLifecycleOwner) { lista ->
            adapter.updateData(lista)
        }

        // Configura SearchView
        binding.searchUtenti.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                viewModel.startNewSearch(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.startNewSearch(newText)
                return true
            }
        })

        // Scroll listener per lazy load DB
        binding.recyclerUtenti.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                val lm = rv.layoutManager as LinearLayoutManager
                if (lm.findLastVisibleItemPosition() >= adapter.itemCount - 5) {
                    viewModel.loadNextPage()
                }
            }
        })

        // Carica prima pagina
        viewModel.loadNextPage()
    }

    fun getSelectedIds(): List<String> = adapter.getSelectedIds()
}