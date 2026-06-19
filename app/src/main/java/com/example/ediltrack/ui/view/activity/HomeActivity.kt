package com.example.ediltrack.ui.view.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.ediltrack.ui.viewmodel.HomeViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.ediltrack.R
import com.example.ediltrack.databinding.LayoutHomeGestioneBinding
import com.example.ediltrack.databinding.LayoutHomeOperatoreBinding
import com.example.ediltrack.util.UserRole


class HomeActivity: AppCompatActivity()  {
        private lateinit var homeviewModel: HomeViewModel
        private lateinit var bindingGestione : LayoutHomeGestioneBinding
        private lateinit var bindingUtente : LayoutHomeOperatoreBinding
        private var layoutInitialized = false

        override fun onCreate(savedInstanceState: Bundle?) {
                super.onCreate(savedInstanceState)
                homeviewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(application))[HomeViewModel :: class.java]
                homeviewModel.loadRole()
                //inizializzi la schermata home in base a utente o azienda
                homeviewModel.ruolo.observe(this){
                        ruolo ->
                        //Log.e("ruolo1",ruolo.toString())
                        when(ruolo.code){
                                UserRole.ADMIN.code, UserRole.CAPOCANTIERE.code-> {
                                        bindingGestione = LayoutHomeGestioneBinding.inflate(layoutInflater)
                                        setContentView(bindingGestione.root)
                                        //imposti la bottomNav bar
                                        val navController = supportFragmentManager
                                                .findFragmentById(R.id.container_frag) as NavHostFragment
                                        val controller = navController.navController

                                        // Collega la BottomNavigationView con il NavController
                                        bindingGestione.navView.setupWithNavController(controller)
                                }

                                UserRole.OPERAIO.code->{
                                        //TODO(mettere nella relazione che qui senza questo controllo si bugga)
                                        if(layoutInitialized) return@observe
                                        else layoutInitialized = true
                                        //Log.e("ruolo2",ruolo.toString())
                                        bindingUtente = LayoutHomeOperatoreBinding.inflate(layoutInflater)
                                        setContentView(bindingUtente.root)
                                        //impostiamo la bottom
                                        val navController = supportFragmentManager.findFragmentById(R.id.container_frag_dip) as NavHostFragment
                                        val controller = navController.navController
                                        bindingUtente.navView.setupWithNavController(controller)
                                }



                        }
                }

        }

}