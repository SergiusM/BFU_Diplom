package com.example.test_for_diplom

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.navigation.NavigationView
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.ActionBarDrawerToggle

class Activity_Frag : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_frag)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Инициализация DrawerLayout
        drawerLayout = findViewById(R.id.drawer_layout)

        // Получаем NavHostFragment
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_content_activity_frag) as NavHostFragment
        val navController = navHostFragment.navController

        val navView = findViewById<NavigationView>(R.id.nav_view)

        // Настройка AppBarConfiguration с DrawerLayout
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.scheduleFragment,
                R.id.aiChatFragment,
                R.id.materialsFragment,
                R.id.profileFragment
            ),
            drawerLayout
        )

        // Связывание toolbar с NavController
        setupActionBarWithNavController(navController, appBarConfiguration)

        // Связывание NavigationView с NavController
        navView.setupWithNavController(navController)

        // Настройка ActionBarDrawerToggle
        toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
    }

    override fun onSupportNavigateUp(): Boolean {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_content_activity_frag) as NavHostFragment
        val navController = navHostFragment.navController
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}