package week11.st830661.petpal.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import week11.st830661.petpal.workers.ReminderWorker

object NotificationHelper {
    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Create reminder notification channel
            val reminderChannel = NotificationChannel(
                ReminderWorker.CHANNEL_ID,
                "Pet Care Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminders for pet appointments, feeding, medication, and care"
                enableVibration(true)
                enableLights(true)
            }

            notificationManager.createNotificationChannel(reminderChannel)
        }
    }
}
