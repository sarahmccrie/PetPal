package week11.st830661.petpal.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import week11.st830661.petpal.data.models.Appointment
import week11.st830661.petpal.data.models.Reminder
import week11.st830661.petpal.data.repository.FirestoreReminderRepository

class ReminderViewModel(
    private val repository: FirestoreReminderRepository
) : ViewModel() {

    // Real-time flows for Firestore data
    val reminders: Flow<List<Reminder>> = repository.getRemindersStream()
    val appointments: Flow<List<Appointment>> = repository.getAppointmentsStream()

    suspend fun addReminder(reminder: Reminder) {
        repository.addReminder(reminder)
    }

    fun deleteReminder(reminderId: String) {
        // Call suspend function in a coroutine context
        kotlinx.coroutines.GlobalScope.launch {
            repository.deleteReminder(reminderId)
        }
    }

    fun updateReminder(reminder: Reminder) {
        kotlinx.coroutines.GlobalScope.launch {
            repository.updateReminder(reminder)
        }
    }

    suspend fun addAppointment(appointment: Appointment) {
        repository.addAppointment(appointment)
    }

    fun deleteAppointment(appointmentId: String) {
        kotlinx.coroutines.GlobalScope.launch {
            repository.deleteAppointment(appointmentId)
        }
    }

    fun updateAppointment(appointment: Appointment) {
        kotlinx.coroutines.GlobalScope.launch {
            repository.updateAppointment(appointment)
        }
    }
}

class ReminderViewModelFactory(private val userId: String, private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ReminderViewModel::class.java)) {
            val repository = FirestoreReminderRepository(userId, context)
            return ReminderViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
