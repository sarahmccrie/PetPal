package week11.st830661.petpal.login

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.Firebase
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

@Composable
fun LoginScreen() {

    val auth = Firebase.auth

    val emailState = remember { (mutableStateOf(value = "")) }
    val passwordState = remember { (mutableStateOf(value = "")) }

    Log.d("mylog", "The current email: ${auth.currentUser?.email}")

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        //email
        TextField(value = emailState.value, onValueChange =
            {
                emailState.value = it
            })

        Spacer(modifier = Modifier.height(10.dp))

        //password
        TextField(value = passwordState.value, onValueChange =
            {
                passwordState.value = it
            })

        Spacer(modifier = Modifier.height(10.dp))

        //sign in
        Button(
            onClick = ({
                signIn(auth, emailState.value, passwordState.value)

            }),
        ) {
            Text("Login")
        }

        //sign up
        Button(
            onClick = ({
                signUp(auth, emailState.value, passwordState.value)

            }),
        ) {
            Text("Register")
        }
    }

}

private fun signUp(auth: FirebaseAuth, email: String, password: String) {
    auth.createUserWithEmailAndPassword(email, password)
        .addOnCompleteListener {
            if(it.isSuccessful) {
                Log.d("mylog", "SignUp successful")
            }
            else {
                Log.d("mylog", "SignUp failed", it.exception)
            }
        }
}

private fun signIn(auth: FirebaseAuth, email: String, password: String) {
    auth.signInWithEmailAndPassword(email, password)
        .addOnCompleteListener {
            if(it.isSuccessful) {
                Log.d("mylog", "SignIn successful")
            }
            else {
                Log.d("mylog", "SignIn failed", it.exception)
            }
        }
}

private fun signOut(auth: FirebaseAuth) {
    auth.signOut()
    Log.d("mylog", "Sign Out")
}

private fun deleteAccount(auth: FirebaseAuth, email: String, password: String) {

    val credential = EmailAuthProvider.getCredential(email, password)
    auth.currentUser?.reauthenticate(credential)?.addOnCompleteListener { it
        if(it.isSuccessful) {
            auth.currentUser?.delete()
            Log.d("mylog", "User deleted successful")
        }
        else {
            Log.d("mylog", "Reauthentication failed", it.exception)
        }
    }
}