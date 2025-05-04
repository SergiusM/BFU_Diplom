package com.example.test_for_diplom

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import com.google.android.material.textfield.TextInputEditText
import com.example.test_for_diplom.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        setupUI()
    }

    private fun setupUI() {
        binding.loginButton.setOnClickListener {
            // Правильные ID элементов
            val email = binding.emailEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString().trim()
            val rememberMe = binding.rememberMe.isChecked

            if (validateInput(email, password)) {
                loginUser(email, password, rememberMe)
            }
        }

        binding.registerLink.setOnClickListener {
            // Исправлено: добавлена закрывающая скобка для apply
            startActivity(Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }) // Добавлена недостающая скобка
        } // Исправлено: закрытие блока setOnClickListener
    }

    private fun validateInput(email: String, password: String): Boolean {
        return when {
            email.isEmpty() -> {
                binding.emailEditText.error = "Введите email"
                false
            }
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                binding.emailEditText.error = "Неверный формат email"
                false
            }
            password.isEmpty() -> {
                binding.passwordEditText.error = "Введите пароль"
                false
            }
            password.length < 6 -> {
                binding.passwordEditText.error = "Пароль должен содержать минимум 6 символов"
                false
            }
            else -> true
        }
    }

    private fun loginUser(email: String, password: String, rememberMe: Boolean) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    handleRememberMe(rememberMe)
                    startActivity(Intent(this, Activity_Frag::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    })
                    finish()
                } else {
                    handleLoginError(task.exception)
                }
            }
    }

    private fun handleLoginError(exception: Exception?) {
        val errorMessage = when (exception) {
            is FirebaseAuthInvalidUserException -> "Пользователь не найден"
            is FirebaseAuthInvalidCredentialsException -> "Неверный пароль"
            else -> "Ошибка авторизации: ${exception?.message ?: "Неизвестная ошибка"}"
        }
        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
    }

    private fun handleRememberMe(rememberMe: Boolean) {
        // Оптимизировано использование SharedPreferences
        getSharedPreferences("login_prefs", MODE_PRIVATE).edit()
            .putBoolean("remember_me", rememberMe)
            .apply()
    }

    override fun onStart() {
        super.onStart()
        val sharedPref = getSharedPreferences("login_prefs", MODE_PRIVATE)
        if (sharedPref.getBoolean("remember_me", false) && auth.currentUser != null) {
            startActivity(Intent(this, Activity_Frag::class.java))
            finish()
        }
    }
}