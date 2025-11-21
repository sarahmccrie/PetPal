package week11.st830661.petpal.viewmodel

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import week11.st830661.petpal.data.models.Appointment
import week11.st830661.petpal.data.models.Reminder
import week11.st830661.petpal.data.models.AppointmentType
import java.time.format.DateTimeFormatter

@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    reminders: List<Reminder> = emptyList(),
    appointments: List<Appointment> = emptyList(),
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
                    color = Color(0xFF00CCC5),
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
                        color = Color.Gray,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                } else {
                    // Display upcoming reminders
                    upcomingReminders.forEach { reminder ->
                        val petEmoji = if (reminder.petName.contains("Max", ignoreCase = true)) "🐕" else "🐈"
                        VaccinationCard(
                            title = "${reminder.type.name} - ${reminder.petName}",
                            dueLabel = reminder.title,
                            dueInfo = reminder.time?.let {
                                DateTimeFormatter.ofPattern("HH:mm").format(it)
                            } ?: "Not scheduled",
                            backgroundColor = Color(0xFFF5E6D3),
                            petImage = petEmoji,
                            onClick = { onReminderClick(reminder) }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Display upcoming appointments
                    upcomingAppointments.forEach { appointment ->
                        val petEmoji = if (appointment.petName.contains("Max", ignoreCase = true)) "🐕" else "🐈"
                        VaccinationCard(
                            title = "${appointment.title} - ${appointment.petName}",
                            dueLabel = appointment.vetName,
                            dueInfo = DateTimeFormatter.ofPattern("MMM dd").format(appointment.dateTime),
                            backgroundColor = Color(0xFFF5E6D3),
                            petImage = petEmoji,
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
                        color = Color.Gray,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                } else {
                    vetVisits.forEach { appointment ->
                        val petEmoji = if (appointment.petName.contains("Max", ignoreCase = true)) "🐕" else "🐈"
                        VetVisitCard(
                            title = "${appointment.petName} - ${appointment.type.name.replace("_", " ")}",
                            nextVisit = "Next visit on ${DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm").format(appointment.dateTime)}",
                            vetName = appointment.vetName,
                            backgroundColor = Color(0xFFF5E6D3),
                            petImage = petEmoji,
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

@Composable
fun VaccinationCard(
    title: String,
    dueLabel: String,
    dueInfo: String,
    backgroundColor: Color,
    petImage: String,
    reminder: Reminder? = null,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = dueLabel,
                fontSize = 12.sp,
                color = Color(0xFF7D9C3C)
            )
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = if (backgroundColor == Color(0xFF2C2C2C)) Color.White else Color.Black
            )
            Text(
                text = dueInfo,
                fontSize = 12.sp,
                color = Color(0xFF7D9C3C)
            )
        }

        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(backgroundColor.copy(alpha = 0.7f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = petImage,
                fontSize = 48.sp
            )
        }
    }
}

@Composable
fun VetVisitCard(
    title: String,
    nextVisit: String,
    vetName: String = "",
    backgroundColor: Color,
    petImage: String,
    appointment: Appointment? = null,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            if (vetName.isNotEmpty()) {
                Text(
                    text = "Vet: $vetName",
                    fontSize = 12.sp,
                    color = Color(0xFF7D9C3C)
                )
            }
            Text(
                text = nextVisit,
                fontSize = 12.sp,
                color = Color(0xFF7D9C3C)
            )
        }

        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(backgroundColor.copy(alpha = 0.7f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = petImage,
                fontSize = 48.sp
            )
        }
    }
}

@Composable
fun ActivityLogItem(
    title: String,
    time: String,
    icon: String,
    reminder: Reminder? = null,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFF0F0F0)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = icon,
                fontSize = 24.sp
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
            Text(
                text = time,
                fontSize = 12.sp,
                color = Color(0xFF999999)
            )
        }
    }
}
