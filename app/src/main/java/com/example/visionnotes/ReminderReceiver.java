package com.example.visionnotes;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ReminderReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        // 🔹 Get data from intent
        String title = intent.getStringExtra("title");
        String desc = intent.getStringExtra("desc");
        String noteId = intent.getStringExtra("noteId");

        // 🔹 Safety check (avoid null crash)
        if(title == null) title = "Reminder";
        if(desc == null) desc = "";

        // 🔥 Unique notification id
        int notificationId = (int) System.currentTimeMillis();

        // 🔔 Check notification setting
        if (!PrefsHelper.isNotificationsEnabled(context)) {
            return;
        }

        // 🔥 Show notification (Firebase independent but used with your notes)
        NotificationHelper.showNotification(
                context,
                title,
                desc,
                noteId,
                notificationId
        );
    }
}