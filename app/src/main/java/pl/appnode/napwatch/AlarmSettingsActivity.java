package pl.appnode.napwatch;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class AlarmSettingsActivity extends Activity implements View.OnClickListener {

    private static final String TAG = "AlarmSettingsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alarm_settings_dialog);
        Log.d(TAG, "AlarmSettingsActivity started.");
    }

    @Override
    public void onClick(View v) {
        Intent intent = getIntent();
        switch (v.getId()) {
            case R.id.okAlarmSettings:
                intent.putExtra("ok", "ok");
                setResult(RESULT_OK, intent);
                break;
            case R.id.cancelAlarmSettings:
                setResult(RESULT_CANCELED, intent);
                break;
        }
        finish();
    }
}