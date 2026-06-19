package com.example.ediltrack.ui.view.fragment

import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.example.ediltrack.R
import com.example.ediltrack.databinding.FragmentHomeDipendenteBinding
import com.example.ediltrack.ui.viewmodel.HomeDipendenteViewModel
import com.example.ediltrack.util.UtenteMode.OPERAIO

class HomeDipendenteFragment:Fragment() {
    private val viewModel: HomeDipendenteViewModel by activityViewModels()
    private lateinit var _binding: FragmentHomeDipendenteBinding
    private val binding get() = _binding
    private lateinit var selectImageLauncher: ActivityResultLauncher<Intent>

    // Launcher per selezionare immagine
    val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        // Callback: qui ricevi direttamente l'URI o null
        if (uri != null) {
            viewModel.setImageUri(uri)
            Log.d("PhotoPicker", "Media selezionata: $uri")
        } else {
            Log.d("PhotoPicker", "Nessun media selezionato")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeDipendenteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?){
        super.onViewCreated(view, savedInstanceState)

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        binding.btnUploadImg.setOnClickListener {
            //apro la select dell'immagine , action pick mi da la possibilita di selezionare un file
            //mediastore mi dice solo esterni uri
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
        binding.btnselFase.setOnClickListener {
            if (viewModel.cantiere.value == null){
                viewModel.setMessage("Selezionare prima un cantiere")
            }
            else{
                val action = HomeDipendenteFragmentDirections
                    .actionNavigationHomeDipToSelFaseFragmentt()
                findNavController().navigate(action)
            }

        }
        binding.btnSelCant.setOnClickListener {
            val action = HomeDipendenteFragmentDirections
                .actionNavigationHomeDipToSelectCantiere()
            findNavController().navigate(action)
        }
        binding.btnSalva.setOnClickListener {
            viewModel.salva(requireContext())
        }
        viewModel.message.observe(viewLifecycleOwner) { msg ->
            if (msg.isNullOrBlank()) return@observe
            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()


            if (msg == "Salvataggio salvato con successo") {
                viewModel.reset()
            }
        }
    }
}