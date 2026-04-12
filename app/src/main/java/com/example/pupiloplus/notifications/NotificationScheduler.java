package com.example.pupiloplus.notifications;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class NotificationScheduler {

    /**
     * Schedule a reminder notification with full datetime and recurring support
     * @param context Application context
     * @param reminderId Unique ID for the reminder (used as request code)
     * @param title Notification title
     * @param message Notification message
     * @param dateTimeString Date and time in format "yyyy-MM-dd HH:mm"
     * @param frequency Frequency type: "Once", "Daily", "Weekly", "By Days"
     */
    public static void schedule(Context context, long reminderId, String title, String message, String dateTimeString, String frequency) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(dateFormat.parse(dateTimeString));

            // If the scheduled time is in the past, schedule for the next day
            if (calendar.before(Calendar.getInstance()) && "Once".equals(frequency)) {
                calendar.add(Calendar.DAY_OF_YEAR, 1);
            }

            Intent intent = new Intent(context, NotificationReceiver.class);
            intent.putExtra("title", title);
            intent.putExtra("message", message);
            intent.putExtra("reminderId", reminderId);
            intent.putExtra("frequency", frequency);
            intent.putExtra("dateTimeString", dateTimeString);

            int requestCode = (int) (reminderId & 0xFFFFFFFF);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Cancel a scheduled reminder
     * @param context Application context
     * @param reminderId ID of the reminder to cancel
     */
    public static void cancel(Context context, long reminderId) {
        Intent intent = new Intent(context, NotificationReceiver.class);
        int requestCode = (int) (reminderId & 0xFFFFFFFF);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE);

        if (pendingIntent != null) {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {
                alarmManager.cancel(pendingIntent);
            }
        }
    }
}
