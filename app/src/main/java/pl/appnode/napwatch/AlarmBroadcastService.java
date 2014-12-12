package pl.appnode.napwatch;

import android.app.Service;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.util.Log;

public class AlarmBroadcastService extends Service {

    private final static String TAG = "::AlarmBroadcastService";

    public static final String COUNTDOWN_BROADCAST = "pl.appnode.napwatch";
    Intent mBI = new Intent(COUNTDOWN_BROADCAST);
    int mAlarmId;
    String mAlarmName;
    int mAlarmDuration;

    Uri mAlert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
    Ringtone mRingtone;

    int mStartMode;       // indicates how to behave if the service is killed

    CountDownTimer mCDT = null;

    @Override
    public void onCreate() {
        super.onCreate();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if(mAlert == null){
            mAlert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            if(mAlert == null) {
                mAlert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            }
        }
        mRingtone = RingtoneManager.getRingtone(getApplicationContext(), mAlert);

        mAlarmId = (Integer) intent.getExtras().get("AlarmId");
        mAlarmName = intent.getExtras().get("AlarmName").toString();
        mAlarmDuration = (Integer) intent.getExtras().get("AlarmDuration");

        Log.d(TAG, "Starting timer for " + mAlarmId + " = " + mAlarmName  + " with duration " + mAlarmDuration + " minutes." );
        mCDT = new CountDownTimer(mAlarmDuration * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {

                Log.d(TAG, "Countdown seconds remaining: " + millisUntilFinished / 1000);
                mBI.putExtra("countdown", millisUntilFinished);
                sendBroadcast(mBI);
            }

            @Override
            public void onFinish() {
                mRingtone.play();
                Log.d(TAG, "Timer finished.");
            }
        };
        mCDT.start();

        return mStartMode;
    }

    @Override
    public void onDestroy() {

        mCDT.cancel();
        mRingtone.stop();
        Log.d(TAG, "CountDownTimer cancelled.");
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }
}