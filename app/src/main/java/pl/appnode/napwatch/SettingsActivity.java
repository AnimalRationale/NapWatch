package pl.appnode.napwatch;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class SettingsActivity extends PreferenceActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        if (settings.getBoolean("settings_checkbox_theme", false)) {
            setTheme(android.R.style.Theme_DeviceDefault);
        } else setTheme(android.R.style.Theme_DeviceDefault_Light);
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}