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

import static pl.appnode.napwatch.StateConstants.MINUTE;
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
    private TextView mTitle;
    private EditText mEditAlarmName;
    private RadioButton mRbSeconds;
    private RadioButton mRbMinutes;
    private Button mRingtoneTextButtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.alarm_settings_dialog);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        this.setFinishOnTouchOutside(false);
        mTitle = (TextView) findViewById(R.id.alarmEditTitle);
        mEditAlarmName = (EditText) findViewById(R.id.alarmNameText);
        mRbSeconds = (RadioButton) findViewById(R.id.radioSeconds);
        mRbMinutes = (RadioButton) findViewById(R.id.radioMinutes);
        mRingtoneTextButtn = (Button) findViewById(R.id.changeRingtone);
        mRingtoneTextButtn.setOnClickListener(this);
        Button buttonOk = (Button) findViewById(R.id.okAlarmSettings);
        buttonOk.setOnClickListener(this);
        Button buttonCancel = (Button) findViewById(R.id.cancelAlarmSettings);
        buttonCancel.setOnClickListener(this);
        Intent settingsIntent = getIntent();
        if (settingsIntent.getExtras() != null) {
            mAlarmId = (int) settingsIntent.getExtras().get("AlarmId");
            mAlarmName = (String) settingsIntent.getExtras().get("AlarmName");
            mAlarmTimeUnit = (int) settingsIntent.getExtras().get("AlarmUnit");
            mAlarmRingtoneUri = (String) settingsIntent.getExtras().get("AlarmRingtoneUri");
        }
        Log.d(TAG, "AlarmSettingsActivity started.");
    }

    public void onResume() {
        super.onResume();
        mTitle.setText(R.string.alarm_settings_title);
        mTitle.append("" + (mAlarmId + 1));
        mEditAlarmName.setText(mAlarmName);
        if (mAlarmTimeUnit == SECOND) {
            mRbSeconds.toggle();
        } else mRbMinutes.toggle();
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
        mRingtoneTextButtn.setText(mRingtoneName);
    }

    @Override
    public void onClick(View v) {
        Log.d(TAG, "Clicked!!");
        Intent resultIntent = getIntent();
        switch (v.getId()) {
            case R.id.okAlarmSettings:
                Log.d(TAG, "Clicked OK!");
                resultIntent.putExtra("AlarmId", mAlarmId);
                resultIntent.putExtra("AlarmName", mEditAlarmName.getText());
                if (mRbSeconds.isChecked()) {
                    mAlarmTimeUnit = SECOND;
                } else {
                    mAlarmTimeUnit = MINUTE;
                }
                resultIntent.putExtra("AlarmUnit", mAlarmTimeUnit);
                resultIntent.putExtra("AlarmRingtoneUri", mCurrentRingtoneUri);
                setResult(RESULT_OK, resultIntent);
                finish();
                break;
            case R.id.cancelAlarmSettings:
                Log.d(TAG, "Clicked CANCEL!");
                setResult(RESULT_CANCELED, resultIntent);
                finish();
                break;
            case R.id.changeRingtone:
                Log.d(TAG, "Clicked RINGTONE!");
                break;
        }
    }
}