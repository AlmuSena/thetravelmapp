package com.example.thetravelmapp.data.repository

import android.net.Uri
import android.util.Log
import com.example.thetravelmapp.data.model.Place
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlacesRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val auth: FirebaseAuth
) {

    private val placesCollection = firestore.collection("places")

    //Get all places
    fun getAllPlaces(): Flow<Result<List<Place>>> = flow {
        try {
            val snapshot = placesCollection
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()

            val places = snapshot.documents.mapNotNull { document ->
                val place = document.toObject(Place::class.java)
                place?.copy(id = document.id)
            }

            emit(Result.success(places))
        } catch (e: Exception) {
            emit (Result.failure(e))
        }
    }

    //Get a single place by ID
    suspend fun getPlaceById(placeId: String): Result<Place> {
        return try {
            val document = placesCollection.document(placeId).get().await()

            if (document.exists()) {
                val place = document.toObject(Place::class.java)?.copy(id = document.id)
                if (place != null) {
                    Result.success(place)
                } else {
                    Result.failure(Exception("Unable to parse place data"))
                }
            } else {
                Result.failure(Exception("Place not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    //Add a place
    suspend fun addPlace(place: Place, imageUri: Uri?): Result<String> {
        return try {
            val userId = auth.currentUser?.uid ?: return Result.failure(Exception("User not logged in"))

            //Upload image if provided
            val imageUrl = if (imageUri != null) {
                uploadImage(imageUri)
            } else {
                ""
            }

            // Create place with image URL and user ID
            val placeWithUser = place.copy(
                imageUrl = imageUrl,
                userId = userId
            )

            //Add to firestore
            val docRef = placesCollection.add(placeWithUser.toMap()).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    //Update a place
    suspend fun updatePlace(place: Place, imageUri: Uri?): Result<Unit> {
        return try {
            val userId = auth.currentUser?.uid ?: return Result.failure(Exception("User not logged in"))

            //Check if place belongs to current user
            val existingPlace = getPlaceById(place.id)
            if (existingPlace.isSuccess && existingPlace.getOrNull()?.userId != userId) {
                return Result.failure(Exception("You can only edit your own places"))
            }

            //Upload new image if provided
            val imageUrl = if (imageUri != null) {
                uploadImage(imageUri)
            } else {
                place.imageUrl
            }

            //Update a place with possibly new image Url
            val updatedPlace = place.copy(imageUrl = imageUrl)

            //Update in Firestore
            placesCollection.document(place.id).update(updatedPlace.toMap()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    //Delete a place
    suspend fun deletePlace(placeId: String): Result<Unit> {
        return try {
            val userId = auth.currentUser?.uid ?: return Result.failure(Exception("User not logged in"))

            //Check if place belongs to current user
            val existingPlace = getPlaceById(placeId)
            if (existingPlace.isSuccess) {
                val place = existingPlace.getOrNull()
                if (place?.userId != userId) {
                    return Result.failure(Exception("You can only delete your own places"))
                }

                //Delete image if it exists
                if (place.imageUrl.isNotEmpty()) {
                    try {
                        val imageRef = storage.getReferenceFromUrl(place.imageUrl)
                        imageRef.delete().await()
                    } catch (e: Exception) {
                        //Continue even if image deletion fails
                    }
                }
            }

            //Delete from Firestore
            placesCollection.document(placeId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    //Helper function to upload an image to Firebase Storage
    private suspend fun uploadImage(imageUri: Uri): String {
        val storageRef = storage.reference
        // val imageRef = storageRef.child("place_images/${UUID.randomUUID()}")
        val imageRef = storageRef.child(imageUri.toString())
        Log.d("ImageReeeeef2", imageUri.toString())
        return try {
            imageRef.putFile(imageUri).await()
            imageRef.downloadUrl.await().toString()
        } catch (e: Exception) {
            throw e
        }
    }
}

