package com.example.ediltrack.ui.view.fragment

import android.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import com.example.ediltrack.databinding.FragmentDettaglioDipendenteBinding
import com.example.ediltrack.ui.viewmodel.DipendenteSingoloViewModel
import com.example.ediltrack.util.UserRole
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import coil.load
import kotlinx.coroutines.launch

class DettaglioDipendenteFragment : Fragment() {
    private val args: DettaglioDipendenteFragmentArgs by navArgs()
    private lateinit var viewModel: DipendenteSingoloViewModel
    private lateinit var binding: FragmentDettaglioDipendenteBinding
    private val _binding get() = binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[DipendenteSingoloViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDettaglioDipendenteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        val args = DettaglioDipendenteFragmentArgs.fromBundle(requireArguments())
        viewModel.carica(args.dipendenteId)

        viewModel.url.observe(viewLifecycleOwner) { fullUrl ->
            if (!fullUrl.isNullOrEmpty()) {
                // Coil carica direttamente
                binding.imgProfilo.load(fullUrl) {
                    crossfade(true)
                }
            }
        }

        binding.btnModificaNome.setOnClickListener {
            binding.nomeUtente.isEnabled = true
            binding.nomeUtente.requestFocus()
            binding.nomeUtente.setSelection(binding.nomeUtente.text.length) // posiziona cursore alla fine
        }
        binding.btnModificaCognome.setOnClickListener {
            binding.cognomeUtente.isEnabled = true
            binding.cognomeUtente.requestFocus()
            binding.cognomeUtente.setSelection(binding.cognomeUtente.text.length) // posiziona cursore alla fine
        }
        binding.btnSalvaUtente.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                val success = viewModel.salva(args.dipendenteId)
                if (success) {
                    findNavController().popBackStack()
                }
            }
        }

        // osserva il ruolo dal ViewModel e aggiorna lo spinner

        //setto i ruoli possibili
        val allowedRoles = UserRole.entries
            .filter { it != UserRole.ADMIN }   // esclude l’Admin
            .map { it.displayed }

        // Adapter dello spinner
        val adapter = ArrayAdapter(
            requireContext(),
            R.layout.simple_spinner_item,
            allowedRoles
        )
        // Imposta il layout del dropdown
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.ruoloSpinner.adapter = adapter
        viewModel.ruolo.observe(viewLifecycleOwner) { ruolo ->
            val position = adapter.getPosition(ruolo.displayed)
            if (position >= 0) binding.ruoloSpinner.setSelection(position)
        }
        binding.ruoloSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                val selectedDisplay = parent.getItemAtPosition(pos) as String
                viewModel.ruolo.value = UserRole.fromDisplayName(selectedDisplay)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

    }



}