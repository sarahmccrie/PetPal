package week11.st830661.petpal.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import android.util.Log
import week11.st830661.petpal.data.models.Appointment
import week11.st830661.petpal.data.models.Reminder
import week11.st830661.petpal.data.models.AppointmentType
import week11.st830661.petpal.data.models.RecurrencePattern
import week11.st830661.petpal.data.models.ReminderType
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import android.content.Context
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.GeoPoint
import week11.st830661.petpal.utils.ReminderScheduler

private const val TAG = "FirestoreReminderRepository"

class FirestoreReminderRepository(private val userId: String, private val context: Context) {
    private val db = FirebaseFirestore.getInstance()
    private val remindersCollection = db.collection("users").document(userId).collection("reminders")
    private val appointmentsCollection = db.collection("users").document(userId).collection("appointments")
    private val dateTimeFormatter = DateTimeFormatter.ISO_DATE_TIME
    private val timeFormatter = DateTimeFormatter.ISO_TIME
    private val reminderScheduler = ReminderScheduler(context)

    // Real-time reminders stream
    fun getRemindersStream(): Flow<List<Reminder>> = callbackFlow {
        val listener = remindersCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            val reminders = snapshot?.documents?.mapNotNull { doc ->
                try {
                    val data = doc.data ?: return@mapNotNull null
                    val reminder = Reminder(
                        id = doc.id,
                        petId = data["petId"] as? String ?: "",
                        petName = data["petName"] as? String ?: "",
                        type = try { ReminderType.valueOf(data["type"] as? String ?: "FEEDING") } catch (e: Exception) { ReminderType.FEEDING },
                        title = data["title"] as? String ?: "",
                        description = data["description"] as? String ?: "",
                        dateTime = try { LocalDateTime.parse(data["dateTime"] as? String ?: "", dateTimeFormatter) } catch (e: Exception) { null },
                        time = try { LocalTime.parse(data["time"] as? String ?: "", timeFormatter) } catch (e: Exception) { null },
                        isRecurring = data["isRecurring"] as? Boolean ?: false,
                        recurrencePattern = try { RecurrencePattern.valueOf(data["recurrencePattern"] as? String ?: "DAILY") } catch (e: Exception) { RecurrencePattern.DAILY },
                        reminderTimeBeforeMinutes = (data["reminderTimeBeforeMinutes"] as? Number)?.toInt() ?: 30,
                        isActive = data["isActive"] as? Boolean ?: true,
                        createdAt = try { LocalDateTime.parse(data["createdAt"] as? String ?: "", dateTimeFormatter) } catch (e: Exception) { LocalDateTime.now() }
                    )
                    reminder
                } catch (e: Exception) {
                    null
                }
            } ?: emptyList()

            trySend(reminders)
        }

