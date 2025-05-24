package com.example.recipe_rcm.RecipeAPI

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.recipe_rcm.RecipeAPI.model.Ingredient
import com.google.firebase.database.*

class IngredientViewModel : ViewModel() {
    private val database: DatabaseReference = FirebaseDatabase.getInstance().getReference("ingredients")
    private val _ingredients = MutableLiveData<List<Ingredient>>()
    val ingredients: LiveData<List<Ingredient>> get() = _ingredients

    init {
        // DB의 "ingredients" 경로 변경 감지 이벤트 리스너 등록
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val ingredientList = mutableListOf<Ingredient>()
                // 모든 자식 노드들을 Ingredient 객체로 변환해 리스트에 추가
                for (child in snapshot.children) {
                    val ingredient = child.getValue(Ingredient::class.java)
                    ingredient?.let { ingredientList.add(it) }
                }
                _ingredients.value = ingredientList
            }

            override fun onCancelled(error: DatabaseError) {
                // 에러 처리
            }
        })
    }
    // 이름 기준으로 재료 삭제 (조건에 맞는 노드 찾아 삭제)
    fun addIngredient(ingredient: Ingredient) {
        val key = database.push().key
        key?.let {
            database.child(it).setValue(ingredient)
        }
    }
    // 이름 기준으로 재료 수정 (같은 이름 가진 노드 찾아 값 교체)
    fun deleteIngredient(name: String) {
        database.orderByChild("name").equalTo(name)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (child in snapshot.children) {
                        child.ref.removeValue()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // 삭제 실패 처리
                }
            })
    }
    //❌
    fun updateIngredient(updatedIngredient: Ingredient) {
        database.orderByChild("name").equalTo(updatedIngredient.name)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (child in snapshot.children) {
                        // 기존 항목을 찾아 수정
                        child.ref.setValue(updatedIngredient)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // 에러 처리
                }
            })
    }

    // 이름 변경까지 처리 가능한 메서드 추가
    fun updateIngredientWithNameChange(oldName: String, updatedIngredient: Ingredient) {
        // 기존 이름으로 데이터 검색 및 삭제
        database.orderByChild("name").equalTo(oldName)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (child in snapshot.children) {
                        // 기존 데이터를 삭제
                        child.ref.removeValue()
                    }

                    // 새로운 데이터 추가
                    val key = database.push().key
                    key?.let {
                        database.child(it).setValue(updatedIngredient)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // 에러 처리
                }
            })
    }
}
