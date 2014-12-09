package pl.appnode.napwatch;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.util.Log;

public class AlarmBroadcastService extends Service {

    private final static String TAG = "::AlarmBroadcastService";

    public static final String COUNTDOWN_BROADCAST = "pl.appnode.napwatch";
    Intent mBI = new Intent(COUNTDOWN_BROADCAST);
    int mStartMode;       // indicates how to behave if the service is killed

    CountDownTimer mCDT = null;

    @Override
    public void onCreate() {
        super.onCreate();


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Starting timer.");
        extras = intent.getExtras();
        mCDT = new CountDownTimer(30000, 5000) {
            @Override
            public void onTick(long millisUntilFinished) {

                Log.d(TAG, "Countdown seconds remaining: " + millisUntilFinished / 1000);
                mBI.putExtra("countdown", millisUntilFinished);
                sendBroadcast(mBI);
            }

            @Override
            public void onFinish() {
                Log.d(TAG, "Timer finished.");
            }
        };
        mCDT.start();

        return mStartMode;
    }

    @Override
    public void onDestroy() {

        mCDT.cancel();
        Log.d(TAG, "CountDownTimer cancelled.");
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }
}