package pl.appnode.napwatch;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import static pl.appnode.napwatch.StateConstants.EMPTY;
import static pl.appnode.napwatch.StateConstants.OFF;
import static pl.appnode.napwatch.StateConstants.ON;
import static pl.appnode.napwatch.StateConstants.START;
import static pl.appnode.napwatch.StateConstants.STOP;
import static pl.appnode.napwatch.StateConstants.UPDATE;
import static pl.appnode.napwatch.StateConstants.TIME_DEVIATION_FOR_LAST_TICK;

public class AlarmBroadcastService extends Service {

    private final static String TAG = "::AlarmBroadcastService";

    public static final String COUNTDOWN_BROADCAST = "pl.appnode.napwatch";
    private int mAlarmId;
    private String mAlarmName;
    private int mAlarmDuration;
    private String mAlarmUnit;
    private int mTimeFactor = 0;
    private String mAlarmRingtone;
    private int mAlarmRingtoneVolume;
    private int mAlarmCommand;
    private int mStartMode;
    
    private AlarmCountDownTimer[] mAlarms = new AlarmCountDownTimer[4];

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG,"Using service.");
        mAlarmId = EMPTY;
        MainActivity.setIsService(true);
        Log.d(TAG, "Setting sIsService TRUE.");
        if (intent.getExtras().get("AlarmId") != null) {mAlarmId = (Integer) intent.getExtras().get("AlarmId");}
        if (intent.getExtras().get("AlarmCommand") != null) {mAlarmCommand = (Integer) intent.getExtras().get("AlarmCommand");}
        Log.d(TAG, "mAlarmId = " + mAlarmId);
        if (mAlarmId != EMPTY && mAlarms[mAlarmId] != null && mAlarmCommand == STOP) {
            stopAlarm(mAlarmId);
            return mStartMode;
        } else
        if (mAlarmId != EMPTY && mAlarms[mAlarmId] == null && mAlarmCommand == START) {
            getStartAlarmIntentData(intent);
            mAlarms[mAlarmId] = new AlarmCountDownTimer(mAlarmDuration * mTimeFactor,
                    mTimeFactor - (mTimeFactor / TIME_DEVIATION_FOR_LAST_TICK),
                    mAlarmId, mAlarmName, mAlarmUnit, mAlarmDuration, mAlarmRingtone, mAlarmRingtoneVolume, this);
            mAlarms[mAlarmId].start();
            MainActivity.setAlarmState(mAlarmId, ON);
            return mStartMode;
        } else
        if (mAlarmId == EMPTY && mAlarmCommand == UPDATE) {
            for (int i = 0; i < 4; i++) {
                if (mAlarms[i] != null) {
                    mAlarms[i].broadcastTimeUntilFinished();
                }
            }
            return mStartMode;
        }
        Log.d(TAG, "Command not recognized.");
        return mStartMode;
    }

    @Override
    public void onDestroy() {
        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        for (int i = 0; i < 4; i++) {
            if (mAlarms[i] != null) {
                mAlarms[i].stopRingtone();
                mAlarms[i].cancel();
                mAlarms[i] = null;
                notificationManager.cancel(i);
                MainActivity.setAlarmState(i, OFF);
            }
        }
        Log.d(TAG, "CountDownTimer for alarm [" + mAlarmId + "] cancelled.");
        Log.d(TAG, "Setting sIsService FALSE.");
        MainActivity.setIsService(false);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    private void getStartAlarmIntentData (Intent intent) {
        mAlarmName = intent.getExtras().get("AlarmName").toString();
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
        MainActivity.setAlarmState(alarmId, OFF);
    }
}