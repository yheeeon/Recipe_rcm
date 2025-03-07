package com.example.recipe_rcm.ApiService.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.recipe_rcm.ApiService.Recipe
import com.example.recipe_rcm.R

class RecipeAdapter(
    private val context: Context,
    private val recipes: List<Recipe>,
    private val onItemClick: (Recipe) -> Unit // 클릭 이벤트
) : RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder>() {

    inner class RecipeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val recipeImage: ImageView = view.findViewById(R.id.recipeImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.recipe_item, parent, false)
        return RecipeViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        val recipe = recipes[position]

        // Glide를 사용하여 이미지 로드
        Glide.with(context)
            .load(recipe.imageUrl)
            .placeholder(R.drawable.loading) // 로딩 중 이미지
            .into(holder.recipeImage) //레시피 이미지

        // 이미지 클릭 이벤트
        holder.recipeImage.setOnClickListener {
            onItemClick(recipe)
        }
    }

    override fun getItemCount(): Int = recipes.size
}
