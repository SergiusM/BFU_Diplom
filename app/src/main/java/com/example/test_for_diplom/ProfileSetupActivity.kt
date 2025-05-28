
@file:OptIn(InternalSerializationApi::class)

package com.example.test_for_diplom

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.test_for_diplom.databinding.ActivityProfileSetupBinding
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.launch
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable

class ProfileSetupActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileSetupBinding
    private lateinit var studyFieldDropdown: AutoCompleteTextView
    private var programList: List<Program> = emptyList()
    private var selectedProgram: Program? = null

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

        studyFieldDropdown = binding.studyFieldSpinner
        setupStudyFieldDropdown()

        binding.saveButton.setOnClickListener {
            saveProfileData()
        }
    }

    private fun setupStudyFieldDropdown() {
        lifecycleScope.launch {
            binding.progressBar.visibility = View.VISIBLE
            try {
                programList = Supabase.client.from("programs").select().decodeList()

                val adapter = object : ArrayAdapter<Program>(
                    this@ProfileSetupActivity,
                    R.layout.dropdown_item_two_lines,
                    programList
                ) {
                    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                        val view = convertView ?: layoutInflater.inflate(R.layout.dropdown_item_two_lines, parent, false)
                        val nameText = view.findViewById<TextView>(R.id.textName)
                        val codeText = view.findViewById<TextView>(R.id.textCode)
                        val program = getItem(position)

                        nameText.text = program?.name
                        codeText.text = program?.code
                        return view
                    }

                    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                        return getView(position, convertView, parent)
                    }

                    override fun toString(): String {
                        // По умолчанию ArrayAdapter вызывает toString() объекта для установки текста
                        // Но здесь мы управляем этим вручную, поэтому не переопределяем toString
                        return super.toString()
                    }
                }

                studyFieldDropdown.setAdapter(adapter)

                studyFieldDropdown.setOnItemClickListener { _, _, position, _ ->
                    selectedProgram = programList[position]
                    selectedProgram?.let {
                        studyFieldDropdown.setText(it.name, false)
                    }
                }

            } catch (e: Exception) {
                Toast.makeText(this@ProfileSetupActivity, "Ошибка загрузки направлений", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun saveProfileData() {
        val fullName = binding.fullNameEditText.text.toString().trim()
        val course = binding.courseEditText.text.toString().trim()
        val selected = selectedProgram

        if (fullName.isEmpty()  || course.isEmpty() || selected == null) {
            Toast.makeText(this, "Пожалуйста, заполните все поля", Toast.LENGTH_SHORT).show()
            return
        }

        val courseNumber = course.toIntOrNull()
        if (courseNumber == null || courseNumber !in 1..6) {
            Toast.makeText(this, "Курс должен быть числом от 1 до 6", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            binding.progressBar.visibility = View.VISIBLE
            try {
                val user = Supabase.client.auth.currentUserOrNull()
                if (user == null) {
                    Toast.makeText(this@ProfileSetupActivity, "Пользователь не найден", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@ProfileSetupActivity, LoginActivity::class.java))
                    finish()
                    return@launch
                }


                val userProfile = User(
                    id = user.id,
                    email = user.email ?: "",
                    fullname = fullName,
                    program_id = selected.id,
                    course = course,
                    profileCompleted = true
                )

                Supabase.client.from("users").upsert(userProfile)
                Toast.makeText(this@ProfileSetupActivity, "Профиль сохранён", Toast.LENGTH_SHORT).show()

                startActivity(Intent(this@ProfileSetupActivity, Activity_Frag::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                })
                finish()
            } catch (e: Exception) {
                Toast.makeText(this@ProfileSetupActivity, "Ошибка сохранения: ${e.message}", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }
}

