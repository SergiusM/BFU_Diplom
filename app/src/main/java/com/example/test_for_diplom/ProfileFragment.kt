package com.example.test_for_diplom

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.test_for_diplom.databinding.FragmentProfileBinding
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    // Список направлений
    private val studyFields = arrayOf(
        "Прикладная математика и информатика",
        "Информационные системы и технологии",
        "Информационая безопасность",
        "Математическое обеспечение"
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Настройка Spinner для направлений
        setupStudyFieldSpinner()

        // Загрузка данных профиля
        loadStudentData()

        // Обработка нажатия на кнопку "Сохранить изменения"
        binding.saveButton.setOnClickListener {
            saveProfileChanges()
        }

        // Обработка нажатия на кнопку "Выйти из профиля"
        binding.logoutButton.setOnClickListener {
            lifecycleScope.launch {
                try {
                    Supabase.client.auth.signOut()
                    startActivity(Intent(requireContext(), LoginActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    })
                    requireActivity().finish()
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Ошибка выхода: ${e.message}", Toast.LENGTH_SHORT).show()
                    println("Ошибка выхода: ${e.message}")
                    e.printStackTrace()
                }
            }
        }
    }

    /** Настройка Spinner для выбора направления */
    private fun setupStudyFieldSpinner() {
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, studyFields)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.studyFieldSpinner.adapter = adapter
    }

    /** Загрузка данных студента */
    private fun loadStudentData() {
        lifecycleScope.launch {
            try {
                val user = Supabase.client.auth.currentUserOrNull()
                if (user == null) {
                    Toast.makeText(requireContext(), "Пользователь не аутентифицирован", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(requireContext(), LoginActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    })
                    requireActivity().finish()
                    return@launch
                }

                val userId = user.id
                val userProfile = Supabase.client.from("users")
                    .select {
                        filter { eq("id", userId) }
                    }
                    .decodeSingleOrNull<User>()

                if (userProfile != null) {
                    // Установка данных в EditText и Spinner
                    binding.fullNameEditText.setText(userProfile.fullname)
                    binding.courseEditText.setText(userProfile.course)

                    // Установка текущего направления в Spinner
                    val studyFieldIndex = studyFields.indexOf(userProfile.studyfield)
                    if (studyFieldIndex != -1) {
                        binding.studyFieldSpinner.setSelection(studyFieldIndex)
                    } else {
                        Toast.makeText(requireContext(), "Текущее направление не в списке", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "Данные профиля не найдены", Toast.LENGTH_SHORT).show()
                    println("Профиль не найден для userId: $userId")
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Ошибка загрузки данных: ${e.message}", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }

    /** Сохранение изменений профиля */
    private fun saveProfileChanges() {
        val fullName = binding.fullNameEditText.text.toString().trim()
        val studyField = binding.studyFieldSpinner.selectedItem?.toString()
        val courseText = binding.courseEditText.text.toString().trim()

        // Проверка на пустые поля
        if (fullName.isEmpty() || studyField.isNullOrEmpty() || courseText.isEmpty()) {
            Toast.makeText(requireContext(), "Все поля должны быть заполнены", Toast.LENGTH_SHORT).show()
            return
        }

        // Валидация курса
        val course = courseText.toIntOrNull()
        if (course == null || course !in 1..6) {
            Toast.makeText(requireContext(), "Некорректный курс (должен быть 1-6)", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val user = Supabase.client.auth.currentUserOrNull()
                if (user == null) {
                    Toast.makeText(requireContext(), "Пользователь не аутентифицирован", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                // Получаем текущие данные профиля
                val currentProfile = Supabase.client.from("users")
                    .select {
                        filter { eq("id", user.id) }
                    }
                    .decodeSingle<User>()

                // Обновляем профиль
                val updatedProfile = currentProfile.copy(
                    fullname = fullName,
                    studyfield = studyField,
                    course = course.toString()
                )

                Supabase.client.from("users").update(updatedProfile) {
                    filter { eq("id", user.id) }
                }

                Toast.makeText(requireContext(), "Профиль успешно обновлен", Toast.LENGTH_SHORT).show()
                loadStudentData() // Обновляем данные на экране
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Ошибка обновления: ${e.message}", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}