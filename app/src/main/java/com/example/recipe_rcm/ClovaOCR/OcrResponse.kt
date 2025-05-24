package com.example.recipe_rcm.ClovaOCR

data class OcrResponse(
    val images: List<Image> //OCR 결과로 인식된 이미지 목록
)

data class Image(
    val receipt: Receipt? //영수증 정보
)

data class Receipt(
    val result: ResultData? //OCR 추출된 결과 데이터
)

data class ResultData(
    val subResults: List<SubResult>? //여러 항목 그룹이 있을 수 있음
)

data class SubResult(
    val items: List<Item>? //식재료 등 항목 리스트
)

data class Item(
    val name: TextData?,  //품목명(재료명)
    val count: TextData?, //개수
)

data class TextData(
    val text: String? //OCR로 인식된 문자열
)
