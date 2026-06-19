package com.example.ediltrack.ui.view.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import coil.load
import com.example.ediltrack.databinding.FragmentDettaglioCantiereBinding
import com.example.ediltrack.ui.viewmodel.DipendentiViewModel
import com.example.ediltrack.ui.viewmodel.SingoloCantViewModel
import com.example.ediltrack.util.UtenteMode

class SingoloCantFragment : Fragment() {

    private val args: SingoloCantFragmentArgs by navArgs()
    private lateinit var viewModel: SingoloCantViewModel
    private lateinit var binding: FragmentDettaglioCantiereBinding
    private val _binding get() = binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[SingoloCantViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDettaglioCantiereBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        binding.btnVediFasi.setOnClickListener{
            //inefficenza, caricaFasi2volte
            val act = SingoloCantFragmentDirections.actionDettaglioCantToFasi(args.cantiereId)
            findNavController().navigate(act)
        }

        viewModel.imageUrl.observe(viewLifecycleOwner) { url ->
            Log.e("CantiereImmagine", "URL: $url")
            if (!url.isNullOrEmpty()) {
                // Caricamento con Coil
                binding.imgDettCant.load(url) {
                    crossfade(true)
                }
            }
        }

        binding.btnVediReport.setOnClickListener{
            val act = SingoloCantFragmentDirections.actionDettaglioCantToProb(
                cantiereIdFilter = args.cantiereId
            )
            findNavController().navigate(act)
        }

        // --- 1. GESTIONE MODIFICA CAPOCANTIERE ---
        binding.btnModificaCapocantiere.setOnClickListener {
            val action = SingoloCantFragmentDirections.actionDettaglioCantToSelezionaUtenti(
                cantiereId = args.cantiereId,
                mode = UtenteMode.CAPOCANTIERE.name // "CAPOCANTIERE"
            )
            findNavController().navigate(action)
        }

        // --- 2. GESTIONE MODIFICA OPERAI ---
        binding.btnModificaOperai.setOnClickListener {
            val action = SingoloCantFragmentDirections.actionDettaglioCantToSelezionaUtenti(
                cantiereId = args.cantiereId,
                mode = UtenteMode.OPERAIO.name // "OPERAIO"
            )
            findNavController().navigate(action)
        }

        val args = SingoloCantFragmentArgs.fromBundle(requireArguments())
        viewModel.carica(args.cantiereId)
        viewModel.caricaFasi(args.cantiereId)

    }
}