package pl.appnode.napwatch;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;

public class AlarmSettingsActivity extends Activity implements View.OnClickListener {

    private static final String TAG = "AlarmSettingsActivity";
    private int mAlarmId;
    private String mAlarmName;
    private int mAlarmTimeUnit;
    private String mAlarmRingtoneUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.alarm_settings_dialog);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        this.setFinishOnTouchOutside(false);
        Log.d(TAG, "AlarmSettingsActivity started.");
        Intent settingsIntent = getIntent();
        if (settingsIntent.getExtras().get("AlarmId") != null)
            {mAlarmId = (int) settingsIntent.getExtras().get("AlarmId");}
        if (settingsIntent.getExtras().get("AlarmName") != null)
            {mAlarmName = (String) settingsIntent.getExtras().get("AlarmName");}
        if (settingsIntent.getExtras().get("AlarmUnit") != null)
            {mAlarmTimeUnit = (int) settingsIntent.getExtras().get("AlarmUnit");}
        if (settingsIntent.getExtras().get("AlarmRingtoneUri") != null)
            {mAlarmRingtoneUri = (String) settingsIntent.getExtras().get("AlarmRingtoneUri");}
    }

    public void onResume() {
        super.onResume();
        EditText input = (EditText) findViewById(R.id.alarmNameText);
        input.setText("Text!");
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