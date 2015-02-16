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

    public void destroyAlarm(int alarmId) {
        if (alarmId >= 0 & alarmId < 4) { mAlarms[alarmId] = null; }
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG,"Starting service.");
        MainActivity.isService = true;
        Log.d(TAG, "Setting isService TRUE.");

        mAlarmId = (Integer) intent.getExtras().get("AlarmId");
        mAlarmCommand = (Integer) intent.getExtras().get("AlarmCommand");
        if (mAlarms[mAlarmId] != null & mAlarmCommand == STOP) {
            mAlarms[mAlarmId].stopRingtone();
            mAlarms[mAlarmId].cancel();
            mAlarms[mAlarmId] = null;
            MainActivity.AlarmState[mAlarmId] = OFF;
            return mStartMode;
        }
        if (mAlarms[mAlarmId] == null & mAlarmCommand == START) {
            mAlarmName = intent.getExtras().get("AlarmName").toString();
            mAlarmDuration = (Integer) intent.getExtras().get("AlarmDuration");
            mAlarmUnit = intent.getExtras().get("AlarmUnit").toString();
            notifyId = mAlarmId;
            mAlarms[mAlarmId] = new AlarmCountDownTimer(mAlarmDuration * 1000, 1000, mAlarmId, mAlarmName, mAlarmUnit, mAlarmDuration, this);
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
        notificationManager.cancel(notifyId);
        mAlarms[mAlarmId].stopRingtone();
        mAlarms[mAlarmId].cancel();
        mAlarms[mAlarmId] = null;
        Log.d(TAG, "CountDownTimer for alarm [" + mAlarmId + "] cancelled.");
        Log.d(TAG, "Setting isService FALSE.");
        MainActivity.isService = false;
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }
}