package week11.st830661.petpal.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val infoMessage: String? = null
)

class LoginViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    var uiState by mutableStateOf(LoginUiState())
        private set

    fun onEmailChange(newValue: String) {
        uiState = uiState.copy(email = newValue, errorMessage = null, infoMessage = null)
    }

    fun onPasswordChange(newValue: String) {
        uiState = uiState.copy(password = newValue, errorMessage = null, infoMessage = null)
    }

    fun onConfirmPasswordChange(newValue: String) {
        uiState = uiState.copy(confirmPassword = newValue, errorMessage = null, infoMessage = null)
    }

    fun clearMessages() {
        uiState = uiState.copy(errorMessage = null, infoMessage = null)
    }

    fun login() {
        val email = uiState.email.trim()
        val password = uiState.password

        if (email.isBlank() || password.isBlank()) {
            uiState = uiState.copy(errorMessage = "Email and password are required.")
            return
        }

        uiState = uiState.copy(isLoading = true, errorMessage = null, infoMessage = null)

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                uiState = if (task.isSuccessful) {
                    uiState.copy(isLoading = false)
                } else {
                    uiState.copy(
                        isLoading = false,
                        errorMessage = task.exception?.localizedMessage ?: "Login failed."
                    )
                }
            }
    }

    fun register() {
        val email = uiState.email.trim()
        val password = uiState.password
        val confirm = uiState.confirmPassword

        if (email.isBlank() || password.isBlank() || confirm.isBlank()) {
            uiState = uiState.copy(errorMessage = "All fields are required.")
            return
        }
        if (password != confirm) {
            uiState = uiState.copy(errorMessage = "Passwords do not match.")
            return
        }

        uiState = uiState.copy(isLoading = true, errorMessage = null, infoMessage = null)

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                uiState = if (task.isSuccessful) {
                    uiState.copy(
                        isLoading = false,
                        infoMessage = "Account created. You can log in now."
                    )
                } else {
                    uiState.copy(
                        isLoading = false,
                        errorMessage = task.exception?.localizedMessage ?: "Sign up failed."
                    )
                }
            }
    }

    fun sendPasswordReset() {
        val email = uiState.email.trim()
        if (email.isBlank()) {
            uiState = uiState.copy(errorMessage = "Please enter your email.")
            return
        }

        uiState = uiState.copy(isLoading = true, errorMessage = null, infoMessage = null)

        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                uiState = if (task.isSuccessful) {
                    uiState.copy(
                        isLoading = false,
                        infoMessage = "Password reset email sent."
                    )
                } else {
                    uiState.copy(
                        isLoading = false,
                        errorMessage = task.exception?.localizedMessage
                            ?: "Failed to send reset email."
                    )
                }
            }
    }
}
