package com.example.pupiloplus.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class NotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        android.util.Log.d("NotificationReceiver", "onReceive called");
        String title = intent.getStringExtra("title");
        String message = intent.getStringExtra("message");
        long reminderId = intent.getLongExtra("reminderId", -1);
        String frequency = intent.getStringExtra("frequency");
        String dateTimeString = intent.getStringExtra("dateTimeString");

        NotificationHelper notificationHelper = new NotificationHelper(context);
        notificationHelper.createNotification(title, message);

        if (reminderId != -1 && frequency != null && !frequency.equals("Однократно")) {
            NotificationScheduler.schedule(context, reminderId, title, message, dateTimeString, frequency);
        }
    }
}
