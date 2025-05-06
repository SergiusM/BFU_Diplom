package com.example.test_for_diplom

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class SplashActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var progressBar: ProgressBar
    private lateinit var sharedPref: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
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
                checkProfileCompletion(user.uid)
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
    }

    private fun checkProfileCompletion(userId: String) {
        database.reference.child("users").child(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val profileCompleted = snapshot.child("profileCompleted").getValue(Boolean::class.java) ?: false

                    if (profileCompleted) {
                        navigateToMain()
                    } else {
                        navigateToProfileSetup()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        this@SplashActivity,
                        "Ошибка загрузки данных профиля",
                        Toast.LENGTH_SHORT
                    ).show()
                    navigateToLogin()
                }
            })
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
        startActivity(Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }

    private fun navigateToProfileSetup() {
        startActivity(Intent(this, ProfileSetupActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }
}