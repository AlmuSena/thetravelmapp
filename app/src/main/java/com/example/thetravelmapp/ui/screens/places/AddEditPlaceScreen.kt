package com.example.thetravelmapp.ui.screens.places

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.thetravelmapp.data.model.Place
import com.example.thetravelmapp.ui.viewmodel.PlacesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditPlaceScreen(
    placeId: String = "",
    isEditMode: Boolean = false,
    onNavigateBack: () -> Unit,
    onPlaceSaved: (String) -> Unit,
    placesViewModel: PlacesViewModel
) {
    val placeDetailsState by placesViewModel.placeDetailsState.collectAsState()
    val placeOperationState by placesViewModel.placeOperationState.collectAsState()

    //Debugging log
    LaunchedEffect(placeOperationState) {
        Log.d("AddEditPlaceScreen", "Operation state: $placeOperationState")
    }

    //Form state
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var rating by remember { mutableStateOf("0.0") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var existingImageUrl by remember { mutableStateOf("") }

    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    //Image picker
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { imageUri = it }
    }

    //Load existing place data if in edit mode
    LaunchedEffect(placeId, isEditMode) {
        if (isEditMode && placeId.isNotEmpty()) {
            placesViewModel.getPlaceById(placeId)
        }
    }

    //Update form with existing place data
    LaunchedEffect(placeDetailsState) {
        if (placeDetailsState is PlacesViewModel.PlaceDetailsState.Success && isEditMode) {
            val place = (placeDetailsState as PlacesViewModel.PlaceDetailsState.Success).places
            name = place.name
            description = place.description
            rating = place.rating.toString()
            existingImageUrl = place.imageUrl
        }
    }

    //Handle place operation result
    LaunchedEffect(placeOperationState) {
        if (placeOperationState is PlacesViewModel.PlaceOperationState.Success) {
            val newPlaceId = (placeOperationState as PlacesViewModel.PlaceOperationState.Success).placeId
            placesViewModel.resetOperationState()
            onPlaceSaved(newPlaceId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "Edit Place" else "Add New Place" ) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            //Image selection
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline,
                        shape = RectangleShape
                    )
                    .clickable { imagePickerLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (imageUri != null) {
                    //Show selected image
                    AsyncImage(
                        model = imageUri,
                        contentDescription = "Selected image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else if (existingImageUrl.isNotEmpty()) {
                    //Show existing image
                    AsyncImage(
                        model = existingImageUrl,
                        contentDescription = "Existing image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                } else {
                    //Show placeholder
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Tap to select an image",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            //Name field
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Place name") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            //Rating field
            OutlinedTextField(
                value = rating,
                onValueChange = {
                    //Only allow valid ratings
                    val newValue = it.replace(",", ".")
                    try {
                        val ratingValue = newValue.toFloatOrNull()
                        if (newValue.isEmpty() || (ratingValue != null && ratingValue in 0f..5f)) {
                            rating = newValue
                        }
                    } catch (e: Exception) {
                        //Keep current value if error
                    }
                },
                label = { Text("Rating(0-5)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            //Description field
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                minLines = 4,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            //Save button
            Button(
                onClick = {
                    if (isValidPlaceData(name, description)) {
                        val place = Place(
                            id = if (isEditMode) placeId else "",
                            name =name,
                            description = description,
                            rating = rating.toFloatOrNull() ?: 0f,
                            imageUrl = existingImageUrl // will be updated on server if there is a new image
                        )

                        if (isEditMode) {
                            placesViewModel.updatePlace(place, imageUri)
                        } else {
                            placesViewModel.addPlace(place, imageUri)
                        }
                    }
                },
                enabled = isValidPlaceData(name, description) &&
                        placeOperationState !is PlacesViewModel.PlaceOperationState.Loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                if (placeOperationState is PlacesViewModel.PlaceOperationState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(if (isEditMode) "Update Place" else "Add Place")
                }
            }
        }
    }

    //Error dialog
    if (placeOperationState is PlacesViewModel.PlaceOperationState.Error) {
        AlertDialog(
            onDismissRequest = { placesViewModel.resetOperationState() },
            title = { Text("Error") },
            text = { Text((placeOperationState as PlacesViewModel.PlaceOperationState.Error).message) },
            confirmButton = {
                Button(onClick = { placesViewModel.resetOperationState() }) {
                    Text("Ok")
                }
            }
        )
    }
}

private fun isValidPlaceData(name: String, description: String): Boolean {
    return name.isNotBlank() && description.isNotBlank()
}