package week11.st830661.petpal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import week11.st830661.petpal.login.LoginScreen
import week11.st830661.petpal.ui.theme.PetPalTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PetPalTheme {
                val auth = remember { com.google.firebase.auth.FirebaseAuth.getInstance() }
                var currentUser by remember { mutableStateOf(auth.currentUser) }

                DisposableEffect(Unit) {
                    val l = com.google.firebase.auth.FirebaseAuth.AuthStateListener { fb ->
                        currentUser = fb.currentUser
                    }
                    auth.addAuthStateListener(l)
                    onDispose { auth.removeAuthStateListener(l) }
                }

                if (currentUser == null) {
                    // if no current user signed in
                    LoginScreen()
                } else {
                    // if signed already signed in
                    MainScreen(
                        modifier = Modifier.fillMaxSize(),
                        uid = currentUser!!.uid,
                        onLogout = { auth.signOut() }
                    )
                }
            }
        }
    }
}

@Composable
fun MainScreen(
    modifier: Modifier,
    uid: String,
    onLogout: () -> Unit
) {
    Text(
        text = "Hello Android!",
        modifier = modifier
    )
    Scaffold { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            // Logout row (top-right)
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(onClick = onLogout) {
                    Text("Logout")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            
        }
    }
}
