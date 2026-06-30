package com.example.visionnotes;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class PrefsHelper {

    private static final String PREFS_NAME = "settings_pref";

    private static final String KEY_DARK_THEME = "dark_theme";
    private static final String KEY_NOTIFICATIONS = "notifications";
    private static final String KEY_FONT_LARGE = "font_large";

    private static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    // 🔥 Firebase sync helper
    private static void syncToFirebase(String key, Object value){

        try {
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

            FirebaseDatabase.getInstance()
                    .getReference("users_settings")
                    .child(userId)
                    .child(key)
                    .setValue(value);

        } catch (Exception e){
            e.printStackTrace(); // user not logged maybe
        }
    }

    // 🌙 DARK THEME
    public static void setDarkTheme(Context context, boolean isDark) {

        getPrefs(context).edit().putBoolean(KEY_DARK_THEME, isDark).apply();

        syncToFirebase(KEY_DARK_THEME, isDark); // 🔥 sync
    }

    public static boolean isDarkTheme(Context context) {
        return getPrefs(context).getBoolean(KEY_DARK_THEME, false);
    }

    // 🔔 NOTIFICATIONS
    public static void setNotifications(Context context, boolean enabled) {

        getPrefs(context).edit().putBoolean(KEY_NOTIFICATIONS, enabled).apply();

        syncToFirebase(KEY_NOTIFICATIONS, enabled); // 🔥 sync
    }

    public static boolean isNotificationsEnabled(Context context) {
        return getPrefs(context).getBoolean(KEY_NOTIFICATIONS, true);
    }

    // 🔠 FONT SIZE
    public static void setFontLarge(Context context, boolean isLarge) {

        getPrefs(context).edit().putBoolean(KEY_FONT_LARGE, isLarge).apply();

        syncToFirebase(KEY_FONT_LARGE, isLarge); // 🔥 sync
    }

    public static boolean isFontLarge(Context context) {
        return getPrefs(context).getBoolean(KEY_FONT_LARGE, false);
    }

    // 🔥 CLEAR (Logout)
    public static void clearAll(Context context) {

        getPrefs(context).edit().clear().apply();

        try {
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

            FirebaseDatabase.getInstance()
                    .getReference("users_settings")
                    .child(userId)
                    .removeValue();

        } catch (Exception e){
            e.printStackTrace();
        }
    }
}