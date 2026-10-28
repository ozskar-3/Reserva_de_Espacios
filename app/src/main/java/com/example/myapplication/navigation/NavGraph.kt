package com.example.myapplication.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.myapplication.ui.screens.*
import com.example.myapplication.ui.viewmodel.ReservationViewModel

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Home : Screen("home")
    object BuscarCalendario : Screen("buscar_calendario")
    object ReservaSala : Screen("reserva_sala/{roomId}/{roomName}") {
        fun createRoute(roomId: String, roomName: String) = "reserva_sala/$roomId/$roomName"
    }
    object MisReservas : Screen("mis_reservas")
    object AdminPanel : Screen("admin_panel")
}

@Composable
fun AppNavigation(
    navController: NavHostController,
    viewModel: ReservationViewModel
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = { isCoordinator, email ->
                    viewModel.setUserEmail(email)
                    if (isCoordinator) {
                        navController.navigate(Screen.AdminPanel.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    } else {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToSearch = { navController.navigate(Screen.BuscarCalendario.route) },
                onNavigateToMyReservations = { navController.navigate(Screen.MisReservas.route) },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0)
                    }
                }
            )
        }

        composable(Screen.BuscarCalendario.route) {
            BuscarCalendarioScreen(
                viewModel = viewModel,
                onRoomSelected = { roomId, roomName ->
                    navController.navigate(Screen.ReservaSala.createRoute(roomId, roomName))
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.ReservaSala.route,
            arguments = listOf(
                navArgument("roomId") { type = NavType.StringType },
                navArgument("roomName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val roomId = backStackEntry.arguments?.getString("roomId") ?: ""
            val roomName = backStackEntry.arguments?.getString("roomName") ?: ""
            ReservaSalaScreen(
                roomId = roomId,
                roomName = roomName,
                viewModel = viewModel,
                onReservationComplete = {
                    navController.navigate(Screen.MisReservas.route) {
                        popUpTo(Screen.Home.route)
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.MisReservas.route) {
            MisReservasScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.AdminPanel.route) {
            AdminPanelScreen(
                viewModel = viewModel,
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0)
                    }
                }
            )
        }
    }
}
