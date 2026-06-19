package com.example.ediltrack.ui.view.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import coil.load
import com.example.ediltrack.databinding.FragmentImpostazioniBinding
import com.example.ediltrack.ui.viewmodel.ImpostazioniViewModel
// Sostituisci il seguente import con il path reale della tua LoginActivity
import com.example.ediltrack.ui.view.activity.LoginActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ImpostazioniFragment : Fragment() {

    private var _binding: FragmentImpostazioniBinding? = null
    private val binding get() = _binding!!

    // Inizializzazione del ViewModel legata al ciclo di vita del Fragment
    private val viewModel: ImpostazioniViewModel by viewModels()

    // Launcher per la selezione dell'immagine dalla memoria del dispositivo
    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            viewModel.setImageUri(uri)
            // Mostra l'anteprima locale usando Coil immediatamente, prima dell'upload
            binding.imgProfiloAnteprima.load(uri) {
                crossfade(true)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentImpostazioniBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Innesca il recupero dei dati da Supabase
        viewModel.caricaDatiUtente()

        setupObservers()
        setupListeners()
    }

    private fun setupObservers() {
        // Popola i campi Nome e Cognome appena arrivano dal DB
        viewModel.nome.observe(viewLifecycleOwner) { nome ->
            if (binding.inputNome.text.toString() != nome) {
                binding.inputNome.setText(nome)
            }
        }

        viewModel.cognome.observe(viewLifecycleOwner) { cognome ->
            if (binding.inputCognome.text.toString() != cognome) {
                binding.inputCognome.setText(cognome)
            }
        }

        // Osserva l'URL dal DB e caricalo con Coil
        viewModel.imageUrl.observe(viewLifecycleOwner) { url ->
            if (!url.isNullOrEmpty()) {
                binding.imgProfiloAnteprima.load(url) {
                    crossfade(true)
                }
            }
        }

        // Osserva i messaggi (errore/successo) emessi dal ViewModel
        viewModel.message.observe(viewLifecycleOwner) { msg ->
            if (msg.isNotEmpty()) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupListeners() {
        // Selezione Immagine
        binding.btnModificaImmagine.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }

        // Salvataggio Profilo e Password
        binding.btnSalvaTutto.setOnClickListener {
            val oldPass = binding.inputOldPassword.text.toString()
            val newPass = binding.inputNewPassword.text.toString()
            val repPass = binding.inputRepeatPassword.text.toString()

            // 1. Validazione Password (se l'utente sta cercando di cambiarla)
            val staCambiandoPassword = oldPass.isNotEmpty() || newPass.isNotEmpty() || repPass.isNotEmpty()

            if (staCambiandoPassword) {
                // Controllo 1: Ha inserito la vecchia password?
                if (oldPass.isEmpty()) {
                    Toast.makeText(context, "Inserisci la vecchia password per confermare la modifica", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // Controllo 2: Le nuove password coincidono?
                if (newPass != repPass) {
                    Toast.makeText(context, "Le nuove password non coincidono", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // Controllo 3: Lunghezza minima
                if (newPass.length < 6) {
                    Toast.makeText(context, "La nuova password deve avere almeno 6 caratteri", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // Controllo 4: La nuova password è uguale alla vecchia?
                if (oldPass == newPass) {
                    Toast.makeText(context, "La nuova password deve essere diversa da quella attuale", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // Se passa tutti i controlli, aggiorna la password.
                // Usiamo un blocco per assicurarci che i campi si puliscano SOLO se la chiamata parte
                viewModel.aggiornaPassword(newPass)

                // Opzionale: potresti voler svuotare i campi solo se il viewModel ti conferma il successo,
                // ma per ora li svuotiamo subito per pulizia UI.
                binding.inputOldPassword.text?.clear()
                binding.inputNewPassword.text?.clear()
                binding.inputRepeatPassword.text?.clear()
            }

            // 2. Aggiornamento Anagrafica e Immagine (avviene sempre)
            viewModel.nome.value = binding.inputNome.text.toString()
            viewModel.cognome.value = binding.inputCognome.text.toString()

            viewModel.salvaProfilo(requireContext())
        }

        // Logout
        binding.btnLogout.setOnClickListener {
            viewModel.eseguiLogout {
                navigaALogin()
            }
        }

        // Eliminazione Account
        binding.btnEliminaAccount.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Elimina Account")
                .setMessage("Questa azione è irreversibile. Tutti i tuoi dati verranno cancellati in modo permanente. Vuoi procedere?")
                .setPositiveButton("Elimina") { _, _ ->
                    viewModel.eliminaAccount {
                        navigaALogin()
                    }
                }
                .setNegativeButton("Annulla", null)
                .show()
        }
    }

    private fun navigaALogin() {
        val intent = Intent(requireContext(), LoginActivity::class.java)
        // Rimuove l'intero backstack. Previene il ritorno al fragment premendo il tasto "Indietro"
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Prevenzione memory leak
        _binding = null
    }
}