        awaitClose { listener.remove() }
    }

    // Real-time appointments stream
    fun getAppointmentsStream(): Flow<List<Appointment>> = callbackFlow {
        val listener = appointmentsCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            val appointments = snapshot?.documents?.mapNotNull { doc ->
                try {
                    val data = doc.data ?: return@mapNotNull null
                    val appointment = Appointment(
                        id = doc.id,
                        petId = data["petId"] as? String ?: "",
                        petName = data["petName"] as? String ?: "",
                        type = try { AppointmentType.valueOf(data["type"] as? String ?: "VET_VISIT") } catch (e: Exception) { AppointmentType.VET_VISIT },
                        title = data["title"] as? String ?: "",
                        vetName = data["vetName"] as? String ?: "",
                        clinicName = data["clinicName"] as? String ?: "",
                        locationName = data["locationName"] as? String ?: "",
                        locationAddress = data["locationAddress"] as? String ?: "",
                        locationCoords = data["locationCoords"] as? GeoPoint ?: GeoPoint(0.0, 0.0),
                        dateTime = try { LocalDateTime.parse(data["dateTime"] as? String ?: "", dateTimeFormatter) } catch (e: Exception) { LocalDateTime.now() },
                        notes = data["notes"] as? String ?: "",
                        reminderSet = data["reminderSet"] as? Boolean ?: false,
                        reminderTimeBeforeMinutes = (data["reminderTimeBeforeMinutes"] as? Number)?.toInt() ?: 30,
                        createdAt = try { LocalDateTime.parse(data["createdAt"] as? String ?: "", dateTimeFormatter) } catch (e: Exception) { LocalDateTime.now() }
                    )
                    appointment
                } catch (e: Exception) {
                    null
                }
            } ?: emptyList()

            trySend(appointments)
        }

        awaitClose { listener.remove() }
    }

    suspend fun addReminder(reminder: Reminder) {
        Log.d(TAG, "=== addReminder called ===")
        Log.d(TAG, "Reminder ID: ${reminder.id}")
        Log.d(TAG, "Title: ${reminder.title}")
        Log.d(TAG, "Time: ${reminder.time}")
        Log.d(TAG, "Is Active: ${reminder.isActive}")

        try {
            val data = reminder.toFirestoreMap()
            Log.d(TAG, "Saving to Firestore...")
            remindersCollection.document(reminder.id).set(data).await()
            Log.d(TAG, "Saved to Firestore successfully")

            // Schedule the reminder notification
            Log.d(TAG, "Calling ReminderScheduler.scheduleReminder()...")
            reminderScheduler.scheduleReminder(reminder)
            Log.d(TAG, "ReminderScheduler.scheduleReminder() completed")
        } catch (e: Exception) {
            Log.e(TAG, "Error in addReminder", e)
            throw e
        }
    }

    suspend fun updateReminder(reminder: Reminder) {
        Log.d(TAG, "=== updateReminder called ===")
        Log.d(TAG, "Reminder ID: ${reminder.id}")
        Log.d(TAG, "Title: ${reminder.title}")

        try {
            val data = reminder.toFirestoreMap()
            Log.d(TAG, "Updating in Firestore...")
            remindersCollection.document(reminder.id).set(data).await()
            Log.d(TAG, "Updated in Firestore successfully")

            // Re-schedule the reminder notification
            Log.d(TAG, "Cancelling old schedule...")
            reminderScheduler.cancelReminder(reminder.id)
            Log.d(TAG, "Scheduling new reminder...")
            reminderScheduler.scheduleReminder(reminder)
            Log.d(TAG, "Re-scheduled successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error in updateReminder", e)
            throw e
        }
    }

    suspend fun deleteReminder(reminderId: String) {
        Log.d(TAG, "=== deleteReminder called ===")
        Log.d(TAG, "Reminder ID: $reminderId")

        try {
            Log.d(TAG, "Deleting from Firestore...")
            remindersCollection.document(reminderId).delete().await()
            Log.d(TAG, "Deleted from Firestore successfully")

            // Cancel the scheduled reminder notification
            Log.d(TAG, "Cancelling scheduled reminder...")
            reminderScheduler.cancelReminder(reminderId)
            Log.d(TAG, "Reminder cancelled successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error in deleteReminder", e)
            throw e
        }
    }

    suspend fun addAppointment(appointment: Appointment) {
        Log.d(TAG, "=== addAppointment called ===")
        Log.d(TAG, "Appointment ID: ${appointment.id}")
        Log.d(TAG, "Title: ${appointment.title}")
        Log.d(TAG, "DateTime: ${appointment.dateTime}")
        Log.d(TAG, "Reminder Set: ${appointment.reminderSet}")

        try {
            val data = appointment.toFirestoreMap()
            Log.d(TAG, "Saving to Firestore...")
            appointmentsCollection.document(appointment.id).set(data).await()
            Log.d(TAG, "Saved to Firestore successfully")

            // Schedule appointment reminder if enabled
            if (appointment.reminderSet) {
                Log.d(TAG, "Calling ReminderScheduler.scheduleAppointmentReminder()...")
                reminderScheduler.scheduleAppointmentReminder(appointment)
                Log.d(TAG, "ReminderScheduler.scheduleAppointmentReminder() completed")
            } else {
                Log.d(TAG, "Reminder not set for this appointment, skipping scheduler")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in addAppointment", e)
            throw e
        }
    }

    suspend fun updateAppointment(appointment: Appointment) {
        Log.d(TAG, "=== updateAppointment called ===")
        Log.d(TAG, "Appointment ID: ${appointment.id}")
        Log.d(TAG, "Title: ${appointment.title}")

        try {
            val data = appointment.toFirestoreMap()
            Log.d(TAG, "Updating in Firestore...")
            appointmentsCollection.document(appointment.id).set(data).await()
            Log.d(TAG, "Updated in Firestore successfully")

            // Re-schedule the appointment reminder if enabled
            if (appointment.reminderSet) {
                Log.d(TAG, "Cancelling old schedule...")
                reminderScheduler.cancelAppointmentReminder(appointment.id)
                Log.d(TAG, "Scheduling new appointment reminder...")
                reminderScheduler.scheduleAppointmentReminder(appointment)
                Log.d(TAG, "Re-scheduled successfully")
            } else {
                Log.d(TAG, "Reminder not set, cancelling any existing schedule...")
                reminderScheduler.cancelAppointmentReminder(appointment.id)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in updateAppointment", e)
            throw e
        }
    }

    suspend fun deleteAppointment(appointmentId: String) {
        Log.d(TAG, "=== deleteAppointment called ===")
        Log.d(TAG, "Appointment ID: $appointmentId")

        try {
            Log.d(TAG, "Deleting from Firestore...")
            appointmentsCollection.document(appointmentId).delete().await()
            Log.d(TAG, "Deleted from Firestore successfully")

            // Cancel the scheduled appointment reminder notification
            Log.d(TAG, "Cancelling scheduled reminder...")
            reminderScheduler.cancelAppointmentReminder(appointmentId)
            Log.d(TAG, "Appointment reminder cancelled successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error in deleteAppointment", e)
            throw e
        }
    }

    suspend fun getReminder(reminderId: String): Reminder? {
        return try {
            remindersCollection.document(reminderId).get().addOnSuccessListener { document ->
                document.toObject<Reminder>()
            }.await() as? Reminder
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getAppointment(appointmentId: String): Appointment? {
        return try {
            appointmentsCollection.document(appointmentId).get().addOnSuccessListener { document ->
                document.toObject<Appointment>()
            }.await() as? Appointment
        } catch (e: Exception) {
            null
        }
    }

    private fun Reminder.toFirestoreMap(): Map<String, Any?> = mapOf(
        "petId" to petId,
        "petName" to petName,
        "type" to type.name,
        "title" to title,
        "description" to description,
        "dateTime" to dateTime?.format(dateTimeFormatter),
        "time" to time?.format(timeFormatter),
        "isRecurring" to isRecurring,
        "recurrencePattern" to recurrencePattern.name,
        "reminderTimeBeforeMinutes" to reminderTimeBeforeMinutes,
        "isActive" to isActive,
        "createdAt" to createdAt.format(dateTimeFormatter)
    )

    private fun Appointment.toFirestoreMap(): Map<String, Any?> = mapOf(
        "petId" to petId,
        "petName" to petName,
        "type" to type.name,
        "title" to title,
        "vetName" to vetName,
        "clinicName" to clinicName,
//        "location" to location,
        "locationName" to locationName,
        "locationAddress" to locationAddress,
        "locationCoords" to locationCoords,
        "dateTime" to dateTime.format(dateTimeFormatter),
        "notes" to notes,
        "reminderSet" to reminderSet,
        "reminderTimeBeforeMinutes" to reminderTimeBeforeMinutes,
        "createdAt" to createdAt.format(dateTimeFormatter)
    )
}

// Extension function for converting Task to suspend
private suspend fun <T> com.google.android.gms.tasks.Task<T>.await(): T {
    return kotlinx.coroutines.suspendCancellableCoroutine { continuation ->
        addOnSuccessListener { result ->
            continuation.resume(result)
        }
        addOnFailureListener { exception ->
            continuation.resumeWithException(exception)
        }
    }
}
