package com.example.visionnotes;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class SettingsActivity extends AppCompatActivity {

    RecyclerView rvSettings;
    MaterialToolbar toolbar1;
    MaterialButton btnBackup, btnRestore;

    DatabaseReference dbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Theme
        if (PrefsHelper.isDarkTheme(this))
            setTheme(R.style.Theme_Notes);
        else
            setTheme(R.style.Theme_Notes);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        dbRef = FirebaseDatabase.getInstance().getReference("user_settings"); // Firebase path

        rvSettings = findViewById(R.id.rvSettings);
        toolbar1 = findViewById(R.id.toolbar1);
        setSupportActionBar(toolbar1);
        getSupportActionBar().setTitle("Settings");
        toolbar1.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbar1.setNavigationOnClickListener(v -> finish());

        btnBackup = findViewById(R.id.btnBackup);
        btnRestore = findViewById(R.id.btnRestore);

        List<SettingItem> list = new ArrayList<>();
        list.add(new SettingItem("Dark Theme", true, PrefsHelper.isDarkTheme(this)));
        list.add(new SettingItem("Notifications", true, PrefsHelper.isNotificationsEnabled(this)));
        list.add(new SettingItem("Font Size Large", true, PrefsHelper.isFontLarge(this)));
        list.add(new SettingItem("About", false, false));

        SettingsAdapter adapter = new SettingsAdapter(this, list, (item, isChecked) -> {
            switch (item.getTitle()) {
                case "Dark Theme":
                    PrefsHelper.setDarkTheme(this, isChecked);
                    dbRef.child("dark_theme").setValue(isChecked);
                    recreate();
                    break;
                case "Notifications":
                    PrefsHelper.setNotifications(this, isChecked);
                    dbRef.child("notifications").setValue(isChecked);
                    Toast.makeText(this, isChecked ? "Notifications ON 🔔" : "OFF 🔕", Toast.LENGTH_SHORT).show();
                    break;
                case "Font Size Large":
                    PrefsHelper.setFontLarge(this, isChecked);
                    dbRef.child("font_large").setValue(isChecked);
                    recreate();
                    break;
            }
        });

        rvSettings.setLayoutManager(new LinearLayoutManager(this));
        rvSettings.setAdapter(adapter);

        // Restore settings from Firebase
        dbRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                DataSnapshot snap = task.getResult();
                for (SettingItem item : list) {
                    switch (item.getTitle()) {
                        case "Dark Theme":
                            Boolean dark = snap.child("dark_theme").getValue(Boolean.class);
                            if (dark != null) item.setChecked(dark);
                            break;
                        case "Notifications":
                            Boolean noti = snap.child("notifications").getValue(Boolean.class);
                            if (noti != null) item.setChecked(noti);
                            break;
                        case "Font Size Large":
                            Boolean font = snap.child("font_large").getValue(Boolean.class);
                            if (font != null) item.setChecked(font);
                            break;
                    }
                }
                adapter.notifyDataSetChanged();
            }
        });
    }
}