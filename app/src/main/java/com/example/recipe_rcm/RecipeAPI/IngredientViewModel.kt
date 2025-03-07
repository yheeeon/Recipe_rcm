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
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val ingredientList = mutableListOf<Ingredient>()
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

    fun addIngredient(ingredient: Ingredient) {
        val key = database.push().key
        key?.let {
            database.child(it).setValue(ingredient)
        }
    }

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
