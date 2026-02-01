package com.panaadhaar.extractor.india.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Home : Screen("home")
    object Camera : Screen("camera")
    object Results : Screen("results")
    object Profiles : Screen("profiles")
    object ProfileDetail : Screen("profile_detail/{profileId}") {
        fun createRoute(profileId: Long) = "profile_detail/$profileId"
    }
    object Export : Screen("export")
    object Paywall : Screen("paywall")
    object Settings : Screen("settings")
}
