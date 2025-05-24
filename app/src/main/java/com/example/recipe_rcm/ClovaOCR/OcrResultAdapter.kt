package com.example.recipe_rcm.ClovaOCR

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.recipe_rcm.R
// OCR 결과를 나타내는 데이터 클래스
data class OcrItem(val name: String, val count: String)

class OcrResultAdapter(
    private val context: Context,
    private val items: MutableList<OcrItem>
) : RecyclerView.Adapter<OcrResultAdapter.OcrViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OcrViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_ocr_result, parent, false)
        return OcrViewHolder(view)
    }
    // 각 항목 뷰에 데이터를 바인딩
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: OcrViewHolder, position: Int) {
        val item = items[position]
        holder.ingredientName.text = item.name
        holder.ingredientCount.text = "${item.count}개"

        // 수정 버튼 클릭 시
        holder.btnEdit.setOnClickListener {
            val intent = Intent(context, DialogEditIngredientActivity::class.java).apply {
                putExtra("name", item.name)
                putExtra("count", item.count)
            }
            context.startActivity(intent)
        }
        // 삭제 버튼 클릭 시
        holder.btnDelete.setOnClickListener {
            items.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, itemCount)
        }
    }

    override fun getItemCount(): Int = items.size
    // ViewHolder 클래스 정의: 항목 레이아웃 내의 뷰들을 바인딩
    class OcrViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ingredientName: TextView = view.findViewById(R.id.tvIngredientName)
        val ingredientCount: TextView = view.findViewById(R.id.tvIngredientCount)
        val btnEdit: Button = view.findViewById(R.id.btnEdit)
        val btnDelete: Button = view.findViewById(R.id.btnDelete)
    }
}
