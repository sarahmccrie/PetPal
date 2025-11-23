package week11.st830661.petpal.view.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import week11.st830661.petpal.R
import week11.st830661.petpal.ui.theme.components.PetPalTextField
import week11.st830661.petpal.viewmodel.LoginViewModel

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onNavigateToSignUp: () -> Unit,
    onNavigateToForgot: () -> Unit
) {
    val state = viewModel.uiState

    LaunchedEffect(Unit) { viewModel.clearMessages() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top spacing
        Spacer(modifier = Modifier.height(64.dp))

        // Title + subtitle
        Text(
            text = "Petpal",
            style = MaterialTheme.typography.labelLarge
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Welcome Back",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Paw icon
        Image(
            painter = painterResource(id = R.drawable.pawprint),
            contentDescription = "Petpal logo",
            modifier = Modifier.size(96.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Email
        PetPalTextField(
            value = state.email,
            onValueChange = viewModel::onEmailChange,
            placeholder = "Email",
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Password
        PetPalTextField(
            value = state.password,
            onValueChange = viewModel::onPasswordChange,
            placeholder = "Password",
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Forgot password
        Text(
            text = "Forgot Password?",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier
                .align(Alignment.End)
                .clickable { onNavigateToForgot() }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Error
        state.errorMessage?.let {
            Text(
                it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(Modifier.height(8.dp))
        }
        state.infoMessage?.let {
            Text(
                it,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(Modifier.height(8.dp))
        }

        // Green login button
        Button(
            onClick = { viewModel.login() },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            enabled = !state.isLoading,
            shape = MaterialTheme.shapes.medium,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Log In", fontWeight = FontWeight.SemiBold)
            }
        }

        // Push bottom text down
        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = "Don’t have an account? Sign up",
            modifier = Modifier
                .padding(bottom = 24.dp)
                .clickable { onNavigateToSignUp() },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
