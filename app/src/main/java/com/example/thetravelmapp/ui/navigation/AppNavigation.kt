package com.example.thetravelmapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.thetravelmapp.ui.screens.auth.LoginScreen
import com.example.thetravelmapp.ui.screens.auth.RegisterScreen
import com.example.thetravelmapp.ui.screens.places.AddEditPlaceScreen
import com.example.thetravelmapp.ui.screens.places.PlaceDetailScreen
import com.example.thetravelmapp.ui.screens.places.PlacesListScreen
import com.example.thetravelmapp.ui.screens.welcome.WelcomeScreen
import com.example.thetravelmapp.ui.viewmodel.AuthViewModel
import com.example.thetravelmapp.ui.viewmodel.PlacesViewModel


@Composable
fun AppNavigation(navController: NavHostController) {
    //Get viewmodels
    val authViewModel: AuthViewModel = hiltViewModel()

    val placesViewModel: PlacesViewModel = hiltViewModel()


    //Observe auth state for navigation
    val authState by authViewModel.authState.collectAsState()

    LaunchedEffect(authState) {
        when (authState) {
            is AuthViewModel.AuthState.Success -> {
                navController.navigate(Screen.PlacesList.route) {
                    popUpTo(Screen.Welcome.route) { inclusive = true } // changed from PlacesList to Welcome
                }
            }

            else -> { /* No action needed */ }
        }
    }

    NavHost(
        navController = navController,
        startDestination = if (authViewModel.isUserLoggedIn) Screen.PlacesList.route else Screen.Welcome.route //changed from Login to Welcome
    ) {
        //Welcome Screen
        composable(route= Screen.Welcome.route) {
            WelcomeScreen(
                onLoginClick = { navController.navigate(Screen.Login.route) },
                onRegisterClick = { navController.navigate(Screen.Register.route) }
            )
        }

        //Auth Screens
        composable(route = Screen.Login.route) {
            LoginScreen(
                onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                authViewModel = authViewModel
            )
        }

        composable(route = Screen.Register.route) {
            RegisterScreen(
                onNavigateBack = { navController.popBackStack() },
                authViewModel = authViewModel
            )
        }

        //Places Screens
        composable(route = Screen.PlacesList.route) {
            PlacesListScreen(
                onNavigateToDetail = { placeId ->
                    navController.navigate(Screen.PlaceDetail.createRoute(placeId))
                },
                onNavigateToAdd = {
                    navController.navigate(Screen.AddPlace.route)
                },
                onSignOut = {
                    authViewModel.signOut()
                    //Navigate back to welcome screen after sign out
                    navController.navigate(Screen.Welcome.route) {
                        popUpTo(Screen.PlacesList.route) { inclusive = true }
                    }
                },
                placesViewModel = placesViewModel
            )
        }

        composable(
            route = Screen.PlaceDetail.route,
            arguments = listOf(navArgument("placeId") { type = NavType.StringType })
        ) { backStackEntry ->
            val placeId = backStackEntry.arguments?.getString("placeId") ?: ""
            PlaceDetailScreen(
                placeId = placeId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = { id ->
                    navController.navigate(Screen.EditPlace.createRoute(id))
                },
                onNavigateToList = {
                    navController.navigate(Screen.PlacesList.route) {
                        popUpTo(Screen.PlacesList.route) { inclusive = true }
                    }
                },
                placesViewModel = placesViewModel
            )
        }

        composable(route = Screen.AddPlace.route) {
            AddEditPlaceScreen(
                isEditMode = false,
                onNavigateBack = { navController.popBackStack() },
                onPlaceSaved = { placeId ->
                    navController.navigate(Screen.PlaceDetail.createRoute(placeId)) {
                        popUpTo(Screen.PlacesList.route)
                    }
                },
                placesViewModel = placesViewModel
            )
        }

        composable(
            route = Screen.EditPlace.route,
            arguments = listOf(navArgument("placeId") { type = NavType.StringType })
        ) { backStackEntry ->
            val placeId = backStackEntry.arguments?.getString("placeId") ?: ""
            AddEditPlaceScreen(
                placeId = placeId,
                isEditMode = true,
                onNavigateBack = { navController.popBackStack() },
                onPlaceSaved = { _ ->
                    navController.popBackStack()
                },
                placesViewModel = placesViewModel
            )
        }
    }
}