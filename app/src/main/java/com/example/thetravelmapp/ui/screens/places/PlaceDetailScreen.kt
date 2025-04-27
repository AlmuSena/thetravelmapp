package com.example.thetravelmapp.ui.screens.places

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.thetravelmapp.data.model.Place
import com.example.thetravelmapp.ui.viewmodel.PlacesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceDetailScreen(
    placeId: String,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (String) -> Unit,
    onNavigateToList: () -> Unit,
    placesViewModel: PlacesViewModel
) {
    val placeDetailsState by placesViewModel.placeDetailsState.collectAsState()
    val placeOperationState by placesViewModel.placeOperationState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }

    //Load place details when screen is shown
    LaunchedEffect(placeId) {
        placesViewModel.getPlaceById(placeId)
    }

    //Handle place deletion result
    LaunchedEffect(placeOperationState) {
        if (placeOperationState is PlacesViewModel.PlaceOperationState.Deleted) {
            placesViewModel.resetOperationState()
            onNavigateToList()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Place Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    //Edit button
                    IconButton(
                        onClick = { onNavigateToEdit(placeId) },
                        enabled = placeDetailsState is PlacesViewModel.PlaceDetailsState.Success
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit"
                        )
                    }

                    //Delete button
                    IconButton(
                        onClick = { showDeleteDialog = true },
                        enabled = placeDetailsState is PlacesViewModel.PlaceDetailsState.Success
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (placeDetailsState) {
                is PlacesViewModel.PlaceDetailsState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is PlacesViewModel.PlaceDetailsState.Success -> {
                    val place = (placeDetailsState as PlacesViewModel.PlaceDetailsState.Success).places
                    PlaceDetail(place = place)
                }

                is PlacesViewModel.PlaceDetailsState.Error -> {
                    ErrorView(
                        message = (placeDetailsState as PlacesViewModel.PlaceDetailsState.Error).message,
                        onRetry = { placesViewModel.getPlaceById(placeId) },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                else -> { /* Do nothing */ }
            }

            //Show Loading indicator when deleting
            if (placeOperationState is PlacesViewModel.PlaceOperationState.Loading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }

    //Delete confirmation dialog

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Place") },
            text = { Text("Are you sure you want to delete this place? This action cannot be undone" ) },
            confirmButton = {
                Button(
                    onClick = {
                        placesViewModel.deletePlace(placeId)
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    //Error dialog

    if (placeOperationState is PlacesViewModel.PlaceOperationState.Error) {
        AlertDialog(
            onDismissRequest = { placesViewModel.resetOperationState() },
            title = { Text("Error") },
            text = { Text((placeOperationState as PlacesViewModel.PlaceOperationState.Error).message) },
            confirmButton = {
                Button(onClick = { placesViewModel.resetOperationState() }) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
fun PlaceDetail(place: Place) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        //Place image
        if (place.imageUrl.isNotEmpty()) {
            AsyncImage(
                model = place.imageUrl,
                contentDescription = place.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text("No image")
            }
        }

        //Place details
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = place.name,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Row(
                modifier = Modifier.padding(bottom = 16.dp)
            ) {

            }

            Row(
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Text(
                    text = "Rating: ",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${place.rating}/5",
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Text(
                text = "Description: ",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = place.description,
                style = MaterialTheme.typography.bodyLarge
            )
        }

    }
}