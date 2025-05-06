package com.example.test_for_diplom

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.test_for_diplom.databinding.ActivityMainBinding
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.FirebaseDatabase

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        setupRegistration()
        setupLoginNavigation() // Добавляем обработчик для перехода на логин
    }

    // Новый метод для настройки перехода на экран входа
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

    // Остальной код без изменений
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
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    auth.currentUser?.let { user ->
                        user.sendEmailVerification()
                            .addOnCompleteListener { verificationTask ->
                                if (verificationTask.isSuccessful) {
                                    updateUserProfile(user.uid, user.email ?: "")
                                } else {
                                    showToast("Ошибка отправки подтверждения: ${verificationTask.exception?.message}")
                                    navigateToLogin() //+
                                // Переход даже при ошибке верификации
                                }
                            }
                    }
                } else {
                    showToast("Ошибка: ${task.exception?.message}")
                }
            }
    }

    private fun updateUserProfile(userId: String, email: String) {
        val profileUpdates = UserProfileChangeRequest.Builder().build()
        auth.currentUser?.updateProfile(profileUpdates)
            ?.addOnCompleteListener {
                saveUserToDatabase(userId, email)
            }?.addOnFailureListener {
                showToast("Ошибка профиля: ${it.message}")
            }?.addOnSuccessListener {
                navigateToActivityFrag() // Изменено на переход в Activity_Frag
            }
    }

    // Новый метод для перехода в основное Activity
    private fun navigateToActivityFrag() {
        startActivity(Intent(this, Activity_Frag::class.java))
        finish()
    }

    // Старый метод для перехода к логину (используется только для кнопки)
    private fun navigateToLogin() {
        auth.signOut()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private fun saveUserToDatabase(userId: String, email: String) {
        val user = User(email, System.currentTimeMillis())
        database.reference.child("users").child(userId).setValue(user)
            .addOnFailureListener {
                showToast("Ошибка базы данных: ${it.message}")
            }
    }

    private fun showError(field: android.widget.EditText, message: String) {
        field.error = message
        field.requestFocus()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    data class User(
        val email: String = "",
        val createdAt: Long = 0
    )
}