package com.example.ediltrack.ui.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.ediltrack.databinding.FragmentDipendentiBinding
import com.example.ediltrack.ui.viewmodel.DipendentiViewModel

class UtentiFragment :Fragment() {

    lateinit var binding: FragmentDipendentiBinding
    lateinit var viewModel: DipendentiViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[DipendentiViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentDipendentiBinding.inflate(inflater, container, false)
        return binding.root
    }

}