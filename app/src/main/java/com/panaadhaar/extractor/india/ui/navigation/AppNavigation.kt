package com.panaadhaar.extractor.india.ui.navigation

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.panaadhaar.extractor.india.data.database.AppDatabase
import com.panaadhaar.extractor.india.data.repository.BillingRepository
import com.panaadhaar.extractor.india.data.repository.EntitlementRepository
import com.panaadhaar.extractor.india.data.repository.ProfileRepository
import com.panaadhaar.extractor.india.ui.ViewModelFactory
import com.panaadhaar.extractor.india.ui.screens.splash.SplashScreen
import com.panaadhaar.extractor.india.utils.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val application = context.applicationContext as Application
    
    val database = remember { AppDatabase.getInstance(context) }
    val sharedPreferences = remember { 
        context.getSharedPreferences(Constants.BILLING_PREFS, android.content.Context.MODE_PRIVATE) 
    }
    
    val profileRepository = remember { ProfileRepository(database.profileDao()) }
    val entitlementRepository = remember { 
        EntitlementRepository(database.entitlementDao(), sharedPreferences) 
    }
    val billingRepository = remember {
        BillingRepository(
            context = context,
            entitlementRepository = entitlementRepository,
            coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
        )
    }
    
    val viewModelFactory = remember {
        ViewModelFactory(
            application = application,
            profileRepository = profileRepository,
            entitlementRepository = entitlementRepository,
            billingRepository = billingRepository
        )
    }
    
    LaunchedEffect(Unit) {
        entitlementRepository.initializeEntitlement()
    }
    
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                navController = navController,
                onCheckEntitlement = {
                    // Entitlement already initialized in LaunchedEffect above
                }
            )
        }
        
        composable(Screen.Home.route) {
            // TODO: Wire up HomeScreen with ViewModel state and callbacks
            // Placeholder - needs actual screen implementation wiring
        }
        
        composable(Screen.Camera.route) {
            // TODO: Wire up CameraScreen with ViewModel state and callbacks
            // Placeholder - needs actual screen implementation wiring
        }
        
        composable(Screen.Results.route) {
            // TODO: Wire up ResultsScreen with ViewModel state and callbacks
            // Placeholder - needs actual screen implementation wiring
        }
        
        composable(Screen.Profiles.route) {
            // TODO: Wire up ProfilesScreen with ViewModel state and callbacks
            // Placeholder - needs actual screen implementation wiring
        }
        
        composable(
            route = Screen.ProfileDetail.route,
            arguments = listOf(navArgument("profileId") { type = NavType.StringType })
        ) { backStackEntry ->
            val profileId = backStackEntry.arguments?.getString("profileId") ?: ""
            // TODO: Wire up ProfileDetailScreen with ViewModel state and callbacks
            // Placeholder - needs actual screen implementation wiring
        }
        
        composable(Screen.Export.route) {
            // TODO: Wire up ExportScreen with ViewModel state and callbacks
            // Placeholder - needs actual screen implementation wiring
        }
        
        composable(Screen.Paywall.route) {
            // TODO: Wire up PaywallScreen with ViewModel state and callbacks
            // Placeholder - needs actual screen implementation wiring
        }
        
        composable(Screen.Settings.route) {
            // TODO: Wire up SettingsScreen with ViewModel state and callbacks
            // Placeholder - needs actual screen implementation wiring
        }
    }
}
