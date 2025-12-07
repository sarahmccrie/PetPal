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
import androidx.compose.foundation.background
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
import week11.st830661.petpal.ui.theme.PetPalTheme
import week11.st830661.petpal.view.dashboard.DashboardScreen
import week11.st830661.petpal.viewmodel.PetsScreen
import week11.st830661.petpal.viewmodel.HealthScreen
import week11.st830661.petpal.view.reminder.RemindersScreen
import week11.st830661.petpal.view.reminder.ReminderDetailScreen
import week11.st830661.petpal.view.reminder.AppointmentDetailScreen
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
import week11.st830661.petpal.navigation.LoginNavigation
import week11.st830661.petpal.viewmodel.LoginViewModel
import week11.st830661.petpal.viewmodel.PetsViewModel
import week11.st830661.petpal.viewmodel.PetsViewModelFactory
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
import week11.st830661.petpal.utils.ReminderScheduler
import week11.st830661.petpal.viewmodel.MedicalRecordViewModel

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

                val loginViewModel: LoginViewModel = viewModel()

                if (currentUser == null) {
                    // if no current user signed in
                    LoginNavigation(loginViewModel = loginViewModel)
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
    val reminderViewModel: ReminderViewModel = viewModel(
        factory = ReminderViewModelFactory(uid, context)
    )

    val petsViewModel: PetsViewModel = viewModel(
        factory = PetsViewModelFactory(uid)
    )

    val coroutineScope = rememberCoroutineScope()

    var selectedNavItem by remember { mutableStateOf(NavItem.Dashboard) }
    var selectedReminder by remember { mutableStateOf<Reminder?>(null) }
    var selectedAppointment by remember { mutableStateOf<Appointment?>(null) }

    // Collect reminders and appointments from Firestore via ViewModel
    val reminders by reminderViewModel.reminders.collectAsState(initial = emptyList())
    val appointments by reminderViewModel.appointments.collectAsState(initial = emptyList())
    val pets by petsViewModel.pets.collectAsState(initial = emptyList())

    // Reschedule active reminders when they load from Firestore
    LaunchedEffect(reminders) {
        val reminderScheduler = ReminderScheduler(context)
        reminders.filter { it.isActive }.forEach { reminder ->
            reminderScheduler.scheduleReminder(reminder)
        }
    }

    // If a detail screen is open, show it
    if (selectedReminder != null) {
        ReminderDetailScreen(
            reminder = selectedReminder!!,
            onBackClick = { selectedReminder = null },
            onDeleteClick = {
                reminderViewModel.deleteReminder(selectedReminder!!.id)
                selectedReminder = null
            },
            onUpdateReminder = { updatedReminder ->
                reminderViewModel.updateReminder(updatedReminder)
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
                reminderViewModel.deleteAppointment(selectedAppointment!!.id)
                selectedAppointment = null
            },
            onUpdateAppointment = { updatedAppointment ->
                reminderViewModel.updateAppointment(updatedAppointment)
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
                .background(Color(0xFFF6FFF5))
                .padding(innerPadding)
        ) {
            when (selectedNavItem) {
                NavItem.Dashboard -> DashboardScreen(
                    reminders = reminders,
                    appointments = appointments,
                    pets = pets,
                    onReminderClick = { selectedReminder = it },
                    onAppointmentClick = { selectedAppointment = it },
                    onLogout = onLogout
                )
                NavItem.Pets -> PetsScreen(
                    uid = uid
                )
                NavItem.Health -> HealthScreen(uid,
                    reminderViewModel)
                NavItem.Reminders -> RemindersScreen(
                    reminders = reminders,
                    appointments = appointments,
                    pets = pets,
                    onReminderClick = { selectedReminder = it },
                    onAppointmentClick = { selectedAppointment = it },
                    onAddReminder = { reminder ->
                        coroutineScope.launch {
                            reminderViewModel.addReminder(reminder)
                        }
                    },
                    onAddAppointment = { appointment ->
                        coroutineScope.launch {
                            reminderViewModel.addAppointment(appointment)
                        }
                    }
                )
            }
        }
    }
}
