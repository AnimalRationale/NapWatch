package pl.appnode.napwatch;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

public class AlarmBroadcastService extends Service {

    private final static String TAG = "::AlarmBroadcastService";

    public static final String COUNTDOWN_BROADCAST = "pl.appnode.napwatch";
    Intent mBI = new Intent(COUNTDOWN_BROADCAST);
    int notifyID = 0;
    int mAlarmId;
    String mAlarmName;
    int mAlarmDuration;

    Uri mAlert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
    Ringtone mRingtone;

    int mStartMode;       // indicates how to behave if the service is killed

    CountDownTimer mCDT = null;

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

        mAlarmId = (Integer) intent.getExtras().get("AlarmId");
        mAlarmName = intent.getExtras().get("AlarmName").toString();
        mAlarmDuration = (Integer) intent.getExtras().get("AlarmDuration");

        if(mAlert == null){
            mAlert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            if(mAlert == null) {
                mAlert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            }
        }

        Intent resultIntent = new Intent(this, MainActivity.class);
        resultIntent.setAction(Intent.ACTION_MAIN);
        resultIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0, resultIntent, 0);
        notifyID = mAlarmId;
        final NotificationManager mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        final NotificationCompat.Builder mNotify = new NotificationCompat.Builder(this)
                .setContentTitle(mAlarmDuration + getResources().getString(R.string.notification_title))
                .setContentText(mAlarmName + getResources().getString(R.string.notification_text02) + mAlarmDuration + getResources().getString(R.string.notification_text03_seconds))
                .setSmallIcon(R.drawable.ic_alarm_add_grey600_24dp)
                .setContentIntent(resultPendingIntent); // TODO: use resources in smarter way :) !
        mNM.notify(notifyID, mNotify.build());

        mRingtone = RingtoneManager.getRingtone(getApplicationContext(), mAlert);

        MainActivity.isService = true;
        Log.d(TAG, "Setting isService TRUE.");
        Log.d(TAG, "Starting timer for [" + mAlarmId + "] = " + mAlarmName  + " with duration " + mAlarmDuration + " minutes." );

        mCDT = new CountDownTimer(mAlarmDuration * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {

                Log.d(TAG, "Countdown seconds remaining: " + millisUntilFinished / 1000);
                mBI.putExtra("AlarmID", mAlarmId);
                mBI.putExtra("countdown", millisUntilFinished / 1000);
                sendBroadcast(mBI);
                mNotify.setContentTitle(millisUntilFinished / 1000 + " minutes left");
                mNM.notify(notifyID, mNotify.build());
            }

            @Override
            public void onFinish() {
                mRingtone.play();
                mNotify.setContentTitle(mAlarmName + getResources().getString(R.string.notification_text02) + mAlarmDuration + getResources().getString(R.string.notification_text03_finished_seconds))
                        .setContentText(getResources().getString(R.string.notification_text_finished));
                mNM.notify(notifyID, mNotify.build());
                Log.d(TAG, "Timer [" + mAlarmId + "] finished.");

                mBI.putExtra("AlarmID", mAlarmId);
                mBI.putExtra("countdown", Long.valueOf(0)); // working on millisecs/Long will generate warning with int;
                sendBroadcast(mBI);
                Log.d(TAG, "Countdown finished.");
            }
        };
        mCDT.start();
        return mStartMode;
    }

    @Override
    public void onDestroy() {
        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(notifyID);
        mCDT.cancel();
        mRingtone.stop();
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
