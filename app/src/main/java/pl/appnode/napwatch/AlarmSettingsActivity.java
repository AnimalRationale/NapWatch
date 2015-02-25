package pl.appnode.napwatch;

import android.app.Activity;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import static pl.appnode.napwatch.StateConstants.SECOND;

public class AlarmSettingsActivity extends Activity implements View.OnClickListener {

    private static final String TAG = "AlarmSettingsActivity";
    private int mAlarmId;
    private String mAlarmName;
    private int mAlarmTimeUnit;
    private String mAlarmRingtoneUri;
    private Uri mCurrentRingtoneUri;
    private Ringtone mRingtone;
    private String mRingtoneName;

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

        TextView title = (TextView) findViewById(R.id.alarmEditTitle);
        title.setText(R.string.alarm_settings_title);
        title.append("" + (mAlarmId + 1));

        EditText input = (EditText) findViewById(R.id.alarmNameText);
        input.setText(mAlarmName);

        RadioButton rbSeconds = (RadioButton) findViewById(R.id.radioSeconds);
        RadioButton rbMinutes = (RadioButton) findViewById(R.id.radioMinutes);
        if (mAlarmTimeUnit == SECOND) {
            rbSeconds.toggle();
        } else rbMinutes.toggle();

        if (mAlarmRingtoneUri == null) {
            mCurrentRingtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            if (mCurrentRingtoneUri == null) {
                mCurrentRingtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                if (mCurrentRingtoneUri == null) {
                    mCurrentRingtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
                }
            }
        } else mCurrentRingtoneUri = Uri.parse(mAlarmRingtoneUri);
        mRingtone = RingtoneManager.getRingtone(this.getApplicationContext(), mCurrentRingtoneUri);
        mRingtoneName =  mRingtone.getTitle(this.getApplicationContext());
        Button ringtoneTextBtn = (Button) findViewById(R.id.changeRingtone);
        ringtoneTextBtn.setText(mRingtoneName);
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