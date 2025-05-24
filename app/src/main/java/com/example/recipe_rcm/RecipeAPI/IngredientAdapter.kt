package com.example.recipe_rcm.RecipeAPI.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.recipe_rcm.R
import com.example.recipe_rcm.RecipeAPI.model.Ingredient

// RecyclerView 어댑터: 재료(Ingredient) 목록을 보여주고 수정/삭제 기능 제공
class IngredientAdapter(
    private var ingredients: List<Ingredient> = emptyList(),
    private val deleteAction: (String) -> Unit,
    private val editAction: (Ingredient) -> Unit
) : RecyclerView.Adapter<IngredientAdapter.IngredientViewHolder>() {
    // 외부에서 재료 리스트를 업데이트할 때 호출
    fun updateIngredient(newIngredients: List<Ingredient>) {
        ingredients = newIngredients
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IngredientViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_ingredient, parent, false)
        return IngredientViewHolder(view)
    }
    //각 뷰홀더에 데이터 바인딩
    override fun onBindViewHolder(holder: IngredientViewHolder, position: Int) {
        val ingredient = ingredients[position]
        holder.bind(ingredient)
    }
    // 리스트 크기 반환
    override fun getItemCount(): Int = ingredients.size
    //아이템 뷰 내 각 UI 요소 참조 및 바인딩 기능 구현
    inner class IngredientViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val nameTextView: TextView = itemView.findViewById(R.id.textViewName)
        private val countTextView: TextView = itemView.findViewById(R.id.textViewcount)
        private val expirationTextView: TextView = itemView.findViewById(R.id.textViewExpiration)
        private val expirationStatusTextView: TextView = itemView.findViewById(R.id.expirationStatusTextView)
        private val storageStatusTextView: TextView = itemView.findViewById(R.id.textViewStorageStatus)
        private val deleteButton: Button = itemView.findViewById(R.id.btn_delete)
        private val editButton: Button = itemView.findViewById(R.id.btn_edit)

        // 재료 정보를 UI에 표시하고, 버튼 클릭 리스너 등록
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
