/**
 * Author: Sarah McCrie (991405606)
 * Forgot password screen for PetPal.
 * Lets the user request a password reset email using Firebase Auth
 */

package week11.st830661.petpal.view.login

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import week11.st830661.petpal.ui.theme.components.PetPalTextField
import week11.st830661.petpal.viewmodel.LoginViewModel

@Composable
fun ForgotPasswordScreen(
    viewModel: LoginViewModel,
    onNavigateBack: () -> Unit
) {
    val state = viewModel.uiState

    LaunchedEffect(Unit) { viewModel.clearMessages() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6FFF5))
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(64.dp))

        // Title
        Text(
            text = "Reset Password",
            style = MaterialTheme.typography.headlineMedium,
            fontSize = 24.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(24.dp))

        PetPalTextField(
            value = state.email,
            onValueChange = viewModel::onEmailChange,
            placeholder = "Email",
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Messages
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

        // Send Reset button
        Button(
            onClick = { viewModel.sendPasswordReset() },
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
                    color = Color.White
                )
            } else {
                Text("Send Reset Email", fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = "Back to login",
            modifier = Modifier
                .padding(bottom = 24.dp)
                .clickable { onNavigateBack() },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
