package pl.appnode.napwatch;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

public class AlarmSettingsActivity extends Activity implements View.OnClickListener {

    private static final String TAG = "AlarmSettingsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alarm_settings_dialog);
    }

    @Override
    public void onClick(View v) {
    }
}