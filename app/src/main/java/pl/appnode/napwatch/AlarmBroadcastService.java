package pl.appnode.napwatch;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import static pl.appnode.napwatch.StateConstants.OFF;
import static pl.appnode.napwatch.StateConstants.ON;
import static pl.appnode.napwatch.StateConstants.START;
import static pl.appnode.napwatch.StateConstants.STOP;

public class AlarmBroadcastService extends Service {

    private final static String TAG = "::AlarmBroadcastService";

    public static final String COUNTDOWN_BROADCAST = "pl.appnode.napwatch";
    int notifyId = 0;
    int mAlarmId;
    String mAlarmName;
    int mAlarmDuration;
    String mAlarmUnit;
    int mAlarmCommand;
    int mStartMode;       // indicates how to behave if the service is killed
    
    AlarmCountDownTimer[] mAlarms = new AlarmCountDownTimer[4];

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG,"Using service.");
        MainActivity.isService = true;
        Log.d(TAG, "Setting isService TRUE.");
        int timeFactor = 0;
        mAlarmId = (Integer) intent.getExtras().get("AlarmId");
        mAlarmCommand = (Integer) intent.getExtras().get("AlarmCommand");
        if (mAlarms[mAlarmId] != null & mAlarmCommand == STOP) {
            stopAlarm(mAlarmId);
            return mStartMode;
        }
        if (mAlarms[mAlarmId] == null & mAlarmCommand == START) {
            mAlarmName = intent.getExtras().get("AlarmName").toString();
            mAlarmDuration = (Integer) intent.getExtras().get("AlarmDuration");
            mAlarmUnit = intent.getExtras().get("AlarmUnit").toString();
            timeFactor = (Integer) intent.getExtras().get("AlarmFactor");
            Log.d(TAG, "TimeFactor: " + timeFactor);
            notifyId = mAlarmId;
            mAlarms[mAlarmId] = new AlarmCountDownTimer(mAlarmDuration * timeFactor, timeFactor, mAlarmId, mAlarmName, mAlarmUnit, mAlarmDuration, this);
            mAlarms[mAlarmId].start();
            MainActivity.AlarmState[mAlarmId] = ON;
            return mStartMode;
        }
        Log.d(TAG, "Command not recognized.");
        return mStartMode;
    }

    @Override
    public void onDestroy() {
        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        for (int i = 0; i < 4; i++) {
            mAlarms[i].stopRingtone();
            mAlarms[i].cancel();
            mAlarms[i] = null;
            notificationManager.cancel(i);
            MainActivity.AlarmState[i] = OFF;
        }
        Log.d(TAG, "CountDownTimer for alarm [" + mAlarmId + "] cancelled.");
        Log.d(TAG, "Setting isService FALSE.");
        MainActivity.isService = false;
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    public void stopAlarm(int alarmId) {
        mAlarms[alarmId].stopRingtone();
        mAlarms[alarmId].cancel();
        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(alarmId);
        mAlarms[alarmId] = null;
        MainActivity.AlarmState[alarmId] = OFF;
    }
}