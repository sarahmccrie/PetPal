package week11.st830661.petpal.view.reminder

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.util.Log
import android.widget.DatePicker
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.MarkerState.Companion.invoke
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch
import week11.st830661.petpal.data.models.Appointment
import week11.st830661.petpal.data.models.Reminder
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import week11.st830661.petpal.data.models.AppointmentType
import week11.st830661.petpal.data.models.RecurrencePattern
import week11.st830661.petpal.view.mapIntegration.FindAVet

@Composable
fun ReminderDetailScreen(
    reminder: Reminder,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {},
    onUpdateReminder: (Reminder) -> Unit = {}
) {
    var isEditing by remember { mutableStateOf(false) }
    var editedReminder by remember { mutableStateOf(reminder) }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Text(
                    text = if (isEditing) "Edit Reminder" else "Reminder Details",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Top
        ) {
            if (!isEditing) {
                // Display Mode
                Text(
                    text = editedReminder.title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = "Pet",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = editedReminder.petName,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Text(
                    text = "Type",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = editedReminder.type.name,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Text(
                    text = "Time",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = editedReminder.time?.format(DateTimeFormatter.ofPattern("HH:mm")) ?: "Not set",
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Text(
                    text = "Frequency",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = if (editedReminder.isRecurring) "Recurring (${editedReminder.recurrencePattern.name})" else "One-time",
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Text(
                    text = "Reminder Before",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = "${editedReminder.reminderTimeBeforeMinutes} minutes",
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                if (editedReminder.description.isNotEmpty()) {
                    Text(
                        text = "Description",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        text = editedReminder.description,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                Text(
                    text = "Status",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = if (editedReminder.isActive) "Active" else "Inactive",
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { isEditing = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Edit")
                    }
                    Button(
                        onClick = onDeleteClick,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Delete")
                    }
                }
            } else {
                // Edit Mode
                var errorMessage by remember { mutableStateOf("") }

                OutlinedTextField(
                    value = editedReminder.title,
                    onValueChange = { editedReminder = editedReminder.copy(title = it) },
                    label = { Text("Reminder Title") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = editedReminder.petName,
                    onValueChange = { editedReminder = editedReminder.copy(petName = it) },
                    label = { Text("Pet Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Date Picker
                OutlinedButton(
                    onClick = {
                        val calendar = Calendar.getInstance()
                        val datePickerDialog = DatePickerDialog(
                            context,
                            { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
                                val selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
                                editedReminder = editedReminder.copy(
                                    dateTime = selectedDate.atTime(editedReminder.time ?: LocalTime.now())
                                )
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                        )
                        datePickerDialog.show()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = editedReminder.dateTime?.let {
                            "Date: ${DateTimeFormatter.ofPattern("MMM dd, yyyy").format(it)}"
                        } ?: "Select Date"
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Time Picker
                OutlinedButton(
                    onClick = {
                        val calendar = Calendar.getInstance()
                        val hour = editedReminder.time?.hour ?: calendar.get(Calendar.HOUR_OF_DAY)
                        val minute = editedReminder.time?.minute ?: calendar.get(Calendar.MINUTE)

                        val timePickerDialog = TimePickerDialog(
                            context,
                            { _, selectedHour: Int, selectedMinute: Int ->
                                val selectedTime = LocalTime.of(selectedHour, selectedMinute)
                                editedReminder = editedReminder.copy(time = selectedTime)
                            },
                            hour,
                            minute,
                            true
                        )
                        timePickerDialog.show()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = editedReminder.time?.let {
                            "Time: ${DateTimeFormatter.ofPattern("HH:mm").format(it)}"
                        } ?: "Select Time"
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = editedReminder.description,
                    onValueChange = { editedReminder = editedReminder.copy(description = it) },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Recurring Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = editedReminder.isRecurring,
                        onCheckedChange = { isRecurring ->
                            editedReminder = editedReminder.copy(isRecurring = isRecurring)
                        }
                    )
                    Text(
                        text = "Recurring Reminder",
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                if (editedReminder.isRecurring) {
                    var showPatternDropdown by remember { mutableStateOf(false) }

                    Spacer(modifier = Modifier.height(8.dp))

                    Box {
                        OutlinedButton(
                            onClick = { showPatternDropdown = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Pattern: ${editedReminder.recurrencePattern.name}")
                        }
                        DropdownMenu(
                            expanded = showPatternDropdown,
                            onDismissRequest = { showPatternDropdown = false }
                        ) {
                            RecurrencePattern.entries.forEach { pattern ->
                                DropdownMenuItem(
                                    text = { Text(pattern.name) },
                                    onClick = {
                                        editedReminder = editedReminder.copy(recurrencePattern = pattern)
                                        showPatternDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { isEditing = false },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            errorMessage = ""
                            if (editedReminder.time == null) {
                                errorMessage = "Time is mandatory"
                            } else {
                                onUpdateReminder(editedReminder)
                                isEditing = false
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Save")
                    }
                }

                if (errorMessage.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = errorMessage,
                        color = Color.Red,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
fun AppointmentDetailScreen(
    appointment: Appointment,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {},
    onUpdateAppointment: (Appointment) -> Unit = {}
) {
    var isEditing by remember { mutableStateOf(false) }
    var editedAppointment by remember { mutableStateOf(appointment) }
    var viewMap by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(editedAppointment.locationCoords, 12f)
    }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Text(
                    text = if (isEditing) "Edit Appointment" else "Appointment Details",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Top
        ) {
            if (!isEditing) {
                // Display Mode
                Text(
                    text = editedAppointment.title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = "Pet",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = editedAppointment.petName,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Text(
                    text = "Type",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = editedAppointment.type.name.replace("_", " "),
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Text(
                    text = "Veterinarian",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = editedAppointment.vetName,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Text(
                    text = "Clinic",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = editedAppointment.clinicName,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Text(
                    text = "Location",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = "${editedAppointment.clinicName}, ${editedAppointment.locationAddress}",
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

//                scope.launch {
//                    cameraPositionState.animate(
//                        update = CameraUpdateFactory.newLatLngZoom(
//                            editedAppointment.locationCoords,
//                            12f
//                        ),
//                        durationMs = 1000
//                    )
//                }
//                Box(modifier = Modifier.fillMaxWidth()
//                    .weight(1f)
//                    .clip(RoundedCornerShape(16.dp))) {
//                    // Google Map
//                    GoogleMap(
//                        modifier = Modifier.fillMaxSize(),
//                        cameraPositionState = cameraPositionState,
//                        properties = MapProperties(isMyLocationEnabled = false),
//                        uiSettings = MapUiSettings(zoomControlsEnabled = true)
//                    ) {
//                        editedAppointment.locationCoords.let { position ->
//                            Marker(
//                                state = MarkerState(position = position),
//                                title = "Selected Location"
//                            )
//                        }
//                    }
//                }

                Text(
                    text = "Date & Time",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = editedAppointment.dateTime.format(DateTimeFormatter.ofPattern("MMMM dd, yyyy HH:mm")),
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                if (editedAppointment.notes.isNotEmpty()) {
                    Text(
                        text = "Notes",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        text = editedAppointment.notes,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                Text(
                    text = "Reminder",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = if (editedAppointment.reminderSet) "Set (${editedAppointment.reminderTimeBeforeMinutes} min before)" else "Not set",
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { isEditing = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Edit")
                    }
                    Button(
                        onClick = onDeleteClick,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Delete")
                    }
                }
            } else {
                // Edit Mode
                var showTypeDropdown by remember { mutableStateOf(false) }

                OutlinedTextField(
                    value = editedAppointment.title,
                    onValueChange = { editedAppointment = editedAppointment.copy(title = it) },
                    label = { Text("Appointment Title") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = editedAppointment.petName,
                    onValueChange = { editedAppointment = editedAppointment.copy(petName = it) },
                    label = { Text("Pet Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Appointment Type Dropdown
                Box {
                    OutlinedButton(
                        onClick = { showTypeDropdown = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Type: ${editedAppointment.type.name.replace("_", " ")}")
                    }
                    DropdownMenu(
                        expanded = showTypeDropdown,
                        onDismissRequest = { showTypeDropdown = false }
                    ) {
                        AppointmentType.entries.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.name.replace("_", " ")) },
                                onClick = {
                                    editedAppointment = editedAppointment.copy(type = type, title = type.name.replace("_", " "))
                                    showTypeDropdown = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = editedAppointment.vetName,
                    onValueChange = { editedAppointment = editedAppointment.copy(vetName = it) },
                    label = { Text("Vet Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = editedAppointment.clinicName,
                    onValueChange = { editedAppointment = editedAppointment.copy(clinicName = it) },
                    label = { Text("Clinic Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Location",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                Text(
                    text = "${editedAppointment.clinicName}, ${editedAppointment.locationAddress}",
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Button(
                    onClick = {
                        viewMap = true
                    }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Outlined.LocationOn, contentDescription = "Pin")
                        Text("Find a Vet Nearby")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Date Picker
                OutlinedButton(
                    onClick = {
                        val calendar = Calendar.getInstance()
                        val datePickerDialog = DatePickerDialog(
                            context,
                            { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
                                val selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
                                editedAppointment = editedAppointment.copy(
                                    dateTime = selectedDate.atTime(editedAppointment.dateTime.toLocalTime())
                                )
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                        )
                        datePickerDialog.show()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Date: ${DateTimeFormatter.ofPattern("MMM dd, yyyy").format(editedAppointment.dateTime)}"
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Time Picker
                OutlinedButton(
                    onClick = {
                        val hour = editedAppointment.dateTime.hour
                        val minute = editedAppointment.dateTime.minute

                        val timePickerDialog = TimePickerDialog(
                            context,
                            { _, selectedHour: Int, selectedMinute: Int ->
                                val newDateTime = editedAppointment.dateTime
                                    .withHour(selectedHour)
                                    .withMinute(selectedMinute)
                                editedAppointment = editedAppointment.copy(dateTime = newDateTime)
                            },
                            hour,
                            minute,
                            true
                        )
                        timePickerDialog.show()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Time: ${DateTimeFormatter.ofPattern("HH:mm").format(editedAppointment.dateTime)}"
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { isEditing = false },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            onUpdateAppointment(editedAppointment)
                            isEditing = false
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Save")
                    }
                    if (viewMap) {
                        Dialog(onDismissRequest = { viewMap = false }) {
                            FindAVet(OnNavigate = { locationDetails ->
                                editedAppointment = editedAppointment.copy(
                                    locationName = locationDetails.clinicName,
                                    locationCoords = locationDetails.coords,
                                    locationAddress = locationDetails.address)
                                viewMap = false
                            })
                        }
                    }
                }
            }
        }
    }
}
