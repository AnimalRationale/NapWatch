package pl.appnode.napwatch;

import android.app.Activity;
import android.content.Intent;
import android.media.AudioManager;
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
import android.widget.SeekBar;
import android.widget.TextView;

import static pl.appnode.napwatch.StateConstants.MINUTE;
import static pl.appnode.napwatch.StateConstants.OFF;
import static pl.appnode.napwatch.StateConstants.RINGTONE_INTENT_REQUEST;
import static pl.appnode.napwatch.StateConstants.SECOND;

public class AlarmSettingsActivity extends Activity implements View.OnClickListener {

    private static final String TAG = "AlarmSettingsActivity";
    private int mAlarmId;
    private String mAlarmName;
    private int mAlarmTimeUnit;
    private String mAlarmRingtoneUri;
    private int mAlarmRingtoneVolume;
    private int mOriginalVolume;
    private AudioManager mAudioManager;
    private Uri mCurrentRingtoneUri;
    private Ringtone mRingtone;
    private String mRingtoneName;
    private TextView mTitle;
    private EditText mEditAlarmName;
    private RadioButton mRbSeconds;
    private RadioButton mRbMinutes;
    private Button mRingtoneTextButton;
    private SeekBar mVolumeSeekbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.alarm_settings_dialog);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        this.setFinishOnTouchOutside(false);
        mAudioManager = (AudioManager) this.getSystemService(this.AUDIO_SERVICE);
        mTitle = (TextView) findViewById(R.id.alarmEditTitle);
        mEditAlarmName = (EditText) findViewById(R.id.alarmNameText);
        mRbSeconds = (RadioButton) findViewById(R.id.radioSeconds);
        mRbMinutes = (RadioButton) findViewById(R.id.radioMinutes);
        mRingtoneTextButton = (Button) findViewById(R.id.changeRingtone);
        mRingtoneTextButton.setOnClickListener(this);
        mVolumeSeekbar = (SeekBar) findViewById(R.id.volumeSeekBar);
        Button buttonOk = (Button) findViewById(R.id.okAlarmSettings);
        buttonOk.setOnClickListener(this);
        Button buttonCancel = (Button) findViewById(R.id.cancelAlarmSettings);
        buttonCancel.setOnClickListener(this);
        AudioManager audioManager = (AudioManager) getSystemService(this.AUDIO_SERVICE);
        Intent settingsIntent = getIntent();
        if (settingsIntent.getExtras() != null) {
            mAlarmId = (int) settingsIntent.getExtras().get("AlarmId");
            mAlarmName = (String) settingsIntent.getExtras().get("AlarmName");
            mAlarmTimeUnit = (int) settingsIntent.getExtras().get("AlarmUnit");
            mAlarmRingtoneUri = (String) settingsIntent.getExtras().get("AlarmRingtoneUri");
            if (settingsIntent.getExtras().get("AlarmRingtoneVol") != null) {
                mAlarmRingtoneVolume = (int) settingsIntent.getExtras().get("AlarmRingtoneVol");
                Log.d(TAG, "Volume OK.");
            } else {
                mAlarmRingtoneVolume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM);
                Log.d(TAG, "Volume NULL");
            }
        }
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
        mRingtoneTextButton.setText(mRingtoneName);
        mRingtone.setStreamType(AudioManager.STREAM_ALARM);
        mVolumeSeekbar.setMax(audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM));
        mVolumeSeekbar.setProgress(mAlarmRingtoneVolume);
        mVolumeSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mAlarmRingtoneVolume = progress;
                playRingtone();
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                stopRingtone();
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        mTitle.setText(R.string.alarm_settings_title );
        mTitle.append("" + (mAlarmId + 1));
        mEditAlarmName.setText(mAlarmName);
        if (mAlarmTimeUnit == SECOND) {
            mRbSeconds.toggle();
        } else mRbMinutes.toggle();
        Log.d(TAG, "AlarmSettingsActivity started.");
    }

    public void onResume() {
        super.onResume();
        if (MainActivity.sAlarmState[mAlarmId] != OFF) {finish();}
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent resultIntent) {
        Log.d(TAG, "onActivityResult fo Ringtone Picker.");
        if (requestCode == RINGTONE_INTENT_REQUEST && resultCode == RESULT_OK && resultIntent.getExtras() != null) {
            Log.d(TAG, "Proper Ringtone ResultIntent.");
            mCurrentRingtoneUri = resultIntent.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            mRingtone = RingtoneManager.getRingtone(this.getApplicationContext(), mCurrentRingtoneUri);
            mRingtoneName =  mRingtone.getTitle(this.getApplicationContext());
            mRingtoneTextButton.setText(mRingtoneName);
            Log.d(TAG, "Ringtone Result: " + mRingtoneName);
        }
    }

    private void playRingtone() {
        mOriginalVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_ALARM);
        if (mAlarmRingtoneVolume <= 0) {
            mAudioManager.setStreamVolume(mAudioManager.STREAM_ALARM, 0, 0);
        } else if (mAlarmRingtoneVolume >= mAudioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM)) {
            mAudioManager.setStreamVolume(AudioManager.STREAM_ALARM, mAudioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM), 0);
        } else {
            mAudioManager.setStreamVolume(AudioManager.STREAM_ALARM, mAlarmRingtoneVolume, 0);}
        Log.d(TAG, "Set ringtone volume: " + mAlarmRingtoneVolume);
        mRingtone.play();
    }

    private void stopRingtone() {
        mRingtone.stop();
        mAudioManager.setStreamVolume(AudioManager.STREAM_ALARM, mOriginalVolume, 0);
        Log.d(TAG, "Restored ringtone volume: " + mOriginalVolume);
    }

    private void resultOk() {
        Intent resultIntent = getIntent();
        resultIntent.putExtra("AlarmId", mAlarmId);
        resultIntent.putExtra("AlarmName", mEditAlarmName.getText().toString());
        if (mRbSeconds.isChecked()) {
            mAlarmTimeUnit = SECOND;
        } else {
            mAlarmTimeUnit = MINUTE;
        }
        resultIntent.putExtra("AlarmUnit", mAlarmTimeUnit);
        resultIntent.putExtra("AlarmRingtoneUri", mCurrentRingtoneUri.toString());
        resultIntent.putExtra("AlarmRingtoneVol", mAlarmRingtoneVolume);
        Log.d(TAG, "INTENT: ID=" + mAlarmId + " Name:"
                + mEditAlarmName.getText().toString() + " Unit:" + mAlarmTimeUnit
                + " Ringtone:" + mCurrentRingtoneUri.toString() + " Volume: " + mAlarmRingtoneVolume);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    private void resultCancel() {
        Intent resultIntent = getIntent();
        setResult(RESULT_CANCELED, resultIntent);
        finish();
    }

    private void ringtonePicker() {
        Intent ringtoneIntent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
        ringtoneIntent.putExtra
                (RingtoneManager.EXTRA_RINGTONE_TITLE,
                        getResources().getString(R.string.alarm_settings_ringtone_picker) + mTitle.getText());
        ringtoneIntent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false);
        ringtoneIntent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
        ringtoneIntent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, mCurrentRingtoneUri);
        ringtoneIntent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALL);
        startActivityForResult( ringtoneIntent, RINGTONE_INTENT_REQUEST);
    }

    @Override
    public void onClick(View v) {
        Log.d(TAG, "Clicked!!");
        switch (v.getId()) {
            case R.id.okAlarmSettings:
                Log.d(TAG, "Clicked OK!");
                stopRingtone();
                resultOk();
                break;
            case R.id.cancelAlarmSettings:
                Log.d(TAG, "Clicked CANCEL!");
                stopRingtone();
                resultCancel();
                break;
            case R.id.changeRingtone:
                Log.d(TAG, "Clicked RINGTONE!");
                stopRingtone();
                ringtonePicker();
                break;
        }
    }
}