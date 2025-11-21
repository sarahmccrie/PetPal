package week11.st830661.petpal.data.repository

import week11.st830661.petpal.data.models.Appointment
import week11.st830661.petpal.data.models.Reminder

class ReminderRepository {
    private val reminders = mutableListOf<Reminder>()
    private val appointments = mutableListOf<Appointment>()

    fun addReminder(reminder: Reminder): Boolean {
        return reminders.add(reminder)
    }

    fun removeReminder(reminderId: String): Boolean {
        return reminders.removeAll { it.id == reminderId }
    }

    fun updateReminder(reminder: Reminder): Boolean {
        val index = reminders.indexOfFirst { it.id == reminder.id }
        return if (index >= 0) {
            reminders[index] = reminder
            true
        } else {
            false
        }
    }

    fun getReminder(reminderId: String): Reminder? {
        return reminders.find { it.id == reminderId }
    }

    fun getAllReminders(): List<Reminder> {
        return reminders.toList()
    }

    fun getRemindersByPet(petName: String): List<Reminder> {
        return reminders.filter { it.petName == petName }
    }

    fun getActiveReminders(): List<Reminder> {
        return reminders.filter { it.isActive }
    }

    // Appointment methods
    fun addAppointment(appointment: Appointment): Boolean {
        return appointments.add(appointment)
    }

    fun removeAppointment(appointmentId: String): Boolean {
        return appointments.removeAll { it.id == appointmentId }
    }

    fun updateAppointment(appointment: Appointment): Boolean {
        val index = appointments.indexOfFirst { it.id == appointment.id }
        return if (index >= 0) {
            appointments[index] = appointment
            true
        } else {
            false
        }
    }

    fun getAppointment(appointmentId: String): Appointment? {
        return appointments.find { it.id == appointmentId }
    }

    fun getAllAppointments(): List<Appointment> {
        return appointments.toList()
    }

    fun getAppointmentsByPet(petName: String): List<Appointment> {
        return appointments.filter { it.petName == petName }
    }

    fun getUpcomingAppointments(): List<Appointment> {
        val now = java.time.LocalDateTime.now()
        return appointments.filter { it.dateTime.isAfter(now) }
            .sortedBy { it.dateTime }
    }
}
