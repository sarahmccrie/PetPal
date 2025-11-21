package week11.st830661.petpal.utils

import android.content.Context
import android.util.Log
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.OneTimeWorkRequestBuilder
import week11.st830661.petpal.data.models.Reminder
import week11.st830661.petpal.data.models.RecurrencePattern
import week11.st830661.petpal.workers.ReminderWorker
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.Duration
import java.util.concurrent.TimeUnit

private const val TAG = "ReminderScheduler"

class ReminderScheduler(private val context: Context) {

    private val workManager = WorkManager.getInstance(context)

    fun scheduleReminder(reminder: Reminder) {
        Log.d(TAG, "=== SCHEDULE REMINDER START ===")
        Log.d(TAG, "Reminder ID: ${reminder.id}")
        Log.d(TAG, "Title: ${reminder.title}")
        Log.d(TAG, "Pet Name: ${reminder.petName}")
        Log.d(TAG, "Time: ${reminder.time}")
        Log.d(TAG, "Is Active: ${reminder.isActive}")
        Log.d(TAG, "Is Recurring: ${reminder.isRecurring}")
        Log.d(TAG, "Recurring Pattern: ${reminder.recurrencePattern}")
        Log.d(TAG, "Minutes Before: ${reminder.reminderTimeBeforeMinutes}")

        // Check if reminder should be scheduled
        if (!reminder.isActive) {
            Log.w(TAG, "Reminder is not active, skipping scheduling")
            return
        }

        if (reminder.time == null) {
            Log.w(TAG, "Reminder time is null, skipping scheduling")
            return
        }

        val inputData = Data.Builder()
            .putString("reminder_id", reminder.id)
            .putString("reminder_title", reminder.title)
            .putString("reminder_description", reminder.description)
            .putString("pet_name", reminder.petName)
            .build()

        Log.d(TAG, "Input data built successfully")

        if (reminder.isRecurring) {
            Log.d(TAG, "Scheduling as RECURRING reminder")
            scheduleRecurringReminder(reminder, inputData)
        } else {
            Log.d(TAG, "Scheduling as ONE-TIME reminder")
            scheduleOneTimeReminder(reminder, inputData)
        }

        Log.d(TAG, "=== SCHEDULE REMINDER END ===")
    }

    private fun scheduleOneTimeReminder(reminder: Reminder, inputData: Data) {
        Log.d(TAG, "--- scheduleOneTimeReminder START ---")
        val delayInMinutes = calculateDelayToReminderTime(
            reminder.time ?: LocalTime.now(),
            reminder.reminderTimeBeforeMinutes
        )

        Log.d(TAG, "Calculated delay (for 'before' notification): $delayInMinutes minutes")
        Log.d(TAG, "Current time: ${LocalDateTime.now()}")
        Log.d(TAG, "'Before' notification will trigger at: ${LocalDateTime.now().plusMinutes(delayInMinutes)}")

        // Schedule the "before" notification
        val beforeInputData = Data.Builder()
            .putString("reminder_id", reminder.id)
            .putString("reminder_title", "${reminder.title} (in ${reminder.reminderTimeBeforeMinutes} min)")
            .putString("reminder_description", reminder.description)
            .putString("pet_name", reminder.petName)
            .putBoolean("is_before_reminder", true)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInputData(beforeInputData)
            .setInitialDelay(delayInMinutes, TimeUnit.MINUTES)
            .build()

        val workName = "${ReminderWorker.UNIQUE_WORK_NAME_PREFIX}${reminder.id}"
        Log.d(TAG, "Enqueueing 'BEFORE' work with name: $workName")
        Log.d(TAG, "Work ID: ${workRequest.id}")

        workManager.enqueueUniqueWork(
            workName,
            androidx.work.ExistingWorkPolicy.KEEP,
            workRequest
        )

        Log.d(TAG, "'BEFORE' work enqueued successfully")

        // Schedule the exact time notification
        val delayToExactTime = calculateDelayToExactTime(
            reminder.time ?: LocalTime.now()
        )

        Log.d(TAG, "Calculated delay (for exact time notification): $delayToExactTime minutes")
        Log.d(TAG, "Exact time notification will trigger at: ${LocalDateTime.now().plusMinutes(delayToExactTime)}")

        val exactInputData = Data.Builder()
            .putString("reminder_id", reminder.id)
            .putString("reminder_title", reminder.title)
            .putString("reminder_description", reminder.description)
            .putString("pet_name", reminder.petName)
            .putBoolean("is_before_reminder", false)
            .build()

        val exactWorkRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInputData(exactInputData)
            .setInitialDelay(delayToExactTime, TimeUnit.MINUTES)
            .build()

        val exactWorkName = "${ReminderWorker.UNIQUE_WORK_NAME_PREFIX}${reminder.id}_exact"
        Log.d(TAG, "Enqueueing 'EXACT TIME' work with name: $exactWorkName")
        Log.d(TAG, "Work ID: ${exactWorkRequest.id}")

        workManager.enqueueUniqueWork(
            exactWorkName,
            androidx.work.ExistingWorkPolicy.KEEP,
            exactWorkRequest
        )

        Log.d(TAG, "'EXACT TIME' work enqueued successfully")
        Log.d(TAG, "--- scheduleOneTimeReminder END ---")
    }

