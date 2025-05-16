package com.example.test_for_diplom

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ProfileFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    private lateinit var fullNameTextView: TextView
    private lateinit var studyFieldTextView: TextView
    private lateinit var courseTextView: TextView
    private lateinit var logoutButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Инициализация Firebase
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        // Привязка UI элементов
        fullNameTextView = view.findViewById(R.id.fullNameTextView)
        studyFieldTextView = view.findViewById(R.id.studyFieldTextView)
        courseTextView = view.findViewById(R.id.courseTextView)
        logoutButton = view.findViewById(R.id.logout_button)

        // Загрузка данных студента
        loadStudentData()

        // Обработка нажатия на кнопку "Выйти из профиля"
        logoutButton.setOnClickListener {
            auth.signOut()
            startActivity(Intent(requireContext(), LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            requireActivity().finish()
        }
    }

    private fun loadStudentData() {
        val userId = auth.currentUser?.uid ?: return
        val userRef = database.child("users").child(userId)

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val fullName = snapshot.child("fullName").getValue(String::class.java) ?: "Не указано"
                    val studyField = snapshot.child("studyField").getValue(String::class.java) ?: "Не указано"
                    val course = snapshot.child("course").getValue(String::class.java) ?: "Не указано"

                    // Обновление UI
                    fullNameTextView.text = "ФИО: $fullName"
                    studyFieldTextView.text = "Направление: $studyField"
                    courseTextView.text = "Курс: $course"
                } else {
                    Toast.makeText(requireContext(), "Данные профиля не найдены", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Ошибка загрузки данных: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}