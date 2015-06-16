package pl.appnode.napwatch;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;

public class ThemeSetup {

    public static boolean themeSetup(Context context) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        boolean themeDark = settings.getBoolean("settings_checkbox_theme", false);
        if (themeDark) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                context.setTheme(android.R.style.Theme_Holo);
            } else {
                context.setTheme(android.R.style.Theme_Material);
            }
        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            context.setTheme(android.R.style.Theme_Holo_Light);
        } else {
            context.setTheme(android.R.style.Theme_Material_Light);
        }
        return themeDark;
    }
}
