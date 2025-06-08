
@file:OptIn(InternalSerializationApi::class) // Если User класс тоже использует InternalSerializationApi
package com.example.test_for_diplom

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.test_for_diplom.databinding.FragmentScheduleBinding // Используйте правильное имя вашего биндинг-класса
import com.google.android.material.card.MaterialCardView
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.launch
import android.widget.ArrayAdapter
import android.widget.AdapterView
import java.util.Calendar
import kotlinx.serialization.InternalSerializationApi // Если User класс тоже использует InternalSerializationApi


class ScheduleFragment : Fragment() {

    private var _binding: FragmentScheduleBinding? = null
    private val binding get() = _binding!!

    private val sessionManager by lazy { SessionManager(requireContext()) }

    private val daysOfWeek = listOf("Понедельник", "Вторник", "Среда", "Четверг", "Пятница", "Суббота", "Воскресенье")

    private var allScheduleItems: List<Schedule> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScheduleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupDaySpinner()
        loadSchedule()
    }

    private fun setupDaySpinner() {
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, daysOfWeek)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.daySpinner.adapter = adapter

        val calendar = Calendar.getInstance()
        val todayIndex = (calendar.get(Calendar.DAY_OF_WEEK) + 5) % 7 // Пн=0
        binding.daySpinner.setSelection(todayIndex)

        binding.daySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {
                val selectedDay = daysOfWeek[position]
                displaySchedule(allScheduleItems, selectedDay)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun loadSchedule() {
        lifecycleScope.launch {
            try {
                val userId = sessionManager.getUserId()
                if (userId == null) {
                    Toast.makeText(requireContext(), "Пользователь не найден", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val user = Supabase.client.from("users")
                    .select()
                    .decodeList<User>()
                    .find { it.id == userId }

                if (user == null) {
                    Toast.makeText(requireContext(), "Профиль пользователя не найден", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val userCourse = user.course?.toIntOrNull()
                val userProgramId = user.program_id

                if (userCourse == null) {
                    Toast.makeText(requireContext(), "Курс пользователя не указан", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val allEntries = Supabase.client.from("schedule")
                    .select()
                    .decodeList<Schedule>()

                allScheduleItems = allEntries.filter {
                    it.course == userCourse && it.program_id == userProgramId
                }

                // Показать текущий выбранный день
                val selectedDay = daysOfWeek[binding.daySpinner.selectedItemPosition]
                displaySchedule(allScheduleItems, selectedDay)

            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Ошибка загрузки расписания: ${e.message}", Toast.LENGTH_LONG).show()
                e.printStackTrace()
            }
        }
    }

    private fun displaySchedule(scheduleItems: List<Schedule>, selectedDay: String) {
        val context = requireContext()
        binding.scheduleContainer.removeAllViews()

        val filteredItems = scheduleItems.filter { it.day == selectedDay }


        if (filteredItems.isEmpty()) {
            val noScheduleText = TextView(context).apply {
                text = " $selectedDay выходной ."
                textSize = 18f
                setTextColor(Color.GRAY)
                gravity = Gravity.CENTER
                setPadding(16, 16, 16, 16)
            }
            binding.scheduleContainer.addView(noScheduleText)
            return
        }

        val sortedItems = filteredItems.sortedBy { it.time_slot }

        for (item in sortedItems) {
            val cardView = MaterialCardView(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(8, 8, 8, 16)
                }
                radius = 24f
                elevation = 8f
                setCardBackgroundColor(ContextCompat.getColor(context, R.color.white))
            }

            val itemLayout = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(24, 24, 24, 24)
            }

            val timeSlotText = TextView(context).apply {
                text = item.time_slot
                textSize = 16f
                setTypeface(null, Typeface.BOLD)
                setTextColor(Color.DKGRAY)
            }
            itemLayout.addView(timeSlotText)

            val subjectText = TextView(context).apply {
                text = item.subject
                textSize = 18f
                setTextColor(Color.BLACK)
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = 8
                }
            }
            itemLayout.addView(subjectText)

            if (item.information.isNotBlank()) {
                val infoText = TextView(context).apply {
                    text = item.information
                    textSize = 14f
                    setTextColor(Color.GRAY)
                    layoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    ).apply {
                        topMargin = 8
                    }
                }
                itemLayout.addView(infoText)
            }

            cardView.addView(itemLayout)
            binding.scheduleContainer.addView(cardView)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}