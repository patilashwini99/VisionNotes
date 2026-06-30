package com.example.visionnotes;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class SnoozeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        String title = intent.getStringExtra("title");
        String desc = intent.getStringExtra("desc");
        String noteId = intent.getStringExtra("noteId"); // 🔥 Firebase support
        int notificationId = intent.getIntExtra("notificationId", 0);

        // ✅ Cancel old notification
        NotificationManager nm =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (nm != null) {
            nm.cancel(notificationId);
        }

        // ⏰ 10 min snooze
        long snoozeTime = System.currentTimeMillis() + (10 * 60 * 1000);

        // 🔥 ReminderReceiver call
        Intent reminderIntent = new Intent(context, ReminderReceiver.class);
        reminderIntent.putExtra("title", title);
        reminderIntent.putExtra("desc", desc);
        reminderIntent.putExtra("noteId", noteId); // 🔥 IMPORTANT
        reminderIntent.putExtra("notificationId", notificationId);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                notificationId,
                reminderIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager =
                (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if (alarmManager != null) {

            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                        !alarmManager.canScheduleExactAlarms()) {

                    // fallback if exact not allowed
                    alarmManager.set(
                            AlarmManager.RTC_WAKEUP,
                            snoozeTime,
                            pendingIntent
                    );

                } else {
                    alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            snoozeTime,
                            pendingIntent
                    );
                }

            } catch (Exception e) {
                e.printStackTrace();

                // fallback safe
                alarmManager.set(
                        AlarmManager.RTC_WAKEUP,
                        snoozeTime,
                        pendingIntent
                );
            }
        }
    }
}