package com.example.visionnotes;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

public class NotificationHelper {

    public static final String CHANNEL_ID = "notes_channel";

    public static void showNotification(Context context,
                                        String title,
                                        String desc,
                                        String noteId,
                                        int notificationId) {

        createChannel(context);

        // 🔥 OPEN NOTE (click notification)
        Intent openIntent = new Intent(context, AddNoteActivity.class);
        openIntent.putExtra("id", noteId);
        openIntent.putExtra("title", title);
        openIntent.putExtra("description", desc);

        PendingIntent openPendingIntent = PendingIntent.getActivity(
                context,
                notificationId,
                openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // 🔥 SNOOZE
        Intent snoozeIntent = new Intent(context, SnoozeReceiver.class);
        snoozeIntent.putExtra("title", title);
        snoozeIntent.putExtra("desc", desc);
        snoozeIntent.putExtra("noteId", noteId);
        snoozeIntent.putExtra("notificationId", notificationId);

        PendingIntent snoozePendingIntent = PendingIntent.getBroadcast(
                context,
                notificationId + 1,
                snoozeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Action snoozeAction =
                new NotificationCompat.Action.Builder(
                        R.drawable.ic_snooze,
                        "Snooze 10 min",
                        snoozePendingIntent
                ).build();

        // 🔥 MARK DONE (Firebase update)
        Intent doneIntent = new Intent(context, MarkDoneReceiver.class);
        doneIntent.putExtra("noteId", noteId);
        doneIntent.putExtra("notificationId", notificationId);

        PendingIntent donePendingIntent = PendingIntent.getBroadcast(
                context,
                notificationId + 2,
                doneIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Action doneAction =
                new NotificationCompat.Action.Builder(
                        R.drawable.ic_done,
                        "Done",
                        donePendingIntent
                ).build();

        // 🔥 BUILD NOTIFICATION
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_notes)
                        .setContentTitle(title == null ? "Note Reminder" : title)
                        .setContentText(desc == null ? "" : desc)
                        .setContentIntent(openPendingIntent) // 🔥 open note
                        .addAction(snoozeAction)
                        .addAction(doneAction)
                        .setAutoCancel(true)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setDefaults(NotificationCompat.DEFAULT_ALL);

        NotificationManager nm =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (nm != null) {
            nm.notify(notificationId, builder.build());
        }
    }

    // 🔥 CREATE CHANNEL
    private static void createChannel(Context context) {

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Notes Reminder",
                    NotificationManager.IMPORTANCE_HIGH
            );

            channel.setDescription("Channel for Notes reminders");

            NotificationManager nm =
                    context.getSystemService(NotificationManager.class);

            if (nm != null) {
                nm.createNotificationChannel(channel);
            }
        }
    }
}