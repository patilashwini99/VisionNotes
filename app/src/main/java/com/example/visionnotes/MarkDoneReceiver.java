package com.example.visionnotes;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class MarkDoneReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        int notificationId = intent.getIntExtra("notificationId", 0);
        String noteId = intent.getStringExtra("noteId");

        // 🔥 Cancel notification
        NotificationManager nm =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (nm != null) nm.cancel(notificationId);

        // 🔥 Firebase update
        if (noteId != null) {

            String userId = FirebaseAuth.getInstance()
                    .getCurrentUser()
                    .getUid();

            FirebaseDatabase.getInstance()
                    .getReference("notes")
                    .child(userId)
                    .child(noteId)
                    .child("done")
                    .setValue(true);
        }

        Toast.makeText(context, "Task Completed ✅", Toast.LENGTH_SHORT).show();
    }
}