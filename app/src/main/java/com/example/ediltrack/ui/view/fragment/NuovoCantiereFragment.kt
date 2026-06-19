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
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.example.ediltrack.R
import com.example.ediltrack.databinding.FragmentNuovoCantiereBinding
import com.example.ediltrack.ui.viewmodel.NuovoCantiereViewModel
import com.example.ediltrack.util.UtenteMode.*

class NuovoCantiereFragment : Fragment() {

    private val viewModel: NuovoCantiereViewModel by navGraphViewModels(R.id.nav_graph_nuovo_cantiere)
    private lateinit var _binding: FragmentNuovoCantiereBinding
    private val binding get() = _binding
    private lateinit var selectImageLauncher: ActivityResultLauncher<Intent>

    val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        // Callback: qui ricevi direttamente l'URI o null
        if (uri != null) {
            viewModel.setCantiereImageUri(uri)
            Log.d("PhotoPicker", "Media selezionata: $uri")
        } else {
            Log.d("PhotoPicker", "Nessun media selezionato")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNuovoCantiereBinding.inflate(inflater, container, false)
        _binding.viewModel = viewModel
        _binding.lifecycleOwner = viewLifecycleOwner

        binding.btnAssociaCapoCant.setOnClickListener {
            val action = NuovoCantiereFragmentDirections
                .actionNuovoFragmentToSelezionaUtentiFragment(CAPOCANTIERE.name)
            findNavController().navigate(action)
        }

        binding.btnUploadImg.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        binding.btnAssociaOperaio.setOnClickListener {
            val action = NuovoCantiereFragmentDirections
                .actionNuovoFragmentToSelezionaUtentiFragment(OPERAIO.name)
            findNavController().navigate(action)
        }
        binding.btnFasi.setOnClickListener {
            val action = NuovoCantiereFragmentDirections.actionNuovoFragmentToSelezionaFasiFragment()
            findNavController().navigate(action)
        }
        binding.btnSalvaCantiere.setOnClickListener {
            viewModel.salvaCantiere(requireContext())
        }


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.message.observe(viewLifecycleOwner) { msg ->
            if (msg.isNullOrBlank()) return@observe
            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
            viewModel.resetmess()
            if (msg == "Cantiere salvato con successo") {
                findNavController().popBackStack()
            }
        }
    }
}