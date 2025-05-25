package com.example.test_for_diplom

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.test_for_diplom.databinding.ActivityRegisterBinding
import com.google.android.material.textfield.TextInputLayout
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRegistration()
        setupLoginNavigation()
    }

    private fun setupLoginNavigation() {
        binding.loginExistingAccountButton.setOnClickListener {
            navigateToLogin()
        }
    }

    private fun setupRegistration() {
        binding.registerButton.setOnClickListener {
            validateAndRegister()
        }
    }

    private fun validateAndRegister() {
        val email = binding.emailEditText.text.toString().trim()
        val password = binding.passwordEditText.text.toString().trim()
        val repeatPassword = binding.repeatPasswordEditText.text.toString().trim()

        when {
            email.isEmpty() -> showError(binding.email, "Введите email")
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> showError(binding.email, "Некорректный email")
            password.isEmpty() -> showError(binding.password, "Введите пароль")
            password.length < 6 -> showError(binding.password, "Минимум 6 символов")
            repeatPassword.isEmpty() -> showError(binding.repeatPassword, "Повторите пароль")
            password != repeatPassword -> showError(binding.repeatPassword, "Пароли не совпадают")
            else -> performRegistration(email, password)
        }
    }

    private fun showError(field: TextInputLayout, message: String) {
        field.error = message
        field.requestFocus()
    }

    private fun performRegistration(email: String, password: String) {
        lifecycleScope.launch {
            try {
                Supabase.client.auth.signUpWith(Email) {
                    this.email = email
                    this.password = password
                }
                val userId = Supabase.client.auth.currentUserOrNull()?.id
                if (userId != null) {
                    saveUserToSupabase(userId, email)
                    navigateToProfileSetup() // Перенаправляем на ProfileSetupActivity после успешной регистрации
                } else {
                    showToast("Пожалуйста, подтвердите ваш email")
                    navigateToLogin()
                }
            } catch (e: Exception) {
                showToast("Ошибка регистрации: ${e.message}")
                e.printStackTrace()
                navigateToLogin()
            }
        }
    }

    private suspend fun saveUserToSupabase(userId: String, email: String) {
        try {
            println("Сохранение пользователя: userId=$userId, email=$email")
            Supabase.client.from("users").insert(
                User(
                    id = userId,
                    email = email
                )
            )
            println("Данные успешно сохранены")
        } catch (e: Exception) {
            showToast("Ошибка сохранения профиля: ${e.message}")
            e.printStackTrace()
            throw e // Пробрасываем исключение, чтобы обработать ошибку в performRegistration
        }
    }

    private fun navigateToProfileSetup() {
        startActivity(Intent(this, ProfileSetupActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }

    private fun navigateToLogin() {
        lifecycleScope.launch {
            try {
                Supabase.client.auth.signOut()
            } catch (e: Exception) {
                // Игнорируем ошибки выхода
            }
            startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
            finish()
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    @Serializable
    data class User(
        val id: String,
        val email: String
    )
}