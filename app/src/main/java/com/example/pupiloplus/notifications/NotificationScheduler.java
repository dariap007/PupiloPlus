package com.example.pupiloplus.notifications;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.AlarmManagerCompat;

import com.example.pupiloplus.MainActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
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

            calendar = getNextOccurrence(calendar, frequency);
            long scheduledTime = calendar.getTimeInMillis();

            Intent intent = new Intent(context, NotificationReceiver.class);
            intent.setAction("com.example.pupiloplus.notifications.ACTION_NOTIFY_" + reminderId);
            intent.putExtra("title", title);
            intent.putExtra("message", message);
            intent.putExtra("reminderId", reminderId);
            intent.putExtra("frequency", frequency);
            intent.putExtra("dateTimeString", dateTimeString);

            int requestCode = (int) (reminderId & 0xFFFFFFFF);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {
                PendingIntent showIntent = PendingIntent.getActivity(context, 0, new Intent(context, MainActivity.class), PendingIntent.FLAG_IMMUTABLE);
                AlarmManager.AlarmClockInfo alarmClockInfo = new AlarmManager.AlarmClockInfo(scheduledTime, showIntent);
                alarmManager.setAlarmClock(alarmClockInfo, pendingIntent);
            }

            android.util.Log.d("NotificationScheduler", "Scheduling reminder at " + scheduledTime);
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
        intent.setAction("com.example.pupiloplus.notifications.ACTION_NOTIFY_" + reminderId);
        int requestCode = (int) (reminderId & 0xFFFFFFFF);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE);

        if (pendingIntent != null) {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {
                alarmManager.cancel(pendingIntent);
            }
        }
    }

    private static Calendar getNextOccurrence(Calendar calendar, String frequency) {
        Calendar now = Calendar.getInstance();
        if (frequency == null) {
            if (calendar.before(now)) {
                calendar.add(Calendar.DAY_OF_YEAR, 1);
            }
            return calendar;
        }

        if (frequency.equals("Ежедневно")) {
            while (calendar.before(now)) {
                calendar.add(Calendar.DAY_OF_YEAR, 1);
            }
            return calendar;
        }

        if (frequency.startsWith("По дням недели:")) {
            String daysPart = frequency.substring("По дням недели:".length()).trim();
            List<Integer> targetDays = parseWeekdays(daysPart);
            if (targetDays.isEmpty()) {
                if (calendar.before(now)) {
                    calendar.add(Calendar.DAY_OF_YEAR, 1);
                }
                return calendar;
            }

            Calendar next = Calendar.getInstance();
            next.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY));
            next.set(Calendar.MINUTE, calendar.get(Calendar.MINUTE));
            next.set(Calendar.SECOND, calendar.get(Calendar.SECOND));
            next.set(Calendar.MILLISECOND, calendar.get(Calendar.MILLISECOND));

            int currentDayOfWeek = next.get(Calendar.DAY_OF_WEEK);
            int bestDelta = Integer.MAX_VALUE;
            for (int day : targetDays) {
                int delta = (day - currentDayOfWeek + 7) % 7;
                if (delta == 0 && next.before(now)) {
                    delta = 7;
                }
                if (delta < bestDelta) {
                    bestDelta = delta;
                }
            }
            if (bestDelta == Integer.MAX_VALUE) {
                bestDelta = 7;
            }
            next.add(Calendar.DAY_OF_YEAR, bestDelta);
            return next;
        }

        if (calendar.before(now)) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }
        return calendar;
    }

    private static List<Integer> parseWeekdays(String daysPart) {
        List<Integer> days = new ArrayList<>();
        if (daysPart == null || daysPart.isEmpty()) {
            return days;
        }
        String[] split = daysPart.split(",");
        for (String dayName : split) {
            String value = dayName.trim().toLowerCase(Locale.ROOT);
            switch (value) {
                case "понедельник":
                    days.add(Calendar.MONDAY);
                    break;
                case "вторник":
                    days.add(Calendar.TUESDAY);
                    break;
                case "среда":
                    days.add(Calendar.WEDNESDAY);
                    break;
                case "четверг":
                    days.add(Calendar.THURSDAY);
                    break;
                case "пятница":
                    days.add(Calendar.FRIDAY);
                    break;
                case "суббота":
                    days.add(Calendar.SATURDAY);
                    break;
                case "воскресенье":
                    days.add(Calendar.SUNDAY);
                    break;
            }
        }
        return days;
    }
}