    private fun scheduleRecurringReminder(reminder: Reminder, inputData: Data) {
        Log.d(TAG, "--- scheduleRecurringReminder START ---")
        val intervalInMinutes = when (reminder.recurrencePattern) {
            RecurrencePattern.DAILY -> (24).toLong()
            RecurrencePattern.WEEKLY -> (24 * 7).toLong()
            RecurrencePattern.MONTHLY -> (24 * 30).toLong()
            RecurrencePattern.ONCE -> {
                Log.w(TAG, "Recurrence pattern is ONCE, should not be recurring")
                return
            }
        }

        Log.d(TAG, "Interval: $intervalInMinutes hours")

        val workRequest = PeriodicWorkRequestBuilder<ReminderWorker>(
            intervalInMinutes,
            TimeUnit.HOURS
        )
            .setInputData(inputData)
            .build()

        val workName = "${ReminderWorker.UNIQUE_WORK_NAME_PREFIX}${reminder.id}"
        Log.d(TAG, "Enqueueing periodic work with name: $workName")
        Log.d(TAG, "Work ID: ${workRequest.id}")

        workManager.enqueueUniquePeriodicWork(
            workName,
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )

        Log.d(TAG, "Periodic work enqueued successfully")
        Log.d(TAG, "--- scheduleRecurringReminder END ---")
    }

    fun cancelReminder(reminderId: String) {
        Log.d(TAG, "Cancelling reminder: $reminderId")

        // Cancel both 'before' and 'exact time' notifications
        val beforeWorkName = "${ReminderWorker.UNIQUE_WORK_NAME_PREFIX}$reminderId"
        val exactWorkName = "${ReminderWorker.UNIQUE_WORK_NAME_PREFIX}${reminderId}_exact"

        Log.d(TAG, "Cancelling 'BEFORE' work: $beforeWorkName")
        workManager.cancelUniqueWork(beforeWorkName)

        Log.d(TAG, "Cancelling 'EXACT' work: $exactWorkName")
        workManager.cancelUniqueWork(exactWorkName)

        Log.d(TAG, "Reminder cancelled: $reminderId")
    }

    fun cancelAllReminders() {
        workManager.cancelAllWork()
    }

    private fun calculateDelayToExactTime(
        reminderTime: LocalTime
    ): Long {
        Log.d(TAG, "--- calculateDelayToExactTime START ---")
        val now = LocalDateTime.now()
        val today = now.toLocalDate()

        Log.d(TAG, "Current DateTime: $now")
        Log.d(TAG, "Today's Date: $today")
        Log.d(TAG, "Reminder Time: $reminderTime")

        // Calculate the exact reminder time (no minutes subtracted)
        val reminderDateTime = today.atTime(reminderTime)
        Log.d(TAG, "Calculated exact reminder DateTime: $reminderDateTime")

        val delayInMinutes = if (reminderDateTime.isAfter(now)) {
            // If reminder time hasn't passed today, schedule it
            val delay = Duration.between(now, reminderDateTime).toMinutes()
            Log.d(TAG, "Exact time hasn't passed today. Delay: $delay minutes")
            delay
        } else {
            // If reminder time has passed, schedule it for tomorrow
            val tomorrowReminder = reminderDateTime.plusDays(1)
            val delay = Duration.between(now, tomorrowReminder).toMinutes()
            Log.d(TAG, "Exact time already passed today. Scheduling for tomorrow at: $tomorrowReminder")
            Log.d(TAG, "Tomorrow's delay: $delay minutes")
            delay
        }

        Log.d(TAG, "Final delay in minutes (for exact time): $delayInMinutes")
        Log.d(TAG, "--- calculateDelayToExactTime END ---")
        return delayInMinutes
    }

    private fun calculateDelayToReminderTime(
        reminderTime: LocalTime,
        minutesBefore: Int
    ): Long {
        Log.d(TAG, "--- calculateDelayToReminderTime START ---")
        val now = LocalDateTime.now()
        val today = now.toLocalDate()

        Log.d(TAG, "Current DateTime: $now")
        Log.d(TAG, "Today's Date: $today")
        Log.d(TAG, "Reminder Time: $reminderTime")
        Log.d(TAG, "Minutes Before: $minutesBefore")

        // Calculate the reminder time (minus the before minutes)
        val reminderDateTime = today.atTime(reminderTime).minusMinutes(minutesBefore.toLong())
        Log.d(TAG, "Calculated reminder DateTime (minus before minutes): $reminderDateTime")

        val delayInMinutes = if (reminderDateTime.isAfter(now)) {
            // If reminder time hasn't passed today, schedule it
            val delay = Duration.between(now, reminderDateTime).toMinutes()
            Log.d(TAG, "Reminder hasn't passed today. Delay: $delay minutes")
            delay
        } else {
            // If reminder time has passed, schedule it for tomorrow
            val tomorrowReminder = reminderDateTime.plusDays(1)
            val delay = Duration.between(now, tomorrowReminder).toMinutes()
            Log.d(TAG, "Reminder already passed today. Scheduling for tomorrow at: $tomorrowReminder")
            Log.d(TAG, "Tomorrow's delay: $delay minutes")
            delay
        }

        Log.d(TAG, "Final delay in minutes: $delayInMinutes")
        Log.d(TAG, "--- calculateDelayToReminderTime END ---")
        return delayInMinutes
    }
}
