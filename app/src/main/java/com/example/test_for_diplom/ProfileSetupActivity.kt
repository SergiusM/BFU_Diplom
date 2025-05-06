package com.example.test_for_diplom

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.test_for_diplom.databinding.ActivityProfileSetupBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class ProfileSetupActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileSetupBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileSetupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        binding.saveButton.setOnClickListener {
            saveProfileData()
        }
    }

    private fun saveProfileData() {
        val fullName = binding.fullNameEditText.text.toString().trim()
        val studyField = binding.studyFieldEditText.text.toString().trim()
        val course = binding.courseEditText.text.toString().trim()

        if (fullName.isEmpty() || studyField.isEmpty() || course.isEmpty()) {
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = auth.currentUser?.uid ?: return
        val userRef = database.reference.child("users").child(userId)

        val profileData = hashMapOf(
            "fullName" to fullName,
            "studyField" to studyField,
            "course" to course,
            "profileCompleted" to true
        )

        userRef.updateChildren(profileData as Map<String, Any>)
            .addOnSuccessListener {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Ошибка сохранения: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}