package com.example.test_for_diplom

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.test_for_diplom.databinding.ActivityProfileSetupBinding
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


class ProfileSetupActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileSetupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityProfileSetupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        setupStudyFieldSpinner()

        binding.saveButton.setOnClickListener {
            saveProfileData()
        }
    }

    private fun setupStudyFieldSpinner() {
        val studyFields = arrayOf(
            "Прикладная математика и информатика",
            "Информационные системы и технологии",
            "Информационная безопасность",
            "Математическое обеспечение"
        )

        val adapter = ArrayAdapter(
            this,
            R.layout.dropdown_menu_popup_item, // Используйте кастомный layout для выпадающего меню
            studyFields
        )
        binding.studyFieldSpinner.setAdapter(adapter)
    }

    private fun saveProfileData() {
        val fullName = binding.fullNameEditText.text.toString().trim()
        val studyField = binding.studyFieldSpinner.text.toString().trim()
        val course = binding.courseEditText.text.toString().trim()

        if (fullName.isEmpty() || studyField.isEmpty() || course.isEmpty()) {
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show()
            return
        }

        val courseNumber = course.toIntOrNull()
        if (courseNumber == null) {
            Toast.makeText(this, "Курс должен быть числом", Toast.LENGTH_SHORT).show()
            return
        }
        if (courseNumber !in 1..6) {
            Toast.makeText(this, "Курс должен быть от 1 до 6", Toast.LENGTH_SHORT).show()
            return
        }

        binding.progressBar.visibility = View.VISIBLE // Показать индикатор
        lifecycleScope.launch {
            try {
                val user = Supabase.client.auth.currentUserOrNull()
                if (user == null) {
                    Toast.makeText(this@ProfileSetupActivity, "Пользователь не аутентифицирован", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@ProfileSetupActivity, LoginActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    })
                    finish()
                    return@launch
                }

                val userId = user.id
                val email = user.email ?: ""

                val userProfile = User(
                    id = userId,
                    email = email,
                    fullname = fullName,
                    studyfield = studyField,
                    course = course,
                    profileCompleted = true
                )

                Supabase.client.from("users").upsert(userProfile)
                Toast.makeText(this@ProfileSetupActivity, "Профиль успешно сохранен", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this@ProfileSetupActivity, Activity_Frag::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                })
                finish()
            } catch (e: Exception) {
                Toast.makeText(this@ProfileSetupActivity, "Ошибка сохранения: ${e.message}", Toast.LENGTH_SHORT).show()
                println("Ошибка сохранения профиля: ${e.message}")
                e.printStackTrace()
            } finally {
                binding.progressBar.visibility = View.GONE // Скрыть индикатор
            }
        }
    }
}