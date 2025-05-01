package com.example.test_for_diplom

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.google.firebase.auth.FirebaseAuth

class SplashActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var progressBar: ProgressBar
    private lateinit var sharedPref: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        auth = FirebaseAuth.getInstance()
        sharedPref = getSharedPreferences("login_prefs", MODE_PRIVATE)
        progressBar = findViewById(R.id.progressBar)

        setupLoadingAnimation()
        checkAuthState()
    }

    private fun setupLoadingAnimation() {
        progressBar.isVisible = true
        progressBar.isIndeterminate = true
    }

    private fun checkAuthState() {
        val user = auth.currentUser
        val rememberMe = sharedPref.getBoolean("remember_me", false)

        when {
            // Пользователь авторизован и включен автовход
            user != null && rememberMe -> {
                navigateToMain()
            }

            // Пользователь авторизован, но выключен автовход
            user != null -> {
                navigateToLogin()
            }

            // Новый пользователь
            else -> {
                navigateToRegistration()
            }
        }
        finish()
    }

    private fun navigateToMain() {
        startActivity(Intent(this, Activity_one::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
    }

    private fun navigateToLogin() {
        startActivity(Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
    }

    private fun navigateToRegistration() {
        startActivity(Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
    }
}