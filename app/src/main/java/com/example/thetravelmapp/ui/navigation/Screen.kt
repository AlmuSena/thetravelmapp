package com.example.thetravelmapp.ui.navigation

sealed class Screen(val route: String) {
    data object Welcome : Screen("welcome")
    data object Login : Screen("login")
    data object Register : Screen("register")
    data object PlacesList : Screen("places_list")
    data object PlaceDetail : Screen("place_detail/{placeId}") {
        fun createRoute(placeId: String) = "place_detail/$placeId"
    }
    data object AddPlace : Screen("add_place")
    data object EditPlace : Screen("edit_place/{placeId}") {
        fun createRoute(placeId: String) = "edit_place/$placeId"
    }
}