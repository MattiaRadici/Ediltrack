package com.example.ediltrack.ui.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView // Import necessario per OnScrollListener
import com.example.ediltrack.databinding.FragmentHomeCantieriBinding
import com.example.ediltrack.ui.adapter.CantiereAdapter
import com.example.ediltrack.ui.viewmodel.HomeViewModel
import com.example.ediltrack.util.UserRole
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class CantieriFragment : Fragment(){

    private lateinit var homeViewModel: HomeViewModel
    private lateinit var adapter: CantiereAdapter
    private lateinit var _binding: FragmentHomeCantieriBinding
    private val binding get() = _binding

    private var searchJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        homeViewModel = ViewModelProvider(this)[HomeViewModel::class.java]
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) : View {
        _binding = FragmentHomeCantieriBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Associa il ViewModel e il ciclo di vita al binding
        binding.viewModel = homeViewModel
        binding.lifecycleOwner = viewLifecycleOwner

        // setup recycler view
        val recyclerView = binding.recyclerViewCantieri
        val layoutManager = LinearLayoutManager(context) // Dichiaro layoutManager qui
        recyclerView.layoutManager = layoutManager

        adapter = CantiereAdapter(){cantiereId ->
            val action = CantieriFragmentDirections.actionNavigationHomeToDettagliCantieriFragment(cantiereId)
            findNavController().navigate(action)
        }
        recyclerView.adapter = adapter
        homeViewModel.loadRole()
        homeViewModel.ruolo.observe(viewLifecycleOwner) { ruolo ->
            // 1. Visibilità per fabNuovoCantiere (Aggiungi Cantiere)
            if (ruolo == UserRole.ADMIN ) {
                binding.fabNuovoCantiere.visibility = View.VISIBLE
            } else {
                binding.fabNuovoCantiere.visibility = View.GONE
            }

            // 2. Visibilità per fabNotifiche (Notifiche)
            if (ruolo == UserRole.CAPOCANTIERE) {
                binding.fabNotifiche.visibility = View.VISIBLE
            } else {
                binding.fabNotifiche.visibility = View.GONE
            }
        }

        // --- INIZIO INTEGRAZIONE PAGINAZIONE ---

        // 1. Scroll listener per caricare la pagina successiva (Infinite Scroll)
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(rv, dx, dy)

                // Ignora lo scroll verso l'alto
                if (dy <= 0) return

                val totalItemCount = layoutManager.itemCount
                val lastVisible = layoutManager.findLastVisibleItemPosition()

                // Se siamo vicini al fondo (es. mancano 5 elementi per arrivare alla fine)
                val visibleThreshold = 5
                if (lastVisible >= totalItemCount - visibleThreshold) {
                    homeViewModel.loadNextPage()
                }
            }
        })

        // 2. Search Listener per avviare una nuova ricerca paginata
        binding.searchView.setOnQueryTextListener(object : android.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                searchJob?.cancel()
                // Avvia la ricerca quando l'utente preme Invio/Cerca
                homeViewModel.startNewSearch(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                searchJob?.cancel()

                // Avvia la ricerca in tempo reale ad ogni digitazione
                searchJob = lifecycleScope.launch {
                    //delay per una ricerca corretta
                    delay(300)

                    // 3. Esegui la ricerca solo se la coroutine non è stata cancellata
                    homeViewModel.startNewSearch(newText)
                }
                return true
            }
        })

        // --- FINE INTEGRAZIONE PAGINAZIONE ---
        homeViewModel.cantieriUI.observe(viewLifecycleOwner) { lista ->
            adapter.updateData(lista)
        }

        homeViewModel.message.observe(viewLifecycleOwner) { msg ->
            msg?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }
        binding.fabNuovoCantiere.setOnClickListener {
            val action =
                CantieriFragmentDirections.actionNavigationHomeToNuovo()
            findNavController().navigate(action)
        }
        binding.fabNotifiche.setOnClickListener{
            val action = CantieriFragmentDirections.actionCantToProb()
            findNavController().navigate(action)
        }

        // Dopo aver configurato gli ascoltatori, avviamo il caricamento della prima pagina
        // Ho cambiato da loadNextPage() a startNewSearch(null) per uniformità,
        // startNewSearch(null) resetta e chiama loadNextPage() la prima volta.
        homeViewModel.startNewSearch(null)
    }

}