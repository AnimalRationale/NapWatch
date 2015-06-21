package pl.appnode.napwatch;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class AppSettings {

    public static boolean isDarkTheme(Context context) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        boolean themeDark = settings.getBoolean("settings_checkbox_theme", false);
        return themeDark;
    }

    public static boolean isRotationOn(Context context) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        boolean rotationOn = settings.getBoolean("settings_checkbox_orientstion", false);
        return rotationOn;
    }
}