
@file:OptIn(InternalSerializationApi::class)

package com.example.test_for_diplom

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.test_for_diplom.databinding.FragmentProfileBinding
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.launch
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable

import android.graphics.drawable.GradientDrawable

import android.view.Gravity

import android.widget.Button
import android.widget.LinearLayout


class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private var programs: List<Program> = emptyList()

    @Serializable
    data class GroupLink(
        val id: Int,
        val link: String,
        val title: String,
        val course: Int? = null,
        val program_id: Int? = null
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

        loadProgramsAndProfile()

        binding.saveButton.setOnClickListener {
            saveProfileChanges()
        }

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
                }
            }
        }
    }

    private fun loadProgramsAndProfile() {
        lifecycleScope.launch {
            try {
                programs = Supabase.client.from("programs")
                    .select().decodeList<Program>()

                val adapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_spinner_item,
                    programs.map { it.name + "-" + it.code }
                )
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.studyFieldSpinner.adapter = adapter

                loadUserProfile()

            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Ошибка загрузки направлений: ${e.message}", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }

    private fun loadUserProfile() {
        lifecycleScope.launch {
            try {
                val user = Supabase.client.auth.currentUserOrNull()
                if (user == null) {
                    Toast.makeText(requireContext(), "Пользователь не аутентифицирован", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val userProfile = Supabase.client.from("users")
                    .select { filter { eq("id", user.id) } }
                    .decodeSingleOrNull<User>()

                if (userProfile != null) {
                    binding.fullNameEditText.setText(userProfile.fullname)
                    binding.courseEditText.setText(userProfile.course)

                    val selectedIndex = programs.indexOfFirst { it.id == userProfile.program_id }
                    if (selectedIndex != -1) {
                        binding.studyFieldSpinner.setSelection(selectedIndex)
                    }


                    val courseInt = userProfile.course.toIntOrNull()
                    loadGroupLinks(userProfile.program_id, courseInt)

                } else {
                    Toast.makeText(requireContext(), "Данные профиля не найдены", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Ошибка загрузки профиля: ${e.message}", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }

    private fun loadGroupLinks(programId: Int?, course: Int?) {
        lifecycleScope.launch {
            try {
                val allLinks = Supabase.client.from("group_links")
                    .select().decodeList<GroupLink>()

                val filteredLinks = allLinks.filter {
                    (it.program_id == null || it.program_id == programId) &&
                            (it.course == null || it.course == course)
                }

                binding.linksContainer.removeAllViews()

                for (link in filteredLinks) {
                    val button = Button(requireContext()).apply {
                        text = link.title
                        textSize = 16f
                        setTextColor(ContextCompat.getColor(context, android.R.color.black))
                        setBackgroundColor(ContextCompat.getColor(context, android.R.color.white))
                        setPadding(32, 16, 32, 16)
                        isAllCaps = false

                        // Скруглённые углы и тень через backgroundDrawable
                        background = GradientDrawable().apply {
                            shape = GradientDrawable.RECTANGLE
                            cornerRadius = 24f
                            setColor(ContextCompat.getColor(context, android.R.color.white))
                            setStroke(2, ContextCompat.getColor(context, android.R.color.darker_gray))
                        }

                        // Центровка и отступы
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        ).apply {
                            gravity = Gravity.CENTER
                            setMargins(16, 16, 16, 16)
                        }

                        setOnClickListener {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link.link))
                            startActivity(intent)
                        }
                    }

                    binding.linksContainer.addView(button)
                }

                if (filteredLinks.isEmpty()) {
                    val emptyText = TextView(requireContext()).apply {
                        text = "Ссылки на беседы отсутствуют"
                        textSize = 14f
                        setPadding(0, 8, 0, 8)
                    }
                    binding.linksContainer.addView(emptyText)
                }

            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Ошибка загрузки ссылок: ${e.message}", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }

    private fun saveProfileChanges() {
        val fullName = binding.fullNameEditText.text.toString().trim()
        val course = binding.courseEditText.text.toString().trim()

        if (fullName.isEmpty() || course.isEmpty()) {
            Toast.makeText(requireContext(), "Все поля должны быть заполнены", Toast.LENGTH_SHORT).show()
            return
        }

        val courseInt = course.toIntOrNull()
        if (courseInt == null || courseInt !in 1..6) {
            Toast.makeText(requireContext(), "Некорректный курс (1-6)", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedIndex = binding.studyFieldSpinner.selectedItemPosition
        if (selectedIndex == -1 || selectedIndex >= programs.size) {
            Toast.makeText(requireContext(), "Выберите направление", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedProgram = programs[selectedIndex]

        lifecycleScope.launch {
            try {
                val user = Supabase.client.auth.currentUserOrNull()
                if (user == null) {
                    Toast.makeText(requireContext(), "Пользователь не найден", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val updatedUser = User(
                    id = user.id,
                    email = user.email ?: "",
                    fullname = fullName,
                    program_id = selectedProgram.id,
                    course = course,
                    profileCompleted = true
                )

                Supabase.client.from("users").update(updatedUser) {
                    filter { eq("id", user.id) }
                }


                Toast.makeText(requireContext(), "Профиль обновлён", Toast.LENGTH_SHORT).show()
                loadUserProfile()

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