package pl.appnode.napwatch;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

import static pl.appnode.napwatch.ThemeSetup.themeSetup;

public class SettingsActivity extends PreferenceActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        themeSetup(this);
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}