
package com.example.test_for_diplom

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
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

                val allMaterials = Supabase.client.from("materials")
                    .select()
                    .decodeList<MaterialLite>()

                val filteredMaterials = allMaterials.filter {
                    it.program_id == user.program_id && it.course == user.course?.toIntOrNull()
                }.mapNotNull {
                    val fileName = it.file_name
                    val link = it.link
                    if (fileName != null && link != null) SimpleMaterial(fileName, link) else null
                }

                if (filteredMaterials.isEmpty()) {
                    Toast.makeText(requireContext(), "Материалы не найдены", Toast.LENGTH_SHORT).show()
                } else {
                    showMaterials(filteredMaterials)
                }

            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }

    data class SimpleMaterial(
        val fileName: String,
        val link: String
    )

    private fun showMaterials(materials: List<SimpleMaterial>) {
        val context = requireContext()
        binding.materialContainer.removeAllViews()

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
                setOnClickListener {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(material.link))
                        startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(context, "Не удалось открыть ссылку", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            val layout = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(32, 32, 32, 32)
            }

            val fileNameText = TextView(context).apply {
                text = "Материал: ${material.fileName}"
                textSize = 16f
                setTextColor(Color.DKGRAY)
                setTypeface(null, Typeface.BOLD)
            }

            layout.addView(fileNameText)
            cardView.addView(layout)
            binding.materialContainer.addView(cardView)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}