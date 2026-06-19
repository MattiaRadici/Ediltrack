package com.example.ediltrack.ui.view.fragment

import android.R
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import coil.load
import com.example.ediltrack.databinding.FragmentProblematicaDetailBinding
import com.example.ediltrack.model.Problematica
import com.example.ediltrack.ui.viewmodel.ProblematicaDetailViewModel
import com.example.ediltrack.util.ConnectDB
import com.example.ediltrack.util.ProblemaStato
import com.example.ediltrack.util.UserRole

class ProblematicaDetailFragment : Fragment() {

    private var _binding: FragmentProblematicaDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProblematicaDetailViewModel by viewModels()
    private val args: ProblematicaDetailFragmentArgs by navArgs()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProblematicaDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // RECUPERO DATI
        val problematica = args.problematicaKey

        // Impostiamo il lifecycle owner per il DataBinding XML
        binding.lifecycleOwner = viewLifecycleOwner

        binding.viewModel = viewModel
        //nel caso non ho un capocantiere elimino la vista della modifica
        viewModel.loadRole()

        viewModel.initData(problematica)
        setupUI(problematica)
        setupPannelloCapocantiere(problematica)
        viewModel.ruolo.observe(viewLifecycleOwner) { ruoloLetto ->
            // lo devo fare solo quando ho l'observe che mi da conferma che il valore sia cambiato
            setupVisibilityByRole()
        }
        setupObservers()
    }

    private fun setupUI(item: Problematica) {
        //Cambio anche il colore
        val stato = ProblemaStato.fromCode(item.validazione)
        binding.tvStatoDetail.text = stato.displayed
        try {
            val color = Color.parseColor(stato.colorHex)
            binding.tvStatoDetail.setTextColor(color)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setupObservers() {
        //carico l'immagine
        viewModel.imageUrl.observe(viewLifecycleOwner) { url ->
            if (url != null) {
                binding.ivProblematica.load(url) {
                    crossfade(true)
                }
            }
        }


        // Observer Risultato Salvataggio
        viewModel.updateResult.observe(viewLifecycleOwner) { successo ->
            if (successo == true) {
                Toast.makeText(context, "Aggiornato con successo!", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack() // Torna indietro
                viewModel.resetUpdateResult()
            } else if (successo == false) {
                Toast.makeText(context, "Errore durante il salvataggio.", Toast.LENGTH_SHORT).show()
                viewModel.resetUpdateResult()
            }
        }

    }

    // Gestisco la visibilità
    private fun setupVisibilityByRole() {
        if (viewModel.ruolo.value == UserRole.OPERAIO.code) {
            // OPERAIO:
            binding.tilSelezioneAzione.visibility = View.GONE
            binding.btnConfermaAzione.visibility = View.GONE
            binding.etNoteCapo.isEnabled = false
        } else {
            // CAPOCANTIERE / AMMINISTRATORE:
            binding.layoutAzioniCapocantiere.visibility = View.VISIBLE
            binding.tilSelezioneAzione.visibility = View.VISIBLE
            binding.btnConfermaAzione.visibility = View.VISIBLE
            binding.etNoteCapo.isEnabled = true
        }
    }

    private fun setupPannelloCapocantiere(item: Problematica) {

        // Riempio note esistenti
        if (!item.commento.isNullOrEmpty()) {
            binding.etNoteCapo.setText(item.commento)
        }
        // Configuro il Dropdown usando l'Enum
        val opzioni = ProblemaStato.entries.map { it.displayed }
        val adapter = ArrayAdapter(requireContext(), R.layout.simple_dropdown_item_1line, opzioni)
        (binding.tilSelezioneAzione.editText as? AutoCompleteTextView)?.apply {
            setAdapter(adapter)
            // Seleziono lo stato attuale
            val statoAttuale = ProblemaStato.fromCode(item.validazione)
            setText(statoAttuale.displayed, false)
        }
        // Bottone Salva
        binding.btnConfermaAzione.setOnClickListener {
            val selezione = binding.actvAzione.text.toString()
            val statoEnum = ProblemaStato.fromDisplayName(selezione)
            val commento = binding.etNoteCapo.text.toString()
            viewModel.salvaModificheProblematica(statoEnum.code, commento)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}