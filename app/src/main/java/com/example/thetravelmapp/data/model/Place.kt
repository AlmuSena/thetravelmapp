package com.example.thetravelmapp.data.model

import com.google.firebase.Timestamp

data class Place(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val rating: Float = 0f,
    val createdAt: Timestamp = Timestamp.now(),
    val userId: String = ""
) {

    fun toMap(): Map<String, Any?> {
        return mapOf(
            "name" to name,
            "description" to description,
            "imageUrl" to imageUrl,
            "rating" to rating,
            "createdAt" to createdAt,
            "userId" to userId
        )
    }
}