package pl.appnode.napwatch;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import static pl.appnode.napwatch.StateConstants.ON;

public class AlarmBroadcastService extends Service {

    private final static String TAG = "::AlarmBroadcastService";

    public static final String COUNTDOWN_BROADCAST = "pl.appnode.napwatch";
    int notifyID = 0;
    int mAlarmId;
    String mAlarmName;
    int mAlarmDuration;
    String mAlarmUnit;
    int mStartMode;       // indicates how to behave if the service is killed
    AlarmCountDownTimer mCDT = null;
    AlarmCountDownTimer[] mAlarms = new AlarmCountDownTimer[4];

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (MainActivity.isService) {
            Log.d(TAG, "Service already running! Ignoring.");
            return mStartMode;
        }
        Log.d(TAG,"Starting service.");

        MainActivity.isService = true;
        Log.d(TAG, "Setting isService TRUE.");

        mAlarmId = (Integer) intent.getExtras().get("AlarmId");
        mAlarmName = intent.getExtras().get("AlarmName").toString();
        mAlarmDuration = (Integer) intent.getExtras().get("AlarmDuration");
        mAlarmUnit = intent.getExtras().get("AlarmUnit").toString();

        mCDT = new AlarmCountDownTimer(mAlarmDuration * 1000, 1000, mAlarmId, mAlarmName, mAlarmUnit, mAlarmDuration, this);
        mCDT.start();
        MainActivity.AlarmState[mAlarmId] = ON;
        return mStartMode;
    }

    @Override
    public void onDestroy() {
        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(notifyID);
        mCDT.stopRingtone();
        mCDT.cancel();
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