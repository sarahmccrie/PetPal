package week11.st830661.petpal.view.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import week11.st830661.petpal.data.models.Appointment
import week11.st830661.petpal.data.models.Reminder
import week11.st830661.petpal.data.models.Pet
import week11.st830661.petpal.data.models.AppointmentType
import week11.st830661.petpal.ui.theme.reusableComponents.ActivityLogItem
import java.time.format.DateTimeFormatter
import  week11.st830661.petpal.ui.theme.reusableComponents.VetVisitCard
import  week11.st830661.petpal.ui.theme.reusableComponents.VaccinationCard
import week11.st830661.petpal.ui.theme.dashboardCardBackground
import week11.st830661.petpal.ui.theme.logoutButtonText
import week11.st830661.petpal.ui.theme.textGray

@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    reminders: List<Reminder> = emptyList(),
    appointments: List<Appointment> = emptyList(),
    pets: List<Pet> = emptyList(),
    onReminderClick: (Reminder) -> Unit = {},
    onAppointmentClick: (Appointment) -> Unit = {},
    onLogout: () -> Unit = {}
) {
    // Get upcoming events sorted by date/time
    val upcomingReminders = reminders.filter { it.isActive }.sortedBy { it.time }.take(2)
    val upcomingAppointments = appointments.sortedBy { it.dateTime }.take(2)
    val vetVisits = appointments.filter { it.type == AppointmentType.VET_VISIT }.sortedBy { it.dateTime }.take(3)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
//                IconButton(onClick = {}) {
//                    Icon(Icons.Default.Menu, contentDescription = "Menu")
//                }
                Text(
                    text = "Dashboard",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Logout",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = logoutButtonText,
                    modifier = Modifier
                        .clickable { onLogout() }
                        .padding(start = 8.dp)
                )
            }
        }

        // Upcoming Section
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "Upcoming",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                if (upcomingReminders.isEmpty() && upcomingAppointments.isEmpty()) {
                    Text(
                        text = "No upcoming events",
                        fontSize = 14.sp,
                        color = textGray,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                } else {
                    // Display upcoming reminders
                    upcomingReminders.forEach { reminder ->
                        val petImageUrl = pets.find { it.name == reminder.petName }?.photoUrl ?: ""
                        VaccinationCard(
                            title = "${reminder.type.name} - ${reminder.petName}",
                            dueLabel = reminder.title,
                            dueInfo = reminder.time?.let {
                                DateTimeFormatter.ofPattern("HH:mm").format(it)
                            } ?: "Not scheduled",
                            backgroundColor = dashboardCardBackground,
                            petImageUrl = petImageUrl,
                            onClick = { onReminderClick(reminder) }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Display upcoming appointments
                    upcomingAppointments.forEach { appointment ->
                        val petImageUrl = pets.find { it.name == appointment.petName }?.photoUrl ?: ""
                        VaccinationCard(
                            title = "${appointment.title} - ${appointment.petName}",
                            dueLabel = appointment.vetName,
                            dueInfo = DateTimeFormatter.ofPattern("MMM dd").format(appointment.dateTime),
                            backgroundColor = dashboardCardBackground,
                            petImageUrl = petImageUrl,
                            onClick = { onAppointmentClick(appointment) }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Vet Visits Section
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "Vet Visits",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                if (vetVisits.isEmpty()) {
                    Text(
                        text = "No upcoming vet visits",
                        fontSize = 14.sp,
                        color = textGray,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                } else {
                    vetVisits.forEach { appointment ->
                        val petImageUrl = pets.find { it.name == appointment.petName }?.photoUrl ?: ""
                        VetVisitCard(
                            title = "${appointment.petName} - ${appointment.type.name.replace("_", " ")}",
                            nextVisit = "Next visit on ${DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm").format(appointment.dateTime)}",
                            vetName = appointment.vetName,
                            backgroundColor = dashboardCardBackground,
                            petImageUrl = petImageUrl,
                            onClick = { onAppointmentClick(appointment) }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Activity Log Section
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 0.dp)
            ) {
                Text(
                    text = "Activity Log",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Walk with Max
                ActivityLogItem(
                    title = "Walk with Max",
                    time = "10:30 AM",
                    icon = "🐾"
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Walk with Bella
                ActivityLogItem(
                    title = "Walk with Bella",
                    time = "11:30 AM",
                    icon = "🐾"
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

