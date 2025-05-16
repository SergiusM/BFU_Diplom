package com.example.test_for_diplom

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.test_for_diplom.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseAuth.getInstance().setLanguageCode("ru") // Установка локали
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        database = FirebaseDatabase.getInstance()
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
            startActivity(Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            })
        }
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
                    checkProfileCompletion()
                } else {
                    handleLoginError(task.exception)
                }
            }
    }

    private fun checkProfileCompletion() {
        val userId = auth.currentUser?.uid ?: run {
            Toast.makeText(this, "Ошибка: пользователь не авторизован", Toast.LENGTH_SHORT).show()
            return
        }

        val userRef = database.reference.child("users").child(userId)
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("LoginDebug", "Данные профиля: ${snapshot.value}")

                if (!snapshot.exists()) {
                    createUserProfile(userId) // Создаем профиль, если его нет
                    return
                }

                val profileCompleted = snapshot.child("profileCompleted").getValue(Boolean::class.java) ?: false
                navigateToActivity(
                    if (profileCompleted) Activity_Frag::class.java
                    else ProfileSetupActivity::class.java
                )
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@LoginActivity,
                    "Ошибка чтения данных: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
                navigateToActivity(Activity_Frag::class.java)
            }
        })
    }

    private fun createUserProfile(userId: String) {
        database.reference.child("users").child(userId).child("profileCompleted").setValue(false)
            .addOnSuccessListener {
                navigateToActivity(ProfileSetupActivity::class.java)
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this@LoginActivity,
                    "Ошибка создания профиля: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
                navigateToActivity(Activity_Frag::class.java)
            }
    }

    private fun navigateToActivity(activityClass: Class<*>) {
        startActivity(Intent(this, activityClass).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
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
        getSharedPreferences("login_prefs", MODE_PRIVATE).edit()
            .putBoolean("remember_me", rememberMe)
            .apply()
    }

    override fun onStart() {
        super.onStart()
        val sharedPref = getSharedPreferences("login_prefs", MODE_PRIVATE)
        if (sharedPref.getBoolean("remember_me", false) && auth.currentUser != null) {
            navigateToActivity(Activity_Frag::class.java)
        }
    }
}