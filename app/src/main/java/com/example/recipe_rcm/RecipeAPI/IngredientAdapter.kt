package com.example.recipe_rcm.RecipeAPI.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.recipe_rcm.R
import com.example.recipe_rcm.RecipeAPI.model.Ingredient


class IngredientAdapter(
    private var ingredients: List<Ingredient> = emptyList(),
    private val deleteAction: (String) -> Unit,
    private val editAction: (Ingredient) -> Unit
) : RecyclerView.Adapter<IngredientAdapter.IngredientViewHolder>() {

    fun updateIngredient(newIngredients: List<Ingredient>) {
        ingredients = newIngredients
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IngredientViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_ingredient, parent, false)
        return IngredientViewHolder(view)
    }

    override fun onBindViewHolder(holder: IngredientViewHolder, position: Int) {
        val ingredient = ingredients[position]
        holder.bind(ingredient)
    }

    override fun getItemCount(): Int = ingredients.size

    inner class IngredientViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val nameTextView: TextView = itemView.findViewById(R.id.textViewName)
        private val countTextView: TextView = itemView.findViewById(R.id.textViewcount)
        private val expirationTextView: TextView = itemView.findViewById(R.id.textViewExpiration)
        private val expirationStatusTextView: TextView = itemView.findViewById(R.id.expirationStatusTextView)
        private val storageStatusTextView: TextView = itemView.findViewById(R.id.textViewStorageStatus)
        private val deleteButton: Button = itemView.findViewById(R.id.btn_delete)
        private val editButton: Button = itemView.findViewById(R.id.btn_edit)

        fun bind(ingredient: Ingredient) {
            nameTextView.text = ingredient.name
            countTextView.text = "수량: ${ingredient.count}"
            expirationTextView.text = "유통기한: ${ingredient.expiration}"
            expirationStatusTextView.text = ingredient.expirationStatus
            storageStatusTextView.text = "보관 상태: ${ingredient.storageStatus}"

            deleteButton.setOnClickListener {
                deleteAction(ingredient.name)
            }
            editButton.setOnClickListener {
                editAction(ingredient)
            }
        }
    }
}
