package com.example.thetravelmapp.ui.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.thetravelmapp.data.model.Place
import com.example.thetravelmapp.data.repository.PlacesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlacesViewModel @Inject constructor(
    private val placesRepository: PlacesRepository
) : ViewModel() {

    private val _placesState = MutableStateFlow<PlacesState>(PlacesState.Loading)
    val placesState: StateFlow<PlacesState> = _placesState.asStateFlow()

    private val _placeDetailsState = MutableStateFlow<PlaceDetailsState>(PlaceDetailsState.Idle)
    val placeDetailsState: StateFlow<PlaceDetailsState> = _placeDetailsState.asStateFlow()

    private val _placeOperationState = MutableStateFlow<PlaceOperationState>(PlaceOperationState.Idle)
    val placeOperationState: StateFlow<PlaceOperationState> = _placeOperationState.asStateFlow()


    //Get all places
    fun getAllPlaces() {
        viewModelScope.launch {
            _placesState.value = PlacesState.Loading

            placesRepository.getAllPlaces().collectLatest { result ->
                result.fold(
                    onSuccess = { places ->
                        _placesState.value = if (places.isEmpty ()) {
                            PlacesState.Empty
                        } else {
                            PlacesState.Success(places)
                        }
                    },
                    onFailure = { exception ->
                        _placesState.value = PlacesState.Error(exception.message ?: "Failed to load places")
                    }
                )
            }
        }
    }

    //Get place by ID
    fun getPlaceById(placeId: String) {
        viewModelScope.launch {
            _placeDetailsState.value = PlaceDetailsState.Loading

            val result = placesRepository.getPlaceById(placeId)
            result.fold(
                onSuccess = { place ->
                    _placeDetailsState.value = PlaceDetailsState.Success(place)
                },
                onFailure = { exception ->
                    _placeDetailsState.value = PlaceDetailsState.Error(exception.message ?: "Failed to load place")
                }
            )
        }
    }

    // Add a new place
    fun addPlace(place: Place, imageUri: Uri?) {
        Log.d("PlacesViewModel", "addPlace called with place: ${place.name}, image: $imageUri")
        viewModelScope.launch {
            _placeOperationState.value = PlaceOperationState.Loading

            val result = placesRepository.addPlace(place, imageUri)
            result.fold(
                onSuccess = { placeId ->
                    _placeOperationState.value = PlaceOperationState.Success(placeId)
                },
                onFailure = { exception ->
                    _placeOperationState.value = PlaceOperationState.Error(exception.message ?: "Failed to add place")
                }
            )
        }
    }

    //Update an existing place
    fun updatePlace(place: Place, imageUri: Uri?) {
        viewModelScope.launch {
            _placeOperationState.value = PlaceOperationState.Loading

            val result = placesRepository.updatePlace(place, imageUri)
            result.fold(
                onSuccess = {
                    _placeOperationState.value = PlaceOperationState.Success(place.id)
                },
                onFailure = { exception ->
                    _placeOperationState.value = PlaceOperationState.Error(exception.message ?: "Failed to update place")
                }
            )
        }
    }

    //Delete a place
    fun deletePlace(placeId: String) {
        viewModelScope.launch {
            _placeOperationState.value = PlaceOperationState.Loading

            val result = placesRepository.deletePlace(placeId)
            result.fold(
                onSuccess = {
                    _placeOperationState.value = PlaceOperationState.Deleted
                },
                onFailure = { exception ->
                    _placeOperationState.value = PlaceOperationState.Error(exception.message ?: "Failed to delete place")
                }
            )
        }
    }

    fun resetOperationState() {
        _placeOperationState.value = PlaceOperationState.Idle
    }

    //States
    sealed class PlacesState {
        object Loading: PlacesState()
        object Empty: PlacesState()
        data class Success(val places: List<Place>) : PlacesState()
        data class Error(val message: String) : PlacesState()
    }

    sealed class PlaceDetailsState {
        object Idle: PlaceDetailsState()
        object Loading: PlaceDetailsState()
        data class Success(val places: Place) : PlaceDetailsState()
        data class Error(val message: String): PlaceDetailsState()
    }

    sealed class PlaceOperationState {
        object Idle: PlaceOperationState()
        object Loading: PlaceOperationState()
        data class Success(val placeId: String) : PlaceOperationState()
        object Deleted: PlaceOperationState()
        data class Error(val message: String) : PlaceOperationState()
    }
}