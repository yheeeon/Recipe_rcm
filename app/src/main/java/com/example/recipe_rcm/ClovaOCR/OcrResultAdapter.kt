package com.example.recipe_rcm.ClovaOCR

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.recipe_rcm.R

data class OcrItem(val name: String, val count: String)

class OcrResultAdapter(
    private val context: Context,
    private val items: List<OcrItem>
) : RecyclerView.Adapter<OcrResultAdapter.OcrViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OcrViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_ocr_result, parent, false)
        return OcrViewHolder(view)
    }

    override fun onBindViewHolder(holder: OcrViewHolder, position: Int) {
        val item = items[position]
        holder.ingredientName.text = item.name
        holder.ingredientCount.text = "${item.count}개"

        holder.btnEdit.setOnClickListener {
            val intent = Intent(context, DialogEditIngredientActivity::class.java).apply {
                putExtra("name", item.name)
                putExtra("count", item.count)
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = items.size

    class OcrViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ingredientName: TextView = view.findViewById(R.id.tvIngredientName)
        val ingredientCount: TextView = view.findViewById(R.id.tvIngredientCount)
        val btnEdit: Button = view.findViewById(R.id.btnEdit)
    }
}
