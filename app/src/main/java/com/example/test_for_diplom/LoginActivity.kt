package com.example.test_for_diplom

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.test_for_diplom.databinding.ActivityLoginBinding
import com.google.android.material.textfield.TextInputLayout
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
    }

    private fun setupUI() {
        binding.loginButton.setOnClickListener {
            val email = binding.emailEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString().trim()
            val rememberMe = binding.rememberMe.isChecked

            if (validateInput(email, password)) {
                loginUser(email, password, rememberMe)
            }
        }

        binding.registerLink.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            })
        }
    }

    private fun validateInput(email: String, password: String): Boolean {
        return when {
            email.isEmpty() -> {
                showError(binding.emailInputLayout, "Введите email")
                false
            }
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                showError(binding.emailInputLayout, "Неверный формат email")
                false
            }
            password.isEmpty() -> {
                showError(binding.passwordInputLayout, "Введите пароль")
                false
            }
            password.length < 6 -> {
                showError(binding.passwordInputLayout, "Пароль должен содержать минимум 6 символов")
                false
            }
            else -> {
                clearErrors()
                true
            }
        }
    }

    private fun showError(field: TextInputLayout, message: String) {
        field.error = message
        field.requestFocus()
    }

    private fun clearErrors() {
        binding.emailInputLayout.error = null
        binding.passwordInputLayout.error = null
    }

    private fun loginUser(email: String, password: String, rememberMe: Boolean) {
        lifecycleScope.launch {
            try {
                Supabase.client.auth.signInWith(Email) {
                    this.email = email
                    this.password = password
                }
                handleRememberMe(rememberMe)
                checkProfileCompletion()
            } catch (e: Exception) {
                handleLoginError(e)
            }
        }
    }

    private suspend fun checkProfileCompletion() {
        val userId = Supabase.client.auth.currentUserOrNull()?.id ?: run {
            showToast("Ошибка: пользователь не авторизован")
            return
        }

        try {
            val result = Supabase.client.from("users").select {
                filter { eq("id", userId) }
            }.decodeSingleOrNull<User>()

            if (result == null) {
                createUserProfile(userId)
            } else {
                val profileCompleted = result.profileCompleted ?: false
                navigateToActivity(
                    if (profileCompleted) Activity_Frag::class.java
                    else ProfileSetupActivity::class.java
                )
            }
        } catch (e: Exception) {
            showToast("Ошибка чтения данных: ${e.message}")
            println("Ошибка проверки профиля: ${e.message}")
            e.printStackTrace()
            // В случае ошибки перенаправляем на ProfileSetupActivity
            navigateToActivity(ProfileSetupActivity::class.java)
        }
    }

    private suspend fun createUserProfile(userId: String) {
        try {
            Supabase.client.from("users").insert(
                User(
                    id = userId,
                    email = Supabase.client.auth.currentUserOrNull()?.email ?: "",
                    profileCompleted = false
                )
            )
            navigateToActivity(ProfileSetupActivity::class.java)
        } catch (e: Exception) {
            showToast("Ошибка создания профиля: ${e.message}")
            println("Ошибка создания профиля: ${e.message}")
            e.printStackTrace()
            // В случае ошибки перенаправляем на ProfileSetupActivity
            navigateToActivity(ProfileSetupActivity::class.java)
        }
    }

    private fun navigateToActivity(activityClass: Class<*>) {
        startActivity(Intent(this, activityClass).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }

    private fun handleLoginError(exception: Exception) {
        val errorMessage = when (exception.message?.contains("Invalid login credentials", ignoreCase = true)) {
            true -> "Неверный email или пароль"
            else -> "Ошибка авторизации: ${exception.message ?: "Неизвестная ошибка"}"
        }
        showToast(errorMessage)
        println("Ошибка логина: $errorMessage")
        exception.printStackTrace()
    }

    private fun handleRememberMe(rememberMe: Boolean) {
        getSharedPreferences("login_prefs", MODE_PRIVATE).edit()
            .putBoolean("remember_me", rememberMe)
            .apply()
    }

    override fun onStart() {
        super.onStart()
        val sharedPref = getSharedPreferences("login_prefs", MODE_PRIVATE)
        if (sharedPref.getBoolean("remember_me", false) && Supabase.client.auth.currentUserOrNull() != null) {
            lifecycleScope.launch {
                checkProfileCompletion()
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    @Serializable
    data class User(
        val id: String,
        val email: String,
        val profileCompleted: Boolean? = null
    )
}