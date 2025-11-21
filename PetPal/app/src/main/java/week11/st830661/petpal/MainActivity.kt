package week11.st830661.petpal

import android.os.Bundle
import android.content.Context
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import android.Manifest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import week11.st830661.petpal.view.login.LoginScreen
import week11.st830661.petpal.ui.theme.PetPalTheme
import week11.st830661.petpal.viewmodel.DashboardScreen
import week11.st830661.petpal.viewmodel.PetsScreen
import week11.st830661.petpal.viewmodel.HealthScreen
import week11.st830661.petpal.viewmodel.RemindersScreen
import week11.st830661.petpal.viewmodel.ReminderDetailScreen
import week11.st830661.petpal.viewmodel.AppointmentDetailScreen
import week11.st830661.petpal.navigation.BottomNavigationBar
import week11.st830661.petpal.navigation.NavItem
import week11.st830661.petpal.data.models.Reminder
import week11.st830661.petpal.data.models.Appointment
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.viewmodel.compose.viewModel
import week11.st830661.petpal.viewmodel.ReminderViewModel
import week11.st830661.petpal.viewmodel.ReminderViewModelFactory
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val requestNotificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        // Handle permission result
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Create notification channel for reminders (required for Android 8.0+)
        createNotificationChannel()

        // Request notification permission for Android 13+
        requestNotificationPermissionIfNeeded()

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
                        context = this@MainActivity,
                        onLogout = { auth.signOut() }
                    )
                }
            }
        }
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API level 26+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Pet Reminders"
            val descriptionText = "Notifications for pet feeding, medication, and other reminders"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("petpal_reminders", name, importance).apply {
                description = descriptionText
                enableVibration(true)
                enableLights(true)
                setShowBadge(true)
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        // Request notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}

@Composable
fun MainScreen(
    modifier: Modifier,
    uid: String,
    context: Context,
    onLogout: () -> Unit
) {
    val viewModel: ReminderViewModel = viewModel(
        factory = ReminderViewModelFactory(uid, context)
    )

    val coroutineScope = rememberCoroutineScope()

    var selectedNavItem by remember { mutableStateOf(NavItem.Dashboard) }
    var selectedReminder by remember { mutableStateOf<Reminder?>(null) }
    var selectedAppointment by remember { mutableStateOf<Appointment?>(null) }

    // Collect reminders and appointments from Firestore via ViewModel
    val reminders by viewModel.reminders.collectAsState(initial = emptyList())
    val appointments by viewModel.appointments.collectAsState(initial = emptyList())

    // If a detail screen is open, show it
    if (selectedReminder != null) {
        ReminderDetailScreen(
            reminder = selectedReminder!!,
            onBackClick = { selectedReminder = null },
            onDeleteClick = {
                viewModel.deleteReminder(selectedReminder!!.id)
                selectedReminder = null
            },
            onUpdateReminder = { updatedReminder ->
                viewModel.updateReminder(updatedReminder)
                selectedReminder = null
            }
        )
        return
    }

    if (selectedAppointment != null) {
        AppointmentDetailScreen(
            appointment = selectedAppointment!!,
            onBackClick = { selectedAppointment = null },
            onDeleteClick = {
                viewModel.deleteAppointment(selectedAppointment!!.id)
                selectedAppointment = null
            },
            onUpdateAppointment = { updatedAppointment ->
                viewModel.updateAppointment(updatedAppointment)
                selectedAppointment = null
            }
        )
        return
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            BottomNavigationBar(
                selectedItem = selectedNavItem,
                onItemSelected = { selectedNavItem = it }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedNavItem) {
                NavItem.Dashboard -> DashboardScreen(
                    reminders = reminders,
                    appointments = appointments,
                    onReminderClick = { selectedReminder = it },
                    onAppointmentClick = { selectedAppointment = it }
                )
                NavItem.Pets -> PetsScreen()
                NavItem.Health -> HealthScreen()
                NavItem.Reminders -> RemindersScreen(
                    reminders = reminders,
                    appointments = appointments,
                    onReminderClick = { selectedReminder = it },
                    onAppointmentClick = { selectedAppointment = it },
                    onAddReminder = { reminder ->
                        coroutineScope.launch {
                            viewModel.addReminder(reminder)
                        }
                    },
                    onAddAppointment = { appointment ->
                        coroutineScope.launch {
                            viewModel.addAppointment(appointment)
                        }
                    }
                )
            }
        }
    }
}
