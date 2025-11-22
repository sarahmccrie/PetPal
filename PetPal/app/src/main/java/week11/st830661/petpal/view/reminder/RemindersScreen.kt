package week11.st830661.petpal.view.reminder

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.launch
import week11.st830661.petpal.data.models.Appointment
import week11.st830661.petpal.data.models.Reminder
import week11.st830661.petpal.data.models.ReminderType
import week11.st830661.petpal.data.models.RecurrencePattern
import week11.st830661.petpal.data.models.AppointmentType
import week11.st830661.petpal.data.models.Pet
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar

@Composable
fun RemindersScreen(
    modifier: Modifier = Modifier,
    reminders: List<Reminder> = emptyList(),
    appointments: List<Appointment> = emptyList(),
    pets: List<Pet> = emptyList(),
    onReminderClick: (Reminder) -> Unit = {},
    onAppointmentClick: (Appointment) -> Unit = {},
    onAddReminder: (Reminder) -> Unit = {},
    onAddAppointment: (Appointment) -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(0) }
    var showAddReminderDialog by remember { mutableStateOf(false) }
    var showAddAppointmentDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (selectedTab == 0) {
                        showAddReminderDialog = true
                    } else {
                        showAddAppointmentDialog = true
                    }
                },
                containerColor = Color(0xFF7D9C3C)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Tab Row
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Reminders") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Appointments") }
                )
            }

            when (selectedTab) {
                0 -> RemindersTabContent(
                    reminders = reminders,
                    onReminderClick = onReminderClick
                )

                1 -> AppointmentsTabContent(
                    appointments = appointments,
                    onAppointmentClick = onAppointmentClick
                )
            }
        }
    }

    // Add Reminder Dialog
    if (showAddReminderDialog) {
        AddReminderDialog(
            pets = pets,
            onDismiss = { showAddReminderDialog = false },
            onSave = { reminder ->
                onAddReminder(reminder)
                showAddReminderDialog = false
            }
        )
    }

    // Add Appointment Dialog
    if (showAddAppointmentDialog) {
        AddAppointmentDialog(
            pets = pets,
            onDismiss = { showAddAppointmentDialog = false },
            onSave = { appointment ->
                onAddAppointment(appointment)
                showAddAppointmentDialog = false
            }
        )
    }
}

