package pl.appnode.napwatch;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import static pl.appnode.napwatch.PreferenceSetup.orientationSetup;
import static pl.appnode.napwatch.StateConstants.MINUTE;
import static pl.appnode.napwatch.StateConstants.OFF;
import static pl.appnode.napwatch.StateConstants.RINGTONE_INTENT_REQUEST;
import static pl.appnode.napwatch.StateConstants.SECOND;
import static pl.appnode.napwatch.PreferenceSetup.themeSetup;
import static pl.appnode.napwatch.AppSettings.isDarkTheme;

public class AlarmSettingsActivity extends Activity implements View.OnClickListener {

    private static final String TAG = "AlarmSettingsActivity";
    private int mAlarmId;
    private String mAlarmName;
    private int mAlarmTimeUnit;
    private String mAlarmRingtoneUri;
    private int mAlarmRingtoneVolume;
    private boolean mAlarmFullscreenOff;
    private int mOriginalVolume;
    private AudioManager mAudioManager;
    private Uri mCurrentRingtoneUri;
    private Ringtone mRingtone;
    private String mRingtoneName;
    private TextView mTitle;
    private EditText mEditAlarmName;
    private Switch mFullscreenOffSwitch;
    private RadioButton mRbSeconds;
    private Button mRingtoneTextButton;
    private ImageButton mPlayStopButton;
    private boolean mIsPlaying = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        themeSetup(this); // Setting theme
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.alarm_settings_dialog);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        layoutParams.dimAmount=0.8f;
        getWindow().setAttributes(layoutParams);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        this.setFinishOnTouchOutside(false);
        colorFixForMaterialDark();
        mAudioManager = (AudioManager) this.getSystemService(this.AUDIO_SERVICE);
        mOriginalVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_ALARM);
        mTitle = (TextView) findViewById(R.id.alarmEditTitle);
        mEditAlarmName = (EditText) findViewById(R.id.alarmNameText);
        mFullscreenOffSwitch = (Switch) findViewById(R.id.switchFullscreenOff);
        mRbSeconds = (RadioButton) findViewById(R.id.radioSeconds);
        RadioButton rbMinutes = (RadioButton) findViewById(R.id.radioMinutes);
        mRingtoneTextButton = (Button) findViewById(R.id.changeRingtone);
        mRingtoneTextButton.setOnClickListener(this);
        SeekBar volumeSeekBar = (SeekBar) findViewById(R.id.volumeSeekBar);
        mPlayStopButton = (ImageButton) findViewById(R.id.playAlarmSettings);
        mPlayStopButton.setOnClickListener(this);
        Button buttonOk = (Button) findViewById(R.id.okAlarmSettings);
        buttonOk.setOnClickListener(this);
        Button buttonCancel = (Button) findViewById(R.id.cancelAlarmSettings);
        buttonCancel.setOnClickListener(this);
        getSettingsIntentData (getIntent());
        mCurrentRingtoneUri = setNotNullRingtone(mAlarmRingtoneUri);
        mRingtone = RingtoneManager.getRingtone(this.getApplicationContext(), mCurrentRingtoneUri);
        mRingtoneName =  mRingtone.getTitle(this.getApplicationContext());
        mRingtoneTextButton.setText(mRingtoneName);
        mRingtone.setStreamType(AudioManager.STREAM_ALARM);
        volumeSeekBar.setMax(mAudioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM));
        volumeSeekBar.setProgress(mAlarmRingtoneVolume);
        volumeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mAlarmRingtoneVolume = progress;
                setVolume();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        mTitle.setText(R.string.alarm_settings_title);
        mTitle.append("" + (mAlarmId + 1));
        mEditAlarmName.setText(mAlarmName);
        mFullscreenOffSwitch.setChecked(mAlarmFullscreenOff);
        if (mAlarmTimeUnit == SECOND) {
            mRbSeconds.toggle();
        } else rbMinutes.toggle();
        Log.d(TAG, "AlarmSettingsActivity started.");
    }

    public void onResume() {
        super.onResume();
        mOriginalVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_ALARM);
        if (MainActivity.getAlarmState(mAlarmId) != OFF) {finish();}
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent resultIntent) {
        Log.d(TAG, "onActivityResult fo Ringtone Picker.");
        if (requestCode == RINGTONE_INTENT_REQUEST && resultCode == RESULT_OK && resultIntent.getExtras() != null) {
            mCurrentRingtoneUri = resultIntent.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            mRingtone = RingtoneManager.getRingtone(this.getApplicationContext(), mCurrentRingtoneUri);
            mRingtoneName =  mRingtone.getTitle(this.getApplicationContext());
            mRingtoneTextButton.setText(mRingtoneName);
            Log.d(TAG, "Ringtone Result: " + mRingtoneName);
        }
    }

    private Uri setNotNullRingtone(String ringtoneIn) {
        Uri ringtoneOut;
        if (ringtoneIn == null) {
            ringtoneOut = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            if (ringtoneOut == null) {
                ringtoneOut = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                if (ringtoneOut == null) {
                    ringtoneOut = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
                }
            }
            return ringtoneOut;
        }
        return Uri.parse(ringtoneIn);
    }

    private void setVolume() {
        if (mAlarmRingtoneVolume <= 0) {
            mAudioManager.setStreamVolume(AudioManager.STREAM_ALARM, 0, 0);
        } else if (mAlarmRingtoneVolume >= mAudioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM)) {
            mAudioManager.setStreamVolume(AudioManager.STREAM_ALARM, mAudioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM), 0);
        } else {
            mAudioManager.setStreamVolume(AudioManager.STREAM_ALARM, mAlarmRingtoneVolume, 0);}
        Log.d(TAG, "Set ringtone volume: " + mAlarmRingtoneVolume);
    }

    private void playRingtone() {
        setVolume();
        mPlayStopButton.setBackgroundResource(R.drawable.round_button_red);
        mPlayStopButton.setImageResource(R.drawable.ic_stop_white_36dp);
        mIsPlaying = true;
        mRingtone.play();
    }

    private void stopRingtone() {
        mRingtone.stop();
        mIsPlaying = false;
        mAudioManager.setStreamVolume(AudioManager.STREAM_ALARM, mOriginalVolume, 0);
        mPlayStopButton.setBackgroundResource(R.drawable.round_button_green);
        mPlayStopButton.setImageResource(R.drawable.ic_play_arrow_white_36dp);
        Log.d(TAG, "Restored ringtone volume: " + mOriginalVolume);
    }

    private void getSettingsIntentData(Intent settingsIntent) {
        if (settingsIntent.getExtras() != null) {
            mAlarmId = (int) settingsIntent.getExtras().get("AlarmId");
            mAlarmName = (String) settingsIntent.getExtras().get("AlarmName");
            mAlarmTimeUnit = (int) settingsIntent.getExtras().get("AlarmUnit");
            mAlarmFullscreenOff = (boolean) settingsIntent.getExtras().get("AlarmFullscreenOff");
            mAlarmRingtoneUri = (String) settingsIntent.getExtras().get("AlarmRingtoneUri");
            if (settingsIntent.getExtras().get("AlarmRingtoneVol") != null) {
                mAlarmRingtoneVolume = (int) settingsIntent.getExtras().get("AlarmRingtoneVol");
            } else {
                mAlarmRingtoneVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_ALARM);
            }
        }
    }

    private void resultOk() {
        Intent resultIntent = getIntent();
        resultIntent.putExtra("AlarmId", mAlarmId);
        resultIntent.putExtra("AlarmName", mEditAlarmName.getText().toString());
        mAlarmFullscreenOff = mFullscreenOffSwitch.isChecked();
        resultIntent.putExtra("AlarmFullscreenOff", mAlarmFullscreenOff);
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
        startActivityForResult(ringtoneIntent, RINGTONE_INTENT_REQUEST);
    }

    private void colorFixForMaterialDark() {
        if (isDarkTheme(this) & Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ImageView[] imageViews = new ImageView[2];
            imageViews[0] = (ImageView) findViewById(R.id.volumeIconMute);
            imageViews[1] = (ImageView) findViewById(R.id.volumeIconUp);
            for (int i = 0; i < imageViews.length; i++) {
                GradientDrawable drawable = (GradientDrawable) imageViews[i].getBackground().getCurrent();
                drawable.setColor(getResources().getColor(R.color.primary_light));
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.okAlarmSettings:
                stopRingtone();
                resultOk();
                break;
            case R.id.cancelAlarmSettings:
                stopRingtone();
                resultCancel();
                break;
            case R.id.changeRingtone:
                stopRingtone();
                ringtonePicker();
                break;
            case R.id.playAlarmSettings:
                if (mRingtone.isPlaying() || mIsPlaying) {
                    stopRingtone();
                } else playRingtone();
                break;
        }
    }
}