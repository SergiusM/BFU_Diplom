package com.example.test_for_diplom

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.test_for_diplom.databinding.ActivityFragBinding

class Activity_Frag : AppCompatActivity() {
    private lateinit var binding: ActivityFragBinding
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFragBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. Установите Toolbar как ActionBar
        setSupportActionBar(binding.toolbar)

        // 2. Инициализация NavController
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_content_activity_frag) as NavHostFragment
        navController = navHostFragment.navController

        // 3. Настройка AppBarConfiguration
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.scheduleFragment,
                R.id.aiChatFragment,
                R.id.materialsFragment,
                R.id.profileFragment
            ),
            binding.drawerLayout
        )

        // 4. Свяжите ActionBar с NavController
        setupActionBarWithNavController(navController, appBarConfiguration)

        // 5. Настройка NavigationView
        binding.navView.setupWithNavController(navController)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}