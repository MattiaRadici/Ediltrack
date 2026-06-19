package com.example.ediltrack.ui.view.activity

import androidx.appcompat.app.AppCompatActivity
import com.example.ediltrack.ui.viewmodel.AuthViewModel
import com.example.ediltrack.R
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.ediltrack.databinding.LoginBinding
import android.content.Intent
import android.widget.Toast

class LoginActivity : AppCompatActivity() {
    lateinit var authViewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        authViewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        )[AuthViewModel::class.java]

        // Ottieni il binding
        val binding: LoginBinding = DataBindingUtil.setContentView(
            this, R.layout.login
        )

        // Collega il ViewModel al layout
        binding.viewModel = authViewModel
        binding.lifecycleOwner = this

        //qui gestisco la navigazione alla prossima activity nel caso il messaggio sia di login corretto
        authViewModel.mess.observe(this,{message -> message?.let {

            Toast.makeText(this, message , Toast.LENGTH_SHORT).show()

            if (message == "Autenticazione riuscita") {
                val intent = Intent(this, HomeActivity::class.java)
                //intent.putExtras("utente" : "ciao")
                startActivity(intent)
                finish()
            }
            }
        })

    }

}