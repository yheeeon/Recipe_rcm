package com.example.recipe_rcm.RecipeAPI

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.recipe_rcm.Alram.Push_alram
import com.example.recipe_rcm.ApiService.adapter.RecipeAdapter
import com.example.recipe_rcm.ApiService.Recipe
import com.example.recipe_rcm.ApiService.RecipeDetailActivity
import com.example.recipe_rcm.databinding.HomeBinding
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL

class Home : Fragment() {

    private var _binding: HomeBinding? = null
    private val binding get() = _binding!!

    private val recipeList = mutableListOf<Recipe>()
    private lateinit var recipeAdapter: RecipeAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = HomeBinding.inflate(inflater, container, false)

        // RecyclerView와 RecipeAdapter 초기화
        recipeAdapter = RecipeAdapter(requireContext(), recipeList) { recipe ->
            // 이미지 클릭 시 RecipeDetailActivity로 이동
            val intent = Intent(requireContext(), RecipeDetailActivity::class.java)
            intent.putExtra("recipe", recipe) // Parcelable 객체 전달
            startActivity(intent)
        }
        binding.recipeRecyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.recipeRecyclerView.adapter = recipeAdapter

        // 사용자 입력 없이 바로 레시피를 랜덤으로 표시
        fetchRandomRecipesAndDisplay()

        // Push 알림 클릭 이벤트
        binding.alram.setOnClickListener {
            val intent = Intent(requireContext(), Push_alram::class.java)
            startActivity(intent)
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // 랜덤 레시피 가져오기
    private fun fetchRandomRecipesAndDisplay() {
        fetchRecipesByIngredients(null, onResult = { recipes ->
            requireActivity().runOnUiThread {
                recipeList.clear()
                recipeList.addAll(recipes)
                recipeAdapter.notifyDataSetChanged()
            }
        }, onError = { errorMessage ->
            requireActivity().runOnUiThread {
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
            }
        })
    }

    companion object {
        fun fetchRecipesByIngredients(
            ingredients: String?,
            onResult: (List<Recipe>) -> Unit,
            onError: (String) -> Unit
        ) {
            Thread {
                val key = "" // ✅ 식약처 API KEY
                val serviceId = "COOKRCP01"
                val dataType = "json"
                val startIdx = "1"
                val endIdx = "100"
                val site =
                    "http://openapi.foodsafetykorea.go.kr/api/$key/$serviceId/$dataType/$startIdx/$endIdx"

                try {
                    val url = URL(site)
                    val conn = url.openConnection()
                    val input = conn.getInputStream()
                    val isr = InputStreamReader(input)
                    val br = BufferedReader(isr)

                    val buf = StringBuffer()
                    var str: String?
                    do {
                        str = br.readLine()
                        if (str != null) buf.append(str)
                    } while (str != null)

                    val root = JSONObject(buf.toString())
                    val body = root.getJSONObject("COOKRCP01")
                    val items = body.getJSONArray("row")

                    Log.d("API_RESPONSE", "Items count: ${items.length()}")

                    val filteredRecipes = mutableListOf<Recipe>()
                    for (i in 0 until items.length()) {
                        val recipe = items.getJSONObject(i)
                        val recipeIngredients = recipe.getString("RCP_PARTS_DTLS")

                        // 재료 필터링
                        if (ingredients.isNullOrEmpty() || ingredients.split(",").all { it.trim() in recipeIngredients }) {
                            val recipeItem = Recipe(
                                name = recipe.getString("RCP_NM"),
                                category = recipe.getString("RCP_PAT2"),
                                method = recipe.getString("RCP_WAY2"),
                                calories = recipe.optString("INFO_ENG", "정보 없음"),
                                ingredients = recipe.getString("RCP_PARTS_DTLS"),
                                imageUrl = recipe.getString("ATT_FILE_NO_MAIN"),
                                steps = extractSteps(recipe),
                                stepImages = extractStepImages(recipe)
                            )
                            filteredRecipes.add(recipeItem)
                        }
                    }
                    Log.d("API_RESPONSE", "Filtered recipes count: ${filteredRecipes.size}")

                    onResult(filteredRecipes)
                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.e("API_ERROR", "Error message: ${e.message}")
                    onError("데이터를 불러오는 중 오류가 발생했습니다.")
                }
            }.start()
        }

        private fun extractSteps(recipe: JSONObject): List<String> {
            val steps = mutableListOf<String>()
            for (i in 1..20) {
                val manual = recipe.optString("MANUAL%02d".format(i), "")
                if (manual.isNotEmpty()) steps.add(manual)
            }
            return steps
        }

        private fun extractStepImages(recipe: JSONObject): List<String> {
            val stepImages = mutableListOf<String>()
            for (i in 1..20) {
                val manualImg = recipe.optString("MANUAL_IMG%02d".format(i), "")
                if (manualImg.isNotEmpty()) stepImages.add(manualImg)
            }
            return stepImages
        }
    }
}