@Composable
fun RemindersTabContent(
    reminders: List<Reminder>,
    onReminderClick: (Reminder) -> Unit
) {
    if (reminders.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No reminders yet. Tap + to add one.",
                fontSize = 16.sp,
                color = Color.Gray
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(reminders) { reminder ->
                ReminderCard(
                    reminder = reminder,
                    onClick = { onReminderClick(reminder) }
                )
            }
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
fun AppointmentsTabContent(
    appointments: List<Appointment>,
    onAppointmentClick: (Appointment) -> Unit
) {
    if (appointments.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No appointments yet. Tap + to add one.",
                fontSize = 16.sp,
                color = Color.Gray
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(appointments) { appointment ->
                AppointmentCard(
                    appointment = appointment,
                    onClick = { onAppointmentClick(appointment) }
                )
            }
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
fun ReminderCard(
    reminder: Reminder,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF5F5F5))
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = reminder.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Text(
                        text = "${reminder.type} - ${reminder.petName}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF7D9C3C))
                        .padding(6.dp)
                ) {
                    Text(
                        text = if (reminder.isRecurring) "Recurring" else "Once",
                        fontSize = 10.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Time: ${reminder.time?.format(DateTimeFormatter.ofPattern("HH:mm")) ?: "Not set"}",
                fontSize = 11.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun AppointmentCard(
    appointment: Appointment,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFE8F5E9))
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = appointment.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Text(
                        text = "${appointment.vetName} - ${appointment.petName}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
                if (appointment.reminderSet) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF7D9C3C))
                            .padding(6.dp)
                    ) {
                        Text(
                            text = "🔔 Set",
                            fontSize = 10.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = appointment.dateTime.format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")),
                fontSize = 11.sp,
                color = Color.Gray
            )
            Text(
                text = appointment.clinicName,
                fontSize = 11.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun AddReminderDialog(
    pets: List<Pet> = emptyList(),
    onDismiss: () -> Unit,
    onSave: (Reminder) -> Unit
) {
    var petName by remember { mutableStateOf("") }
    var selectedPetId by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(ReminderType.FEEDING) }
    var reminderHour by remember { mutableStateOf(8) }
    var reminderMinute by remember { mutableStateOf(0) }
    var isRecurring by remember { mutableStateOf(false) }
    var selectedPattern by remember { mutableStateOf(RecurrencePattern.DAILY) }
    var reminderBeforeMinutes by remember { mutableStateOf("30") }
    var showTypeDropdown by remember { mutableStateOf(false) }
    var showPatternDropdown by remember { mutableStateOf(false) }
    var showPetDropdown by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Add Reminder",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Pet Name Dropdown
                if (pets.isNotEmpty()) {
                    Box {
                        OutlinedButton(
                            onClick = { showPetDropdown = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (petName.isNotEmpty()) "Pet: $petName" else "Select Pet")
                        }
                        DropdownMenu(
                            expanded = showPetDropdown,
                            onDismissRequest = { showPetDropdown = false }
                        ) {
                            pets.forEach { pet ->
                                DropdownMenuItem(
                                    text = { Text(pet.name) },
                                    onClick = {
                                        petName = pet.name
                                        selectedPetId = pet.id
                                        showPetDropdown = false
                                    }
                                )
                            }
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = petName,
                        onValueChange = { petName = it },
                        label = { Text("Pet Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Title
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Reminder Title") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Reminder Type
                Box {
                    OutlinedButton(
                        onClick = { showTypeDropdown = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Type: ${selectedType.name}")
                    }
                    DropdownMenu(
                        expanded = showTypeDropdown,
                        onDismissRequest = { showTypeDropdown = false }
                    ) {
                        ReminderType.entries.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.name) },
                                onClick = {
                                    selectedType = type
                                    showTypeDropdown = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Time Picker
                OutlinedButton(
                    onClick = {
                        TimePickerDialog(
                            context,
                            { _, hour: Int, minute: Int ->
                                reminderHour = hour
                                reminderMinute = minute
                            },
                            reminderHour,
                            reminderMinute,
                            true
                        ).show()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(String.format("Time: %02d:%02d", reminderHour, reminderMinute))
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Recurring Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isRecurring,
                        onCheckedChange = { isRecurring = it }
                    )
                    Text(
                        text = "Recurring Reminder",
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                if (isRecurring) {
                    Box {
                        OutlinedButton(
                            onClick = { showPatternDropdown = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Pattern: ${selectedPattern.name}")
                        }
                        DropdownMenu(
                            expanded = showPatternDropdown,
                            onDismissRequest = { showPatternDropdown = false }
                        ) {
                            RecurrencePattern.entries.forEach { pattern ->
                                DropdownMenuItem(
                                    text = { Text(pattern.name) },
                                    onClick = {
                                        selectedPattern = pattern
                                        showPatternDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Reminder before minutes
                OutlinedTextField(
                    value = reminderBeforeMinutes,
                    onValueChange = { reminderBeforeMinutes = it },
                    label = { Text("Remind before (minutes)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                if (errorMessage.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage,
                        color = Color.Red,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.padding(4.dp))
                    Button(
                        onClick = {
                            // Validation
                            errorMessage = ""
                            if (title.isBlank()) {
                                errorMessage = "Title is required"
                            } else if (petName.isBlank()) {
                                errorMessage = "Please select a pet"
                            } else if (reminderBeforeMinutes.toIntOrNull() == null) {
                                errorMessage = "Enter valid reminder time"
                            } else {
                                val reminder = Reminder(
                                    id = System.currentTimeMillis().toString(),
                                    petId = selectedPetId,
                                    petName = petName,
                                    title = title,
                                    description = description,
                                    type = selectedType,
                                    time = LocalTime.of(reminderHour, reminderMinute),
                                    isRecurring = isRecurring,
                                    recurrencePattern = selectedPattern,
                                    reminderTimeBeforeMinutes = reminderBeforeMinutes.toInt()
                                )
                                coroutineScope.launch {
                                    onSave(reminder)
                                }
                            }
                        }
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}

@Composable
fun AddAppointmentDialog(
    pets: List<Pet> = emptyList(),
    onDismiss: () -> Unit,
    onSave: (Appointment) -> Unit
) {
    var petName by remember { mutableStateOf("") }
    var selectedPetId by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(AppointmentType.VET_VISIT) }
    var vetName by remember { mutableStateOf("") }
    var clinicName by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var appointmentDate by remember { mutableStateOf(LocalDate.now()) }
    var appointmentHour by remember { mutableStateOf(14) }
    var appointmentMinute by remember { mutableStateOf(0) }
    var notes by remember { mutableStateOf("") }
    var reminderSet by remember { mutableStateOf(true) }
    var reminderBeforeMinutes by remember { mutableStateOf("30") }
    var showTypeDropdown by remember { mutableStateOf(false) }
    var showPetDropdown by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Log Appointment",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Pet Name Dropdown
                if (pets.isNotEmpty()) {
                    Box {
                        OutlinedButton(
                            onClick = { showPetDropdown = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (petName.isNotEmpty()) "Pet: $petName" else "Select Pet")
                        }
                        DropdownMenu(
                            expanded = showPetDropdown,
                            onDismissRequest = { showPetDropdown = false }
                        ) {
                            pets.forEach { pet ->
                                DropdownMenuItem(
                                    text = { Text(pet.name) },
                                    onClick = {
                                        petName = pet.name
                                        selectedPetId = pet.id
                                        showPetDropdown = false
                                    }
                                )
                            }
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = petName,
                        onValueChange = { petName = it },
                        label = { Text("Pet Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Appointment Type
                Box {
                    OutlinedButton(
                        onClick = { showTypeDropdown = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Type: ${selectedType.name.replace("_", " ")}")
                    }
                    DropdownMenu(
                        expanded = showTypeDropdown,
                        onDismissRequest = { showTypeDropdown = false }
                    ) {
                        AppointmentType.entries.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.name.replace("_", " ")) },
                                onClick = {
                                    selectedType = type
                                    showTypeDropdown = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Vet Name
                OutlinedTextField(
                    value = vetName,
                    onValueChange = { vetName = it },
                    label = { Text("Vet Name (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Clinic Name
                OutlinedTextField(
                    value = clinicName,
                    onValueChange = { clinicName = it },
                    label = { Text("Clinic Name (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Location
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Location/Address") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Date Picker
                OutlinedButton(
                    onClick = {
                        val calendar = Calendar.getInstance()
                        DatePickerDialog(
                            context,
                            { _, year: Int, month: Int, dayOfMonth: Int ->
                                appointmentDate = LocalDate.of(year, month + 1, dayOfMonth)
                            },
                            appointmentDate.year,
                            appointmentDate.monthValue - 1,
                            appointmentDate.dayOfMonth
                        ).show()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Date: ${DateTimeFormatter.ofPattern("MMM dd, yyyy").format(appointmentDate)}"
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Time Picker
                OutlinedButton(
                    onClick = {
                        TimePickerDialog(
                            context,
                            { _, hour: Int, minute: Int ->
                                appointmentHour = hour
                                appointmentMinute = minute
                            },
                            appointmentHour,
                            appointmentMinute,
                            true
                        ).show()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(String.format("Time: %02d:%02d", appointmentHour, appointmentMinute))
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Reminder Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = reminderSet,
                        onCheckedChange = { reminderSet = it }
                    )
                    Text(
                        text = "Set Reminder",
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                if (reminderSet) {
                    OutlinedTextField(
                        value = reminderBeforeMinutes,
                        onValueChange = { reminderBeforeMinutes = it },
                        label = { Text("Remind before (minutes)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (errorMessage.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage,
                        color = Color.Red,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.padding(4.dp))
                    Button(
                        onClick = {
                            // Validation
                            errorMessage = ""
                            if (petName.isBlank()) {
                                errorMessage = "Please select a pet"
                            } else {
                                val dateTime = appointmentDate.atTime(appointmentHour, appointmentMinute)

                                val appointment = Appointment(
                                    id = System.currentTimeMillis().toString(),
                                    petId = selectedPetId,
                                    petName = petName,
                                    type = selectedType,
                                    title = selectedType.name.replace("_", " "),
                                    vetName = vetName,
                                    clinicName = clinicName,
                                    location = location,
                                    dateTime = dateTime,
                                    notes = notes,
                                    reminderSet = reminderSet,
                                    reminderTimeBeforeMinutes = reminderBeforeMinutes.toIntOrNull() ?: 30
                                )
                                coroutineScope.launch {
                                    onSave(appointment)
                                }
                            }
                        }
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}
