package com.example.recipe_rcm.RecipeAPI

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.recipe_rcm.Alram.Push_alram
import com.example.recipe_rcm.ApiService.Recipe
import com.example.recipe_rcm.ApiService.RecipeDetailActivity
import com.example.recipe_rcm.ApiService.adapter.RecipeAdapter
import com.example.recipe_rcm.R
import com.example.recipe_rcm.databinding.HomeBinding
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL

class Home : Fragment() {

    private var _binding: HomeBinding? = null
    private val binding get() = _binding!!

    // 활동 수준
    private val activityLevels = listOf("앉아서 일함", "가벼운 활동", "보통 활동", "격렬한 활동")
    private val activityMultipliers = listOf(1.2, 1.375, 1.55, 1.725)

    // RecipeAdapter를 위한 레시피 리스트
    private val recipes = mutableListOf<Recipe>()
    private lateinit var recipeAdapter: RecipeAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = HomeBinding.inflate(inflater, container, false)

        // Spinner 설정
        val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, activityLevels)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.activityLevelSpinner.adapter = spinnerAdapter

        // RecyclerView 설정: 가로로 스크롤되는 레이아웃 매니저
        recipeAdapter = RecipeAdapter(requireContext(), recipes) { recipe ->
            // 레시피 클릭 시 동작
            val intent = Intent(requireContext(), RecipeDetailActivity::class.java)
            intent.putExtra("recipe_seq", recipe.seq)
            startActivity(intent)
        }

        // RecyclerView 설정을 가로로 스크롤하는 레이아웃 매니저로 변경
        binding.recipeRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.recipeRecyclerView.adapter = recipeAdapter


        // 추천 버튼 클릭 시
        binding.recommendButton.setOnClickListener {
            recommendByTDEE()
        }

        // 알람 아이콘 클릭 시
        binding.alram.setOnClickListener {
            startActivity(Intent(requireContext(), Push_alram::class.java))
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun recommendByTDEE() {
        try {
            val genderId = binding.genderGroup.checkedRadioButtonId
            if (genderId == -1) {
                Toast.makeText(requireContext(), "성별을 선택해주세요.", Toast.LENGTH_SHORT).show()
                return
            }

            val gender = if (genderId == R.id.radioMale) "male" else "female"
            val age = binding.ageInput.text.toString().toIntOrNull()
            val height = binding.heightInput.text.toString().toDoubleOrNull()
            val weight = binding.weightInput.text.toString().toDoubleOrNull()

            if (age == null || height == null || weight == null) {
                Toast.makeText(requireContext(), "나이, 키, 몸무게를 모두 입력해주세요.", Toast.LENGTH_SHORT).show()
                return
            }

            val bmr = calculateBMR(gender, weight, height, age)
            val activityIndex = binding.activityLevelSpinner.selectedItemPosition
            val activityMultiplier = activityMultipliers.getOrElse(activityIndex) { 1.2 }
            val tdee = bmr * activityMultiplier

            val ingredients = binding.ingredientsEditText.text.toString()
            if (ingredients.isBlank()) {
                Toast.makeText(requireContext(), "재료를 입력해주세요.", Toast.LENGTH_SHORT).show()
                return
            }

            fetchRecipesByIngredients(ingredients, onResult = { fetchedRecipes ->
                requireActivity().runOnUiThread {
                    Log.d("RECIPE", "필터 전 레시피 수: ${fetchedRecipes.size}")

                    val perMealTDEE = tdee / 3
                    val minCal = perMealTDEE * 0.7
                    val maxCal = perMealTDEE * 1.1

                    val filtered = fetchedRecipes.filter {
                        val cal = it.calories.toDoubleOrNull()
                        val inRange = cal != null && cal in minCal..maxCal
                        inRange
                    }

                    Log.d("RECIPE", "필터 후 레시피 수: ${filtered.size}")

                    // 업데이트된 레시피 리스트로 RecyclerView 갱신
                    recipes.clear()
                    recipes.addAll(filtered)
                    recipeAdapter.notifyDataSetChanged()

                    Toast.makeText(requireContext(), "레시피를 불러왔습니다.", Toast.LENGTH_SHORT).show()
                }
            }, onError = {
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "레시피 불러오기 실패: $it", Toast.LENGTH_SHORT).show()
                }
            })

        } catch (e: Exception) {
            Toast.makeText(requireContext(), "입력값 처리 중 오류 발생: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun calculateBMR(gender: String, weight: Double, height: Double, age: Int): Double {
        return if (gender == "male") {
            66.47 + (13.75 * weight) + (5.003 * height) - (6.755 * age)
        } else {
            655.1 + (9.563 * weight) + (1.850 * height) - (4.676 * age)
        }
    }

    companion object {
        fun fetchRecipesByIngredients(
            ingredients: String?,
            onResult: (List<Recipe>) -> Unit,
            onError: (String) -> Unit
        ) {
            Thread {
                val key = "" // API 키 삽입
                val serviceId = "COOKRCP01"
                val dataType = "json"
                val startIdx = "1"
                val endIdx = "100"
                val urlStr = "http://openapi.foodsafetykorea.go.kr/api/$key/$serviceId/$dataType/$startIdx/$endIdx"

                try {
                    val url = URL(urlStr)
                    val conn = url.openConnection()
                    val input = conn.getInputStream()
                    val reader = BufferedReader(InputStreamReader(input))
                    val response = reader.readText()

                    val json = JSONObject(response)
                    val items = json.getJSONObject("COOKRCP01").getJSONArray("row")

                    val recipes = mutableListOf<Recipe>()
                    for (i in 0 until items.length()) {
                        val obj = items.getJSONObject(i)
                        val parts = obj.getString("RCP_PARTS_DTLS")

                        val isMatched = ingredients.isNullOrEmpty() || ingredients.split(",").all { parts.contains(it.trim()) }

                        if (isMatched) {
                            val recipe = Recipe(
                                name = obj.getString("RCP_NM"),
                                category = obj.getString("RCP_PAT2"),
                                method = obj.getString("RCP_WAY2"),
                                calories = obj.optString("INFO_ENG", "0.0"),
                                carbohydrate = obj.optString("INFO_CAR", "0.0"),
                                protein = obj.optString("INFO_PRO", "0.0"),
                                fat = obj.optString("INFO_FAT", "0.0"),
                                sodium = obj.optString("INFO_NA", "0.0"),
                                weight = obj.optString("INFO_WGT", "0.0"),
                                seq = obj.optString("RCP_SEQ", ""),
                                hashTag = obj.optString("HASH_TAG", ""),
                                ingredients = parts,
                                imageUrl = obj.getString("ATT_FILE_NO_MAIN"),
                                steps = extractSteps(obj),
                                stepImages = extractStepImages(obj)
                            )

                            Log.d("RECIPE", "추가된 레시피: ${recipe.name} / 칼로리: ${recipe.calories}")
                            recipes.add(recipe)
                        }
                    }

                    Log.d("RECIPE", "총 레시피 수: ${recipes.size}")
                    onResult(recipes)
                } catch (e: Exception) {
                    e.printStackTrace()
                    onError("에러 발생: ${e.message}")
                }
            }.start()
        }

        private fun extractSteps(obj: JSONObject): List<String> {
            return (1..20).mapNotNull {
                obj.optString("MANUAL%02d".format(it)).takeIf { it.isNotBlank() }
            }
        }

        private fun extractStepImages(obj: JSONObject): List<String> {
            return (1..20).mapNotNull {
                obj.optString("MANUAL_IMG%02d".format(it)).takeIf { it.isNotBlank() }
            }
        }
    }
}

