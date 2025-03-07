package com.example.recipe_rcm.ClovaOCR

data class OcrResponse(
    val images: List<Image>
)

data class Image(
    val receipt: Receipt? // ✅ 영수증 정보가 여기에 포함됨
)

data class Receipt(
    val result: ResultData?
)

data class ResultData(
    val subResults: List<SubResult>?
)

data class SubResult(
    val items: List<Item>?
)

data class Item(
    val name: TextData?,  // ✅ 품목명
    val count: TextData?, // ✅ 개수
)

data class TextData(
    val text: String?
)
