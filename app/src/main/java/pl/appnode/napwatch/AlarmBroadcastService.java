package pl.appnode.napwatch;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;

import static pl.appnode.napwatch.StateConstants.ALARMS_PREFS_FILE;
import static pl.appnode.napwatch.StateConstants.EMPTY;
import static pl.appnode.napwatch.StateConstants.OFF;
import static pl.appnode.napwatch.StateConstants.ON;
import static pl.appnode.napwatch.StateConstants.RESTORE;
import static pl.appnode.napwatch.StateConstants.START;
import static pl.appnode.napwatch.StateConstants.STOP;
import static pl.appnode.napwatch.StateConstants.UPDATE;
import static pl.appnode.napwatch.StateConstants.TIME_DEVIATION_FOR_LAST_TICK;

public class AlarmBroadcastService extends Service {

    private final static String TAG = "::AlarmBroadcastService";

    public static final String COUNTDOWN_BROADCAST = "pl.appnode.napwatch";
    private int mAlarmId;
    private boolean mAlarmFullscreenOff;
    private String mAlarmName;
    private int mAlarmDuration;
    private String mAlarmUnit;
    private int mTimeFactor = 0;
    private String mAlarmRingtone;
    private int mAlarmRingtoneVolume;
    private int mAlarmCommand;
    
    private AlarmCountDownTimer[] mAlarms = new AlarmCountDownTimer[4];

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int startMode = START_STICKY;
        mAlarmId = EMPTY;
        MainActivity.setIsAlarmBroadcastService(true);
        Log.d(TAG, "Setting sIsService TRUE.");
        if (intent.getExtras().get("AlarmId") != null) {mAlarmId = (Integer) intent.getExtras().get("AlarmId");}
        if (intent.getExtras().get("AlarmCommand") != null) {mAlarmCommand = (Integer) intent.getExtras().get("AlarmCommand");}
        Log.d(TAG, "mAlarmId = " + mAlarmId);
        if (mAlarmId != EMPTY && mAlarms[mAlarmId] != null && mAlarmCommand == STOP) {
            cancelAlarm(mAlarmId);
            return startMode;
        } else
        if (mAlarmId != EMPTY && mAlarms[mAlarmId] == null && mAlarmCommand == START) {
            getStartAlarmIntentData(intent);
            mAlarms[mAlarmId] = new AlarmCountDownTimer(mAlarmDuration * mTimeFactor,
                    mTimeFactor - (mTimeFactor / TIME_DEVIATION_FOR_LAST_TICK),
                    mAlarmId, mAlarmName, mAlarmFullscreenOff, mAlarmUnit, mAlarmDuration, mAlarmRingtone, mAlarmRingtoneVolume, this.getApplicationContext());
            mAlarms[mAlarmId].start();
            MainActivity.setAlarmState(mAlarmId, ON);
            return startMode;
        } else
        if (mAlarmId == EMPTY && mAlarmCommand == UPDATE) {
            for (int i = 0; i < 4; i++) {
                if (mAlarms[i] != null) {
                    mAlarms[i].broadcastTimeUntilFinished();
                }
            }
            return startMode;
        }
        Log.d(TAG, "Command not recognized.");
        return startMode;
    }

    @Override
    public void onDestroy() {
        for (int i = 0; i < 4; i++) {
            if (mAlarms[i] != null) {
                stopAlarm(i);
            }
        }
        Log.d(TAG, "CountDownTimer for alarm [" + mAlarmId + "] cancelled.");
        MainActivity.setIsAlarmBroadcastService(false);
        AlarmReceiver.releaseLock();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    private void getStartAlarmIntentData (Intent intent) {
        mAlarmName = intent.getExtras().get("AlarmName").toString();
        mAlarmFullscreenOff = (boolean) intent.getExtras().get("AlarmFullscreenOff");
        mAlarmDuration = (int) intent.getExtras().get("AlarmDuration");
        mAlarmUnit = intent.getExtras().get("AlarmUnit").toString();
        mTimeFactor = (int) intent.getExtras().get("AlarmFactor");
        Log.d(TAG, "TimeFactor: " + mTimeFactor);
        mAlarmRingtone = intent.getExtras().get("AlarmRingtone").toString();
        mAlarmRingtoneVolume = (int) intent.getExtras().get("AlarmRingtoneVol");
    }

    public void stopAlarm(int alarmId) {
        mAlarms[alarmId].stopRingtone();
        mAlarms[alarmId].cancel();
        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(alarmId);
        mAlarms[alarmId] = null;
        if (MainActivity.getAlarmState(alarmId) != RESTORE) {
            MainActivity.setAlarmState(alarmId, OFF);
        }
        Log.d(TAG, "Alarm stopped #" + alarmId + " alarm state: " + MainActivity.getAlarmState(alarmId));
    }

    public void cancelAlarm(int alarmId) {mAlarms[alarmId].stopRingtone();
        mAlarms[alarmId].cancel();
        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(alarmId);
        mAlarms[alarmId] = null;
        if (MainActivity.getAlarmState(alarmId) != RESTORE) {
            MainActivity.setAlarmState(alarmId, OFF);
            SharedPreferences alarmsPrefs = getSharedPreferences(ALARMS_PREFS_FILE, MODE_PRIVATE);
            SharedPreferences.Editor editor = alarmsPrefs.edit();
            String alarmPrefix = "Alarm_" + (mAlarmId + 1);
            editor.putLong(alarmPrefix + "_FinishTime", 0);
            editor.commit();
        }
        Log.d(TAG, "Alarm stopped #" + alarmId + " alarm state: " + MainActivity.getAlarmState(alarmId));

    }
}