package com.example.recipe_rcm.ApiService

import android.os.Parcel
import android.os.Parcelable

data class Recipe(
    val name: String = "",          // 레시피 이름
    val category: String = "",      // 레시피 카테고리
    val method: String = "",        // 조리 방법
    val calories: String = "",      // 칼로리
    val carbohydrate: String = "",  // 탄수화물
    val protein: String = "",       // 단백질
    val fat: String = "",           // 지방
    val sodium: String = "",        // 나트륨
    val weight: String = "",        // 중량
    val seq: String = "",           // 일련번호
    val hashTag: String = "",       // 해시태그
    val ingredients: String = "",   // 재료 목록
    val imageUrl: String = "",      // 레시피 이미지 url
    val steps: List<String> = listOf(),     //조리 순서 텍스트
    val stepImages: List<String> = listOf(),//조리 순서 이미지 url 리스트
    var isFavorite: Boolean = false         //즐겨찾기 여부
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "", // name
        parcel.readString() ?: "", // category
        parcel.readString() ?: "", // method
        parcel.readString() ?: "", // calories
        parcel.readString() ?: "", // carbohydrate
        parcel.readString() ?: "", // protein
        parcel.readString() ?: "", // fat
        parcel.readString() ?: "", // sodium
        parcel.readString() ?: "", // weight
        parcel.readString() ?: "", // seq
        parcel.readString() ?: "", // hashTag
        parcel.readString() ?: "", // ingredients
        parcel.readString() ?: "", // imageUrl
        parcel.createStringArrayList() ?: emptyList(), // steps
        parcel.createStringArrayList() ?: emptyList(), // stepImages
        parcel.readByte() != 0.toByte() // isFavorite
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeString(category)
        parcel.writeString(method)
        parcel.writeString(calories)
        parcel.writeString(carbohydrate)
        parcel.writeString(protein)
        parcel.writeString(fat)
        parcel.writeString(sodium)
        parcel.writeString(weight)
        parcel.writeString(seq)
        parcel.writeString(hashTag)
        parcel.writeString(ingredients)
        parcel.writeString(imageUrl)
        parcel.writeStringList(steps)
        parcel.writeStringList(stepImages)
        parcel.writeByte(if (isFavorite) 1 else 0)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Recipe> {
        override fun createFromParcel(parcel: Parcel): Recipe = Recipe(parcel)
        override fun newArray(size: Int): Array<Recipe?> = arrayOfNulls(size)
    }
}
