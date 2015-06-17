package pl.appnode.napwatch;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ViewConfiguration;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static pl.appnode.napwatch.StateConstants.ALARMS_PREFS_FILE;
import static pl.appnode.napwatch.StateConstants.DEFAULT_TIMER_DURATION;
import static pl.appnode.napwatch.StateConstants.DEFAULT_TIMER_DURATION_MODIFIER;
import static pl.appnode.napwatch.StateConstants.MINUTE_IN_MILLIS;
import static pl.appnode.napwatch.StateConstants.OFF;
import static pl.appnode.napwatch.StateConstants.RESTORE;
import static pl.appnode.napwatch.StateConstants.SECOND;
import static pl.appnode.napwatch.StateConstants.MINUTE;
import static pl.appnode.napwatch.StateConstants.SECOND_IN_MILLIS;
import static pl.appnode.napwatch.StateConstants.SETTINGS_INTENT_REQUEST;
import static pl.appnode.napwatch.StateConstants.UPDATE;
import static pl.appnode.napwatch.ThemeSetup.themeSetup;
import static pl.appnode.napwatch.IsDarkTheme.isDarkTheme;

public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";
    protected static AlarmAdapter mAA;
    private static boolean sIsService;
    private static int[] sAlarmState = new int[4];
    private static boolean sThemeChange;

    private BroadcastReceiver mCountDownBroadcast = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateTime(intent);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        if (intent.hasExtra("id")) {
            if (mAA == null) {
                silentInit(intent.getIntExtra("id", 1));
            }
        }
        themeSetup(this);
        sThemeChange = isDarkTheme(this);
        showActionOverflowMenu();
        setContentView(R.layout.activity_main);
        RecyclerView recyclerList = (RecyclerView) findViewById(R.id.alarmList);
        recyclerList.setItemAnimator(null);
        recyclerList.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerList.setLayoutManager(llm);
        mAA = new AlarmAdapter(createList(), MainActivity.this);
        recyclerList.setAdapter(mAA);
        Log.d(TAG, "After Setting Adapter.");
    }

    @Override
    public void onResume() {
        super.onResume();
        registerReceiver(mCountDownBroadcast, new IntentFilter(AlarmBroadcastService.COUNTDOWN_BROADCAST));
        Log.d(TAG, "OnResume registered broadcast receiver.");
        if (sIsService) {
            updateTimeToFinishIntent();
            Log.d(TAG, "Time to finish update intent on active alarms.");
        }
        checkThemeChange();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    private void showActionOverflowMenu() {
        try {
            ViewConfiguration config = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
            if (menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        } catch (Exception e) {
            Log.d(TAG, e.getLocalizedMessage());
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_about) {
            AboutDialog.showDialog(MainActivity.this);
        }
        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            this.startActivity(settingsIntent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveSharedPrefs();
        unregisterReceiver(mCountDownBroadcast);
        Log.d(TAG, "OnPause unregistered broadcast receiver.");
        for (int i = 0; i < 4; i++) {
            if ( sAlarmState[i] == OFF) {
                WidgetUpdate.setButtonOff(i + 1, this);
            }
        }
    }

    @Override
    public void onStop() {
        try {
            unregisterReceiver(mCountDownBroadcast);
        } catch (Exception e) {
            // Receiver should be stopped in onPause
        }
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    @Override
    public void onDestroy() {
        saveSharedPrefs();
        if (sIsService) {
            Intent serviceIntent = new Intent(this, AlarmBroadcastService.class);
            stopService(serviceIntent);
            Log.d(TAG, "OnDestroy stopping service.");
        }
        super.onDestroy();
    }

    private void silentInit(int alarmId) {
        mAA = new AlarmAdapter(createList(), MainActivity.this);
        mAA.alarmAction(alarmId);
        Intent homeIntent = new Intent(Intent.ACTION_MAIN);
        homeIntent.addCategory(Intent.CATEGORY_HOME);
        homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(homeIntent);
    }

    private List<AlarmInfo> createList() {
        SharedPreferences alarmsPrefs = getSharedPreferences(ALARMS_PREFS_FILE, MODE_PRIVATE);
        String alarmPrefix;
        int timeFactor = SECOND_IN_MILLIS;

        List<AlarmInfo> result = new ArrayList<AlarmInfo>();
        for (int i = 1; i <= 4; i++) {
            AlarmInfo alarm = new AlarmInfo();
            alarmPrefix = "Alarm_" + i;
            alarm.mName = alarmsPrefs.getString(alarmPrefix, "Def Alarm " + i);
            alarm.mDuration = alarmsPrefs.getInt(alarmPrefix + "_Duration", DEFAULT_TIMER_DURATION
                    + (i * DEFAULT_TIMER_DURATION_MODIFIER));
            alarm.mTimeUnit = alarmsPrefs.getInt(alarmPrefix + "_TimeUnit", SECOND);
            switch (alarm.mTimeUnit) {
                case SECOND:  alarm.mTimeUnitSymbol = getResources().getString(R.string.time_unit_seconds);
                    timeFactor = SECOND_IN_MILLIS;
                    break;
                case MINUTE:  alarm.mTimeUnitSymbol = getResources().getString(R.string.time_unit_minutes);
                    timeFactor = MINUTE_IN_MILLIS;
                    break;
            }
            alarm.mIsOn = alarmsPrefs.getBoolean(alarmPrefix + "_State", false);
            alarm.mRingtoneUri = alarmsPrefs.getString(alarmPrefix + "_Ringtone", setRingtone());
            alarm.mRingtoneVolume = alarmsPrefs.getInt(alarmPrefix + "_RingtoneVol", setMaxVolume());
            alarm.mFullscreenOff = alarmsPrefs.getBoolean(alarmPrefix + "_FullScreenOff", true);
            alarm.mFinishTime = alarmsPrefs.getLong(alarmPrefix + "_FinishTime", 0);
            if (alarm.mFinishTime > SystemClock.elapsedRealtime()) {
                alarm.mDurationCounter = (int) (((alarm.mFinishTime - SystemClock.elapsedRealtime()) + timeFactor) / timeFactor);
                sAlarmState[i - 1] = RESTORE;
            } else alarm.mDurationCounter = alarm.mDuration;
            result.add(alarm);
            Log.d(TAG, "Result add #" + i);
        }
        Log.d(TAG, "RETURN!");
        return result;
    }

    protected void saveSharedPrefs() {
        SharedPreferences alarmsPrefs = getSharedPreferences(ALARMS_PREFS_FILE, MODE_PRIVATE);
        SharedPreferences.Editor editor = alarmsPrefs.edit();
        for (int i = 0; i <= 3; i++) {
            String alarmPrefix = "Alarm_" + (i + 1);
            AlarmInfo alarm = mAA.mAlarmList.get(i);
            editor.putString(alarmPrefix, alarm.mName);
            editor.putInt(alarmPrefix + "_Duration", alarm.mDuration);
            editor.putInt(alarmPrefix + "_TimeUnit", alarm.mTimeUnit);
            editor.putBoolean(alarmPrefix + "_State", alarm.mIsOn);
            if (alarm.mIsOn) {
                editor.putLong(alarmPrefix + "_FinishTime", alarm.mFinishTime);
            } else editor.putLong(alarmPrefix + "_FinishTime", 0);
            editor.putString(alarmPrefix + "_Ringtone", alarm.mRingtoneUri);
            editor.putInt(alarmPrefix + "_RingtoneVol", alarm.mRingtoneVolume);
            editor.putBoolean(alarmPrefix + "_FullScreenOff", alarm.mFullscreenOff);
            Log.d(TAG, "Create SharedPrefs: " + alarmPrefix + ": " + alarm.mDuration
                    + ": TimeUnit: " + alarm.mTimeUnitSymbol
                    + " :: isOn: " + alarm.mIsOn + " Vol: " + alarm.mRingtoneVolume + " FSOFF: " + alarm.mFullscreenOff);
        }
        editor.commit();
        Log.d(TAG, "COMMITED SharedPrefs.");
    }

    private static String setRingtone() {
        Uri ringtoneUri;
            ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            if (ringtoneUri == null) {
                ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                if (ringtoneUri == null) {
                    ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
                }
            }
            return ringtoneUri.toString();
    }

    private int setMaxVolume() {
        AudioManager audioManager = (AudioManager) this.getSystemService(AUDIO_SERVICE);
        return audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent resultIntent) {
        if (requestCode == SETTINGS_INTENT_REQUEST && resultCode == RESULT_OK && resultIntent.getExtras() != null) {
            Log.d(TAG, "Proper ResultIntent.");
            int position = resultIntent.getIntExtra("AlarmId", 0);
            AlarmInfo alarm = mAA.mAlarmList.get(position);
            alarm.mName = (String) resultIntent.getExtras().get("AlarmName");
            alarm.mFullscreenOff = (boolean) resultIntent.getExtras().get("AlarmFullscreenOff");
            alarm.mTimeUnit = (int) resultIntent.getExtras().get("AlarmUnit");
            if (alarm.mTimeUnit == SECOND) {
                alarm.mTimeUnitSymbol = getResources().getString(R.string.time_unit_seconds);
            } else alarm.mTimeUnitSymbol = getResources().getString(R.string.time_unit_minutes);
            alarm.mRingtoneUri = (String) resultIntent.getExtras().get("AlarmRingtoneUri");
            alarm.mRingtoneVolume = (int) resultIntent.getExtras().get("AlarmRingtoneVol");
            mAA.notifyItemChanged(position);
            saveSharedPrefs();
            WidgetUpdate.buttonTime(position + 1, alarm.mDuration + alarm.mTimeUnitSymbol, this);
        }
    }

    private void updateTime(Intent intent) {
        if (intent.getExtras() != null) {
            long timeToFinish = intent.getLongExtra("countdown", 0);
            int position = intent.getIntExtra("AlarmId", 0);
            Log.d(TAG, "Countdown time remaining: " + timeToFinish);
            AlarmInfo alarm = mAA.mAlarmList.get(position);
            alarm.mDurationCounter = (int) timeToFinish;
            if (!alarm.mIsOn & timeToFinish > 1) {alarm.mIsOn = true;}
            mAA.notifyItemChanged(position);
        }
    }

    public void updateTimeToFinishIntent() {
        Intent serviceIntent = new Intent(this, AlarmBroadcastService.class);
        serviceIntent.putExtra("AlarmCommand", UPDATE);
        startService(serviceIntent);
    }

    private boolean runningTimer() {
        for (int i = 0; i < 4; i++) {
            if (sAlarmState[i] != 0) return true;
        }
        return false;
    }

    private void checkThemeChange() {
        if (sThemeChange != isDarkTheme(this)) {
            saveSharedPrefs();
            finish();
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    public static int getAlarmState(int alarmId) {
        return sAlarmState[alarmId];
    }

    public static void setAlarmState(int alarmId, int state) {
        sAlarmState[alarmId] = state;
    }

    public static boolean isService() {
        return sIsService;
    }

    public static void setIsService(boolean isService) {
        sIsService = isService;
    }
}