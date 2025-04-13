package com.example.recipe_rcm.RecipeAPI

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import android.widget.ToggleButton
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.recipe_rcm.ApiService.adapter.RecipeAdapter
import com.example.recipe_rcm.ApiService.Recipe
import com.example.recipe_rcm.ApiService.RecipeDetailActivity
import com.example.recipe_rcm.R
import com.example.recipe_rcm.RecipeAPI.adapter.IngredientAdapter
import com.example.recipe_rcm.RecipeAPI.model.Ingredient

import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


class List_ingredient : Fragment() {

    private lateinit var ingredientAdapter: IngredientAdapter
    private lateinit var recipeAdapter: RecipeAdapter
    private lateinit var viewModel: IngredientViewModel
    private val recipeList = mutableListOf<Recipe>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.list_ingredient, container, false)

        // ViewModel 초기화
        viewModel = ViewModelProvider(requireActivity())[IngredientViewModel::class.java]

        // 재료 목록 RecyclerView 초기화
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        ingredientAdapter = IngredientAdapter(emptyList(),
            deleteAction = { ingredientName ->
                viewModel.deleteIngredient(ingredientName) // 재료 삭제 처리
            },
            editAction = { ingredient ->
                showEditIngredientDialog(ingredient) // 재료 수정 처리
            }
        )
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = ingredientAdapter

        // ViewModel 데이터 관찰 (재료 목록 업데이트)
        viewModel.ingredients.observe(viewLifecycleOwner) { ingredients ->
            ingredientAdapter.updateIngredient(ingredients)
        }

        // 레시피 추천 버튼 클릭 이벤트
        /*val ingredientsEditText = view.findViewById<EditText>(R.id.ingredientsEditText)
        val recommendButton = view.findViewById<Button>(R.id.btn_recommend_recipe)
        recommendButton.setOnClickListener {
            val ingredients = ingredientsEditText.text.toString()
            if (ingredients.isBlank()) {
                Toast.makeText(requireContext(), "재료를 입력해주세요.", Toast.LENGTH_SHORT).show()
            } else {
                fetchRecipes(ingredients)
            }
        }*/

        // 재료 추가 버튼 클릭 이벤트
        val addButton = view.findViewById<Button>(R.id.btn_add)
        addButton.setOnClickListener {
            showAddIngredientDialog()
        }
        scheduleExpirationUpdate()

        return view
    }

    /*private fun fetchRecipes(ingredients: String) {
        Home.fetchRecipesByIngredients(
            ingredients = ingredients,
            onResult = { recipes ->
                requireActivity().runOnUiThread {
                    // 레시피 RecyclerView 업데이트
                    recipeList.clear()
                    recipeList.addAll(recipes)
                    if (::recipeAdapter.isInitialized) {
                        recipeAdapter.notifyDataSetChanged()
                    } else {
                        setupRecipeRecyclerView(recipeList)
                    }
                }
            },
            onError = { errorMessage ->
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
                }
            }
        )
    }*/

    private fun setupRecipeRecyclerView(recipes: List<Recipe>) {
        val recyclerView = view?.findViewById<RecyclerView>(R.id.recyclerView)
        if (recyclerView != null) {
            recipeAdapter = RecipeAdapter(requireContext(), recipes) { recipe ->
                // 레시피 클릭 시 상세 화면으로 이동
                val intent = Intent(requireContext(), RecipeDetailActivity::class.java)
                intent.putExtra("recipe", recipe) // Parcelable로 레시피 데이터 전달
                startActivity(intent)
            }
            recyclerView.layoutManager = LinearLayoutManager(requireContext())
            recyclerView.adapter = recipeAdapter
        }
    }

    private fun showAddIngredientDialog() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_ingredient, null)
        val nameEditText = dialogView.findViewById<EditText>(R.id.nameEditText)
        val countEditText = dialogView.findViewById<EditText>(R.id.countEditText)
        val expirationEditText = dialogView.findViewById<EditText>(R.id.expirationEditText)
        val toggleRefrigerated = dialogView.findViewById<ToggleButton>(R.id.toggleRefrigerated)
        val toggleFrozen = dialogView.findViewById<ToggleButton>(R.id.toggleFrozen)
        val toggleRoomTemperature = dialogView.findViewById<ToggleButton>(R.id.toggleRoomTemperature)

        // 유통기한 선택
        expirationEditText.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    expirationEditText.setText("$year-${month + 1}-$dayOfMonth")
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        // 토글 버튼 상태에 따른 동작 설정
        val selectedColor = resources.getColor(R.color.toggle_selected, null)
        val unselectedColor = resources.getColor(R.color.toggle_unselected, null)

        toggleRefrigerated.setOnCheckedChangeListener { _, isChecked ->
            toggleRefrigerated.setBackgroundColor(if (isChecked) selectedColor else unselectedColor)
            if (isChecked) {
                toggleFrozen.isChecked = false
                toggleRoomTemperature.isChecked = false
            }
        }

        toggleFrozen.setOnCheckedChangeListener { _, isChecked ->
            toggleFrozen.setBackgroundColor(if (isChecked) selectedColor else unselectedColor)
            if (isChecked) {
                toggleRefrigerated.isChecked = false
                toggleRoomTemperature.isChecked = false
            }
        }

        toggleRoomTemperature.setOnCheckedChangeListener { _, isChecked ->
            toggleRoomTemperature.setBackgroundColor(if (isChecked) selectedColor else unselectedColor)
            if (isChecked) {
                toggleRefrigerated.isChecked = false
                toggleFrozen.isChecked = false
            }
        }

        // 다이얼로그 생성
        AlertDialog.Builder(requireContext())
            .setTitle("재료 추가")
            .setView(dialogView)
            .setPositiveButton("추가") { _, _ ->
                val name = nameEditText.text.toString()
                val count = countEditText.text.toString()
                val expiration = expirationEditText.text.toString()
                val storageStatus = when {
                    toggleRefrigerated.isChecked -> "냉장"
                    toggleFrozen.isChecked -> "냉동"
                    toggleRoomTemperature.isChecked -> "실온보관"
                    else -> "알 수 없음"
                }
                val expirationStatus = calculateExpirationStatus(expiration)
                val ingredient = Ingredient(name, count, expiration, expirationStatus, storageStatus)
                viewModel.addIngredient(ingredient) // Firebase에 추가
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun showEditIngredientDialog(ingredient: Ingredient) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_ingredient, null)
        val nameEditText = dialogView.findViewById<EditText>(R.id.nameEditText).apply {
            setText(ingredient.name)
            isEnabled = true // 이름은 수정 불가
        }
        val countEditText = dialogView.findViewById<EditText>(R.id.countEditText).apply {
            setText(ingredient.count)
        }
        val expirationEditText = dialogView.findViewById<EditText>(R.id.expirationEditText).apply {
            setText(ingredient.expiration)
        }

        // 보관 상태 토글 버튼 초기화
        val toggleRefrigerated = dialogView.findViewById<ToggleButton>(R.id.toggleRefrigerated)
        val toggleFrozen = dialogView.findViewById<ToggleButton>(R.id.toggleFrozen)
        val toggleRoomTemperature = dialogView.findViewById<ToggleButton>(R.id.toggleRoomTemperature)

        // 기존 보관 상태 반영
        when (ingredient.storageStatus) {
            "냉장" -> toggleRefrigerated.isChecked = true
            "냉동" -> toggleFrozen.isChecked = true
            "실온보관" -> toggleRoomTemperature.isChecked = true
            else -> { // 기본적으로 아무것도 선택 안 된 상태
                toggleRefrigerated.isChecked = false
                toggleFrozen.isChecked = false
                toggleRoomTemperature.isChecked = false
            }
        }

        // 보관 상태 토글 버튼 상태 설정
        toggleRefrigerated.setOnCheckedChangeListener { _, isChecked ->
            toggleRefrigerated.setBackgroundColor(if (isChecked) resources.getColor(R.color.toggle_selected, null) else resources.getColor(R.color.toggle_unselected, null))
            if (isChecked) {
                toggleFrozen.isChecked = false
                toggleRoomTemperature.isChecked = false
            }
        }

        toggleFrozen.setOnCheckedChangeListener { _, isChecked ->
            toggleFrozen.setBackgroundColor(if (isChecked) resources.getColor(R.color.toggle_selected, null) else resources.getColor(R.color.toggle_unselected, null))
            if (isChecked) {
                toggleRefrigerated.isChecked = false
                toggleRoomTemperature.isChecked = false
            }
        }

        toggleRoomTemperature.setOnCheckedChangeListener { _, isChecked ->
            toggleRoomTemperature.setBackgroundColor(if (isChecked) resources.getColor(R.color.toggle_selected, null) else resources.getColor(R.color.toggle_unselected, null))
            if (isChecked) {
                toggleRefrigerated.isChecked = false
                toggleFrozen.isChecked = false
            }
        }

        // 유통기한 선택
        expirationEditText.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    expirationEditText.setText("$year-${month + 1}-$dayOfMonth")
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        // 다이얼로그 생성
        AlertDialog.Builder(requireContext())
            .setTitle("재료 수정")
            .setView(dialogView)
            .setPositiveButton("수정") { _, _ ->
                val updatedName = nameEditText.text.toString()
                val updatedCount = countEditText.text.toString()
                val updatedExpiration = expirationEditText.text.toString()

                // 수정된 보관 상태 결정
                val updatedStorageStatus = when {
                    toggleRefrigerated.isChecked -> "냉장"
                    toggleFrozen.isChecked -> "냉동"
                    toggleRoomTemperature.isChecked -> "실온보관"
                    else -> "알 수 없음"
                }

                val updatedExpirationStatus = calculateExpirationStatus(updatedExpiration)

                // 업데이트된 재료 정보
                val updatedIngredient = ingredient.copy(
                    name = updatedName,
                    count = updatedCount,
                    expiration = updatedExpiration,
                    expirationStatus = updatedExpirationStatus,
                    storageStatus = updatedStorageStatus
                )
                viewModel.updateIngredientWithNameChange(ingredient.name, updatedIngredient) // 이름 변경 포함 업데이트
            }
            .setNegativeButton("취소", null)
            .show()
    }


    private fun calculateExpirationStatus(expiration: String): String {
        if (expiration.isBlank() || expiration == "유통기한 선택") {
            return "유통기한 미설정"
        }

        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val expirationDate = sdf.parse(expiration) ?: return "유효하지 않은 날짜"
            val today = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.time

            val diff = expirationDate.time - today.time
            val daysLeft = (diff / (1000 * 60 * 60 * 24)).toInt()

            when {
                daysLeft > 0 -> "$daysLeft 일 남음"
                daysLeft == 0 -> "오늘까지"
                else -> "${-daysLeft}일 지남"
            }
        } catch (e: Exception) {
            Log.e("DateParsing", "날짜 변환 실패: ${e.localizedMessage}")
            "날짜 변환 오류"
        }
    }


    private fun scheduleExpirationUpdate() {
        val workRequest = PeriodicWorkRequestBuilder<UpdateExpirationWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(calculateInitialDelay(), TimeUnit.MILLISECONDS) // 자정까지의 시간 계산
            .build()

        WorkManager.getInstance(requireContext()).enqueueUniquePeriodicWork(
            "UpdateExpirationWorker",
            ExistingPeriodicWorkPolicy.REPLACE,
            workRequest
        )
    }

    private fun calculateInitialDelay(): Long {
        val now = Calendar.getInstance()
        val nextMidnight = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.DAY_OF_YEAR, 1)
        }
        return nextMidnight.timeInMillis - now.timeInMillis
    }
}


