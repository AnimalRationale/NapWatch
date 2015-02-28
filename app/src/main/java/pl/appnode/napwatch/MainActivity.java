package pl.appnode.napwatch;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

import static pl.appnode.napwatch.StateConstants.SECOND;
import static pl.appnode.napwatch.StateConstants.MINUTE;
import static pl.appnode.napwatch.StateConstants.SETTINGS_INTENT_REQUEST;
import static pl.appnode.napwatch.StateConstants.UPDATE;

public class MainActivity extends Activity {

    public static final String ALARMS_PREFS_FILE = "AlarmsPrefsFile";
    private static final String TAG = "MainActivity";
    protected AlarmAdapter mAA;
    public static boolean isService;
    public static int[] AlarmState = new int[4];

    private BroadcastReceiver mCountDownBroadcast = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateTime(intent);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        RecyclerView recList = (RecyclerView) findViewById(R.id.alarmList);
        recList.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);
        Log.d(TAG, "Before Setting Adapter.");
        mAA = new AlarmAdapter(createList(), MainActivity.this);
        recList.setAdapter(mAA);
        Log.d(TAG, "After Setting Adapter.");
    }

    @Override
    public void onResume() {
        super.onResume();
        registerReceiver(mCountDownBroadcast, new IntentFilter(AlarmBroadcastService.COUNTDOWN_BROADCAST));
        Log.d(TAG, "OnResume registered broadcast receiver.");
        if (isService) {
            Intent serviceIntent = new Intent(this, AlarmBroadcastService.class);
            serviceIntent.putExtra("AlarmCommand", UPDATE);
            startService(serviceIntent);
            Log.d(TAG, "Time to finish update intent on active alarms.");
        };
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_about) {
            AboutDialog.showDialog(MainActivity.this);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveSharedPrefs();
        unregisterReceiver(mCountDownBroadcast);
        Log.d(TAG, "OnPause unregistered broadcast receiver.");
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
        if (isService) {
            Intent serviceIntent = new Intent(this, AlarmBroadcastService.class);
            stopService(serviceIntent);
            Log.d(TAG, "OnDestroy stopping service.");
        };
        super.onDestroy();
    }

    private List<AlarmInfo> createList() {
        Log.d(TAG, "Create list.");

        SharedPreferences alarmsPrefs = getSharedPreferences(ALARMS_PREFS_FILE, MODE_PRIVATE);
        String alarmPrefix;
        List<AlarmInfo> result = new ArrayList<AlarmInfo>();
        for (int i = 1; i <= 4; i++) {
            AlarmInfo ai = new AlarmInfo();
            alarmPrefix = "Alarm_" + i;
            ai.mName = alarmsPrefs.getString(alarmPrefix, "Def Alarm " + i);
            ai.mDuration = alarmsPrefs.getInt(alarmPrefix + "_Duration", 12 + (i*4));
            ai.mDurationCounter = ai.mDuration;
            ai.mTimeUnit = alarmsPrefs.getInt(alarmPrefix + "_TimeUnit", SECOND);
            switch (ai.mTimeUnit) {
                case SECOND:  ai.mTimeUnitSymbol = getResources().getString(R.string.time_unit_seconds);
                    break;
                case MINUTE:  ai.mTimeUnitSymbol = getResources().getString(R.string.time_unit_minutes);
                    break;
            }
            ai.mIsOn = alarmsPrefs.getBoolean(alarmPrefix + "_State", false);
            ai.mRingtoneUri = alarmsPrefs.getString(alarmPrefix + "_Ringtone", null);
            Log.d(TAG, "before Result add #" + i);
            result.add(ai);
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
            editor.putString(alarmPrefix + "_Ringtone", alarm.mRingtoneUri);
            Log.d(TAG, "Create SharedPrefs: " + alarmPrefix + ": " + alarm.mDuration + ": TimeUnit: " + alarm.mTimeUnitSymbol + " :: isOn: " + alarm.mIsOn);
        }
        editor.commit();
        Log.d(TAG, "COMMITED SharedPrefs.");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent resultIntent) {
        if (requestCode == SETTINGS_INTENT_REQUEST && resultCode == RESULT_OK && resultIntent.getExtras() != null) {
            Log.d(TAG, "Proper ResultIntent.");
            int position = resultIntent.getIntExtra("AlarmId", 0);
            AlarmInfo alarm = mAA.mAlarmList.get(position);
            alarm.mName = (String) resultIntent.getExtras().get("AlarmName");
            alarm.mTimeUnit = (int) resultIntent.getExtras().get("AlarmUnit");
            if (alarm.mTimeUnit == SECOND) {
                alarm.mTimeUnitSymbol = getResources().getString(R.string.time_unit_seconds);
            } else alarm.mTimeUnitSymbol = getResources().getString(R.string.time_unit_minutes);
            alarm.mRingtoneUri = (String) resultIntent.getExtras().get("AlarmRingtoneUri");
            mAA.notifyItemChanged(position);
            saveSharedPrefs();
        }
    }

    private void updateTime(Intent intent) {
        if (intent.getExtras() != null) {
            long timeToFinish = intent.getLongExtra("countdown", 0);
            int position = intent.getIntExtra("AlarmId", 0);
            Log.d(TAG, "Countdown time remaining: " +  timeToFinish);
            AlarmInfo alarm = mAA.mAlarmList.get(position);
            alarm.mDurationCounter = (int) timeToFinish;
            if (!alarm.mIsOn & timeToFinish > 1) {alarm.mIsOn = true;}
            mAA.notifyItemChanged(position);
        }
    }
}