package com.nunoapps.cartelli

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.nunoapps.cartelli.ui.CartelloViewModel
import com.nunoapps.cartelli.ui.editor.EditorScreen
import com.nunoapps.cartelli.ui.scan.ScanScreen
import com.nunoapps.cartelli.ui.settings.SettingsScreen
import com.nunoapps.cartelli.ui.theme.CartelliTheme

object Routes {
    const val SCAN = "scan"
    const val EDITOR = "editor"
    const val SETTINGS = "settings"
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CartelliTheme {
                Surface(Modifier.fillMaxSize().navigationBarsPadding()) {
                    val navController = rememberNavController()
                    // ViewModel condiviso a livello di Activity tra le schermate.
                    val vm: CartelloViewModel = viewModel()

                    NavHost(navController = navController, startDestination = Routes.SCAN) {
                        composable(Routes.SCAN) {
                            ScanScreen(
                                onBarcode = { code ->
                                    vm.onBarcodeScanned(code)
                                    navController.navigate(Routes.EDITOR) {
                                        popUpTo(Routes.SCAN) { inclusive = true }
                                    }
                                },
                                onManual = {
                                    vm.startNew()
                                    navController.navigate(Routes.EDITOR) {
                                        popUpTo(Routes.SCAN) { inclusive = true }
                                    }
                                },
                            )
                        }
                        composable(Routes.EDITOR) {
                            EditorScreen(
                                vm = vm,
                                onNewScan = {
                                    vm.startNew()
                                    navController.navigate(Routes.SCAN) {
                                        popUpTo(Routes.EDITOR) { inclusive = true }
                                    }
                                },
                                onSettings = { navController.navigate(Routes.SETTINGS) },
                            )
                        }
                        composable(Routes.SETTINGS) {
                            SettingsScreen(vm = vm, onBack = { navController.popBackStack() })
                        }
                    }
                }
            }
        }
    }
}
