package week11.st830661.petpal.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import week11.st830661.petpal.view.login.ForgotPasswordScreen
import week11.st830661.petpal.view.login.LoginScreen
import week11.st830661.petpal.view.login.SignUpScreen
import week11.st830661.petpal.viewmodel.LoginViewModel

private const val ROUTE_LOGIN = "login"
private const val ROUTE_SIGNUP = "signup"
private const val ROUTE_FORGOT = "forgot"

@Composable
fun LoginNavigation(loginViewModel: LoginViewModel) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = ROUTE_LOGIN
    ) {
        composable(ROUTE_LOGIN) {
            LoginScreen(
                viewModel = loginViewModel,
                onNavigateToSignUp = { navController.navigate(ROUTE_SIGNUP) },
                onNavigateToForgot = { navController.navigate(ROUTE_FORGOT) }
            )
        }
        composable(ROUTE_SIGNUP) {
            SignUpScreen(
                viewModel = loginViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(ROUTE_FORGOT) {
            ForgotPasswordScreen(
                viewModel = loginViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
