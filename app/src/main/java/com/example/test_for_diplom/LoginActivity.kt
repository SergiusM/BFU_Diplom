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
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Автозаполнение email, если remember_me включено
        val sharedPref = getSharedPreferences("login_prefs", MODE_PRIVATE)
        if (sharedPref.getBoolean("remember_me", false)) {
            val savedEmail = sharedPref.getString("saved_email", null)
            if (savedEmail != null) {
                binding.emailEditText.setText(savedEmail)
            }
        }

        setupUI()
    }

    override fun onStart() {
        super.onStart()
        val sharedPref = getSharedPreferences("login_prefs", MODE_PRIVATE)
        if (sharedPref.getBoolean("remember_me", false) && Supabase.client.auth.currentUserOrNull() != null) {
            navigateToMain()
        }
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
                handleRememberMe(rememberMe, email)
                navigateToMain()
            } catch (e: Exception) {
                handleLoginError(e)
            }
        }
    }

    private fun navigateToMain() {
        startActivity(Intent(this, Activity_Frag::class.java).apply {
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

    private fun handleRememberMe(rememberMe: Boolean, email: String) {
        val editor = getSharedPreferences("login_prefs", MODE_PRIVATE).edit()
        editor.putBoolean("remember_me", rememberMe)
        if (rememberMe) {
            editor.putString("saved_email", email)
        } else {
            editor.remove("saved_email")
        }
        editor.apply()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}