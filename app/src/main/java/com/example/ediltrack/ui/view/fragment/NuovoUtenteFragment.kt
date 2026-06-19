package com.example.ediltrack.ui.view.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.ediltrack.databinding.FragmentNuovoUtenteBinding
import com.example.ediltrack.ui.viewmodel.NuovoUtenteViewModel
import com.example.ediltrack.util.UtenteMode

class NuovoUtenteFragment : Fragment() {

    private lateinit var _binding: FragmentNuovoUtenteBinding
    private val binding get() = _binding

    private lateinit var viewModel: NuovoUtenteViewModel

    // Questo gestisce il ritorno dalla galleria
    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            viewModel.immagineUri = uri // Salva l'URI nel ViewModel

            Log.d("PhotoPicker", "Media selezionata: $uri")
            Toast.makeText(requireContext(), "Immagine caricata con successo", Toast.LENGTH_SHORT).show()

        } else {
            // L'utente ha annullato
            Log.d("PhotoPicker", "Nessun media selezionato")
            Toast.makeText(requireContext(), "Nessuna immagine selezionata", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNuovoUtenteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[NuovoUtenteViewModel::class.java]

        // Spinner setup
        val ruoli = UtenteMode.values().map { it.name }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, ruoli)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinner.adapter = adapter

        //LISTENER PER UPLOAD
        binding.btnUploadImg.setOnClickListener {
            // Apre il selettore nativo di Android (foto e video, solo immagini in questo caso)
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        // Salvataggio utente
        binding.salva.setOnClickListener {
            val nome = binding.nome.text.toString()
            val cognome = binding.cognome.text.toString()
            val ruolo = binding.spinner.selectedItem.toString()
            val mail = binding.email.text.toString()
            val pw = binding.password.text.toString()

            if (nome.isEmpty() || cognome.isEmpty()) {
                Toast.makeText(requireContext(), "Nome e Cognome obbligatori", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.salvaUtente(requireContext(), mail, pw, nome, cognome, ruolo)
        }

        // Osserva messaggi dal ViewModel
        viewModel.message.observe(viewLifecycleOwner) { msg ->
            if (msg.isNullOrBlank()) return@observe

            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()

            if (msg == "Utente salvato con successo") {
                findNavController().popBackStack()
            }
        }
    }
}