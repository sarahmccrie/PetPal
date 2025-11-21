package week11.st830661.petpal.workers

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationCompat
import week11.st830661.petpal.MainActivity

private const val TAG = "ReminderWorker"

class ReminderWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    override fun doWork(): Result {
        Log.d(TAG, "=== WORKER STARTED ===")
        return try {
            val reminderTitle = inputData.getString("reminder_title") ?: "Reminder"
            val reminderDescription = inputData.getString("reminder_description") ?: ""
            val petName = inputData.getString("pet_name") ?: "Your Pet"
            val reminderId = inputData.getString("reminder_id") ?: ""
            val isBeforeReminder = inputData.getBoolean("is_before_reminder", false)

            Log.d(TAG, "Reminder ID: $reminderId")
            Log.d(TAG, "Title: $reminderTitle")
            Log.d(TAG, "Description: $reminderDescription")
            Log.d(TAG, "Pet Name: $petName")
            Log.d(TAG, "Is Before Reminder: $isBeforeReminder")

            // Show notification
            Log.d(TAG, "Showing notification...")
            showReminderNotification(reminderTitle, reminderDescription, petName, reminderId, isBeforeReminder)

            Log.d(TAG, "Notification shown successfully")
            Log.d(TAG, "=== WORKER COMPLETED SUCCESSFULLY ===")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error in ReminderWorker", e)
            e.printStackTrace()
            Log.d(TAG, "=== WORKER FAILED - RETRYING ===")
            Result.retry()
        }
    }

    private fun showReminderNotification(
        title: String,
        description: String,
        petName: String,
        reminderId: String,
        isBeforeReminder: Boolean = false
    ) {
        Log.d(TAG, "--- showReminderNotification START ---")
        Log.d(TAG, "Is Before Reminder: $isBeforeReminder")

        val context = applicationContext
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        Log.d(TAG, "Got notification manager")

        // Create intent to open the app
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("reminder_id", reminderId)
        }

        Log.d(TAG, "Created intent")

        val pendingIntent = PendingIntent.getActivity(
            context,
            reminderId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        Log.d(TAG, "Created pending intent")

        val notificationText = if (description.isNotEmpty()) {
            "$petName - $description"
        } else {
            petName
        }

        Log.d(TAG, "Notification text: $notificationText")

        // Use different notification IDs for 'before' vs 'exact time' to allow both to show
        val notificationId = if (isBeforeReminder) {
            reminderId.hashCode() // Original ID for 'before' notification
        } else {
            reminderId.hashCode() + 10000 // Different ID for 'exact time' notification
        }

        Log.d(TAG, "Notification ID: $notificationId (is_before_reminder: $isBeforeReminder)")

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(notificationText)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVibrate(longArrayOf(0, 500, 250, 500)) // Add vibration
            .setSound(android.provider.Settings.System.DEFAULT_NOTIFICATION_URI) // Add sound
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()

        Log.d(TAG, "Notification object created")

        Log.d(TAG, "Notifying with ID: $notificationId, Channel ID: $CHANNEL_ID")
        notificationManager.notify(notificationId, notification)

        Log.d(TAG, "Notification posted")
        Log.d(TAG, "--- showReminderNotification END ---")
    }

    companion object {
        const val CHANNEL_ID = "petpal_reminders"
        const val UNIQUE_WORK_NAME_PREFIX = "reminder_"
    }
}
