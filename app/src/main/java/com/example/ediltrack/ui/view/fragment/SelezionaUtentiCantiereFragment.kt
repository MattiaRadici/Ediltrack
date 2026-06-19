package com.example.ediltrack.ui.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ediltrack.databinding.FragmentSelezionaUtentiBinding
import com.example.ediltrack.ui.adapter.AdapterMode
import com.example.ediltrack.ui.adapter.DipendenteAdapter
import com.example.ediltrack.ui.viewmodel.SingoloCantViewModel
import com.example.ediltrack.util.UserRole
import com.example.ediltrack.util.UtenteMode

class SelezionaUtentiCantiereFragment : Fragment() {

    private var _binding: FragmentSelezionaUtentiBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SingoloCantViewModel by viewModels()
    private val args: SelezionaUtentiCantiereFragmentArgs by navArgs()

    private lateinit var adapter: DipendenteAdapter
    private lateinit var mode: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSelezionaUtentiBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val cantiereId = args.cantiereId
        mode = args.mode

        binding.recyclerUtenti.layoutManager = LinearLayoutManager(requireContext())

        // 1. FAI PARTIRE IL CARICAMENTO
        // Scarica sia la lista utenti sia gli ID "assignedIds" dal DB
        viewModel.initSelezionaUtenti(cantiereId, mode)

        // 2. ASPETTA CHE ARRIVINO GLI ID DAL DB
        // Appena arrivano gli ID già assegnati, creiamo l'Adapter (così le checkbox sono giuste)
        viewModel.assignedIds.observe(viewLifecycleOwner) { idsGiaAssegnati ->

            adapter = DipendenteAdapter(
                mode = if (mode == UtenteMode.CAPOCANTIERE.name) AdapterMode.CAPOCANTIERE else AdapterMode.OPERAIO,

                // Callback Capocantiere
                onCapoCheck = { id, checked ->
                    if (checked) aggiornaCapocantiere(cantiereId, id)
                },

                onItemClick = {}, // non serve

                attuali = emptyList(), // La lista piena gliela diamo dopo

                // QUI PASSIAMO GLI ID SCARICATI DAL DB AL TUO ADAPTER
                preselezionati = idsGiaAssegnati
            )

            binding.recyclerUtenti.adapter = adapter

            // Se la lista utenti è già pronta, aggiornala subito
            viewModel.utentiUI.value?.let { adapter.updateData(it) }
        }

        // 3. OSSERVA LA LISTA UTENTI (Paginazione)
        viewModel.utentiUI.observe(viewLifecycleOwner) { lista ->
            // Aggiorniamo l'adapter solo se è stato già creato (punto 2)
            if (::adapter.isInitialized) {
                adapter.updateData(lista)
            }
        }

        // 4. SCROLL LISTENER (Paginazione)
        binding.recyclerUtenti.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                val lm = rv.layoutManager as LinearLayoutManager
                if (lm.findLastVisibleItemPosition() >= adapter.itemCount - 5) {
                    viewModel.loadNextPage()
                }
            }
        })

        // 5. FAB SALVA (SOLO PER OPERAI)
        if (mode == UtenteMode.OPERAIO.name) {
            binding.fabSalvaOperatori.visibility = View.VISIBLE
            binding.fabSalvaOperatori.setOnClickListener {
                // Recupera gli ID selezionati dall'adapter usando la tua funzione
                val selectedIds = adapter.getSelectedIds()
                salvaOperai(cantiereId, selectedIds)
            }
        } else {
            binding.fabSalvaOperatori.visibility = View.GONE
        }

        // 6. RICERCA
        binding.searchUtenti.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                val ruolo = if (mode == UtenteMode.CAPOCANTIERE.name) UserRole.CAPOCANTIERE.code else UserRole.OPERAIO.code
                viewModel.startNewSearch(query, ruolo)
                return true
            }
            override fun onQueryTextChange(newText: String?): Boolean { return true }
        })
    }

    private fun aggiornaCapocantiere(cantiereId: Int, nuovoCapoId: String) {
        viewModel.aggiornaCapocantiere(cantiereId, nuovoCapoId) { ok ->
            if (ok) {
                Toast.makeText(context, "Capocantiere aggiornato", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            } else Toast.makeText(context, "Errore", Toast.LENGTH_SHORT).show()
        }
    }

    private fun salvaOperai(cantiereId: Int, listaIds: List<String>) {
        viewModel.aggiornaListaOperai(cantiereId, listaIds) { ok ->
            if (ok) {
                Toast.makeText(context, "Operai aggiornati", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            } else Toast.makeText(context, "Errore salvataggio", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}