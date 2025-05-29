
package com.example.test_for_diplom

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.test_for_diplom.databinding.FragmentMaterialBinding
import com.google.android.material.card.MaterialCardView
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.launch

class MaterialsFragment : Fragment() {

    private var _binding: FragmentMaterialBinding? = null
    private val binding get() = _binding!!

    private val sessionManager by lazy { SessionManager(requireContext()) }

    private lateinit var allSubjects: List<Subject>
    private lateinit var allMaterials: List<MaterialLite>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMaterialBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadMaterials()
    }

    private fun loadMaterials() {
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

                allMaterials = Supabase.client.from("materials")
                    .select()
                    .decodeList<MaterialLite>()

                allSubjects = Supabase.client.from("subjects")
                    .select()
                    .decodeList<Subject>()

                val filteredSubjects = allSubjects.filter {
                    it.program_id == user.program_id && it.course == user.course?.toIntOrNull()
                }

                val filteredMaterials = allMaterials.filter {
                    it.program_id == user.program_id && it.course == user.course?.toIntOrNull()
                }

                showSubjectButtons(filteredSubjects, filteredMaterials)

            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }

    private fun showSubjectButtons(subjects: List<Subject>, materials: List<MaterialLite>) {
        val context = requireContext()
        binding.materialContainer.removeAllViews()

        for (subject in subjects) {
            val button = TextView(context).apply {
                text = subject.name
                textSize = 18f
                setTextColor(Color.WHITE)
                setBackgroundColor(ContextCompat.getColor(context, R.color.teal_700))
                setPadding(32, 24, 32, 24)
                setTypeface(null, Typeface.BOLD)
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(16, 16, 16, 16)
                }

                setOnClickListener {
                    val subjectMaterials = materials.filter { it.subject_id == subject.id }
                    showMaterials(subject.name, subjectMaterials)
                }
            }

            binding.materialContainer.addView(button)
        }
    }


    private fun showMaterials(subjectName: String, materials: List<MaterialLite>) {
        val context = requireContext()
        binding.materialContainer.removeAllViews()

        // Добавим заголовок
        val header = TextView(context).apply {
            text = "Материалы по предмету: $subjectName"
            textSize = 20f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.BLACK)
            setPadding(32, 16, 32, 16)
        }
        binding.materialContainer.addView(header)

        // Кнопка "Назад к предметам"
        val backButton = Button(context).apply {
            text = "← Назад к предметам"
            setBackgroundColor(ContextCompat.getColor(context, R.color.purple_200))
            setTextColor(Color.WHITE)
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(32, 16, 32, 32)
            }

            setOnClickListener {
                showSubjectButtons(allSubjects, allMaterials)
            }
        }
        binding.materialContainer.addView(backButton)

        // Показываем материалы
        for (material in materials) {
            val cardView = MaterialCardView(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(16, 16, 16, 16)
                }
                radius = 24f
                elevation = 8f
                setCardBackgroundColor(ContextCompat.getColor(context, R.color.white))
            }

            val layout = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(32, 32, 32, 32)
            }

            val titleText = TextView(context).apply {
                text = "Материал: ${material.file_name ?: "Без названия"}"
                textSize = 16f
                setTextColor(Color.BLACK)
            }

            layout.addView(titleText)
            cardView.addView(layout)

            cardView.setOnClickListener {
                material.link?.let { url ->
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    startActivity(intent)
                }
            }

            binding.materialContainer.addView(cardView)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}