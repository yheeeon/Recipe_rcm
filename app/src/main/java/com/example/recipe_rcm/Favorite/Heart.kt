package com.example.recipe_rcm.Favorite

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.recipe_rcm.ApiService.adapter.RecipeAdapter
import com.example.recipe_rcm.ApiService.Recipe
import com.example.recipe_rcm.ApiService.RecipeDetailActivity
import com.example.recipe_rcm.R
import com.google.firebase.database.*

class Heart : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var recipeAdapter: RecipeAdapter
    private val favoriteRecipes = mutableListOf<Recipe>()
    private val databaseReference: DatabaseReference =
        FirebaseDatabase.getInstance().reference.child("favorites")

    private lateinit var emptyTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.heart, container, false)

        // RecyclerView와 EmptyTextView 초기화
        recyclerView = view.findViewById(R.id.favorites_recyclerview)
        emptyTextView = view.findViewById(R.id.emptyTextView)

        recyclerView.layoutManager = LinearLayoutManager(context)
        recipeAdapter = RecipeAdapter(requireContext(), favoriteRecipes) { recipe ->
            // 즐겨찾기 목록에서 레시피 클릭 시 상세 화면으로 이동
            val intent = Intent(requireContext(), RecipeDetailActivity::class.java)
            intent.putExtra("recipe", recipe)
            startActivity(intent)
        }
        recyclerView.adapter = recipeAdapter

        // Firebase에서 즐겨찾기 데이터 로드
        loadFavoritesFromFirebase()

        return view
    }
    //Firebase-favorite 노드에 데이터 변경이 있을 때마다 호출되는 리스너 등록
    private fun loadFavoritesFromFirebase() {
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                favoriteRecipes.clear()
                for (dataSnapshot in snapshot.children) {
                    val recipe = dataSnapshot.getValue(Recipe::class.java)
                    recipe?.let { favoriteRecipes.add(it) }
                }
                recipeAdapter.notifyDataSetChanged()

                // 데이터 유무에 따라 EmptyTextView 표시
                if (favoriteRecipes.isEmpty()) {
                    emptyTextView.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                } else {
                    emptyTextView.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE
                }
            }
            // 데이터 로드 실패 시 메시지 표시
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    requireContext(),
                    "데이터를 가져오는 데 실패했습니다: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }
}
