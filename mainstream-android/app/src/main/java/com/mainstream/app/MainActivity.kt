package com.mainstream.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.mainstream.app.navigation.NavGraph
import com.mainstream.app.navigation.Screen
import com.mainstream.app.ui.MainScreen
import com.mainstream.app.ui.auth.LoginViewModel
import com.mainstream.app.ui.theme.MainstreamTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainstreamTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainApp()
                }
            }
        }
    }
}

@Composable
fun MainApp() {
    val navController = rememberNavController()
    val loginViewModel: LoginViewModel = hiltViewModel()
    val uiState by loginViewModel.uiState.collectAsState()

    val startDestination = if (uiState.isAuthenticated) {
        Screen.Trophies.route
    } else {
        Screen.Login.route
    }

    if (uiState.isAuthenticated) {
        MainScreen(
            onLogout = {
                loginViewModel.logout()
            }
        )
    } else {
        NavGraph(
            navController = navController,
            startDestination = startDestination
        )
    }
}
