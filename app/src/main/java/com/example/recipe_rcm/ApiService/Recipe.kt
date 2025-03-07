package com.example.recipe_rcm.ApiService

import android.os.Parcel
import android.os.Parcelable

data class Recipe(
    val name: String = "",  // 기본 값 추가
    val category: String = "",
    val method: String = "",
    val calories: String = "",
    val ingredients: String = "",
    val imageUrl: String = "",
    val steps: List<String> = listOf(),
    val stepImages: List<String> = listOf(),
    var isFavorite: Boolean = false // 기본 값
) : Parcelable {

    // 기본 생성자 (Firebase에서 역직렬화할 때 필요)
    constructor() : this("", "", "", "", "", "", listOf(), listOf(), false)

    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.createStringArrayList() ?: emptyList(),
        parcel.createStringArrayList() ?: emptyList(),
        parcel.readByte() != 0.toByte() // isFavorite 복원
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeString(category)
        parcel.writeString(method)
        parcel.writeString(calories)
        parcel.writeString(ingredients)
        parcel.writeString(imageUrl)
        parcel.writeStringList(steps)
        parcel.writeStringList(stepImages)
        parcel.writeByte(if (isFavorite) 1 else 0) // isFavorite 저장
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Recipe> {
        override fun createFromParcel(parcel: Parcel): Recipe = Recipe(parcel)
        override fun newArray(size: Int): Array<Recipe?> = arrayOfNulls(size)
    }
}
