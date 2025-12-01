package week11.st830661.petpal.data.models

import com.google.android.gms.maps.model.LatLng
import java.time.LocalDateTime
import java.time.LocalTime

data class Reminder(
    val id: String = "",
    val petId: String = "",
    val petName: String = "",
    val type: ReminderType = ReminderType.FEEDING,
    val title: String = "",
    val description: String = "",
    val dateTime: LocalDateTime? = null,
    val time: LocalTime? = null,
    val isRecurring: Boolean = false,
    val recurrencePattern: RecurrencePattern = RecurrencePattern.DAILY,
    val reminderTimeBeforeMinutes: Int = 30,
    val isActive: Boolean = true,
    val createdAt: LocalDateTime = LocalDateTime.now()
)

enum class ReminderType {
    FEEDING,
    MEDICATION,
    GROOMING,
    VET_APPOINTMENT,
    VACCINATION,
    CARE,
    OTHER
}

enum class RecurrencePattern {
    DAILY,
    WEEKLY,
    MONTHLY,
    ONCE
}

enum class AppointmentType {
    VET_VISIT,
    GROOMING,
    DENTAL,
    VACCINATION,
    CHECKUP,
    SURGERY,
    OTHER
}

data class Appointment(
    val id: String = "",
    val petId: String = "",
    val petName: String = "",
    val type: AppointmentType = AppointmentType.VET_VISIT,
    val title: String = "",
    val vetName: String = "",
    val clinicName: String = "",
//    val location : String = "",
    val locationName: String = "",
    val locationAddress: String = "",
    val locationCoords : LatLng = LatLng(0.0, 0.0),
    val dateTime: LocalDateTime = LocalDateTime.now(),
    val notes: String = "",
    val reminderSet: Boolean = false,
    val reminderTimeBeforeMinutes: Int = 30,
    val createdAt: LocalDateTime = LocalDateTime.now()
)

