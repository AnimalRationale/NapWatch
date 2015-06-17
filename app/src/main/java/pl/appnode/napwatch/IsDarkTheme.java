package pl.appnode.napwatch;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class IsDarkTheme {

    public static boolean isDarkTheme(Context context) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        boolean themeDark = settings.getBoolean("settings_checkbox_theme", false);
        return themeDark;
    }
}