package com.example.visionnotes;

import android.app.Application;
import com.google.firebase.database.FirebaseDatabase;

public class VisionNotes extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Enable Firebase offline persistence
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}