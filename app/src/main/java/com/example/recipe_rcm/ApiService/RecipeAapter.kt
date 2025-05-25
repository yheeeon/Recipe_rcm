package com.example.recipe_rcm.ApiService.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.recipe_rcm.ApiService.Recipe
import com.example.recipe_rcm.ApiService.RecipeDetailActivity
import com.example.recipe_rcm.R

class RecipeAdapter(
    private val context: Context,
    private val recipes: List<Recipe>,
    private val onItemClick: (Recipe) -> Unit
) : RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder>() {

    inner class RecipeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val recipeImage: ImageView = view.findViewById(R.id.recipeImage)
        val recipeName: TextView = view.findViewById(R.id.recipeName)
    }
    // ViewHolder를 생성, layout을 inflate (XML → View 객체로 변환)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.recipe_item, parent, false)
        return RecipeViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        val recipe = recipes[position]

        // Glide를 사용하여 이미지 로드
        Glide.with(context)
            .load(recipe.imageUrl)
            .placeholder(R.drawable.loading) // 로딩 중
            .into(holder.recipeImage) //레시피 이미지

        // 레시피명 텍스트뷰에 세팅
        holder.recipeName.text = recipe.name

        // 이미지 클릭 시 상세 화면 이동
        holder.recipeImage.setOnClickListener {
            val intent = Intent(context, RecipeDetailActivity::class.java)
            intent.putExtra("recipe", recipe)  // seq 등 필요한 데이터 전달
            context.startActivity(intent)
        }

        // 아이템 전체 클릭 시도 상세 화면 이동
        holder.itemView.setOnClickListener {
            val intent = Intent(context, RecipeDetailActivity::class.java)
            intent.putExtra("recipe", recipe)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = recipes.size
}