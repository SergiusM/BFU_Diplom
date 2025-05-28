package com.example.test_for_diplom

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.example.test_for_diplom.databinding.ActivitySplashBinding
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private lateinit var sharedPref: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPref = getSharedPreferences("login_prefs", MODE_PRIVATE)
        setupLoadingAnimation()
        checkAuthState()
    }

    private fun setupLoadingAnimation() {
        binding.progressBar.isVisible = true
        binding.progressBar.isIndeterminate = true
    }

    private fun checkAuthState() {
        lifecycleScope.launch {
            try {
                val user = Supabase.client.auth.currentUserOrNull()
                val rememberMe = sharedPref.getBoolean("remember_me", false)

                println("SplashActivity: user=${user?.id}, remember_me=$rememberMe")

                when {
                    user != null -> {
                        // Пользователь авторизован, перенаправляем в Activity_Frag
                        navigateToMain()
                    }
                    rememberMe -> {
                        // Сессия истекла, но remember_me включено, перенаправляем в LoginActivity
                        navigateToLogin()
                    }
                    else -> {
                        // Пользователь не авторизован, перенаправляем в RegisterActivity
                        navigateToRegistration()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@SplashActivity,
                    "Ошибка проверки авторизации: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
                println("SplashActivity: ошибка проверки авторизации: ${e.message}")
                navigateToRegistration()
            }
        }
    }

    private fun navigateToMain() {
        startActivity(Intent(this, Activity_Frag::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }

    private fun navigateToLogin() {
        startActivity(Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }

    private fun navigateToRegistration() {
        startActivity(Intent(this, RegisterActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }
}