package com.partyhub

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        
        // Los destinos de primer nivel no muestran el botón "atrás" en la Toolbar
        val appBarConfiguration = AppBarConfiguration(
            setOf(R.id.hubFragment, R.id.historyFragment, R.id.settingsFragment)
        )
        
        toolbar.setupWithNavController(navController, appBarConfiguration)
        
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav)
        bottomNav.setupWithNavController(navController)

        // Ocultar BottomNav cuando entramos en un juego para tener más espacio
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.hubFragment, R.id.historyFragment, R.id.settingsFragment -> {
                    bottomNav.visibility = View.VISIBLE
                }
                else -> {
                    bottomNav.visibility = View.GONE
                }
            }
        }
    }
}
