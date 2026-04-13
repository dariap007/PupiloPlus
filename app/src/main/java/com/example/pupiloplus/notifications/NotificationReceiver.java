package com.example.pupiloplus.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class NotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("NotificationReceiver", "onReceive called");
        String title = intent.getStringExtra("title");
        String message = intent.getStringExtra("message");
        NotificationHelper notificationHelper = new NotificationHelper(context);
        notificationHelper.createNotification(title, message);
    }
}
