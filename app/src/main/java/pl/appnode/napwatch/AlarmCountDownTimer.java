package pl.appnode.napwatch;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.CountDownTimer;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class AlarmCountDownTimer extends CountDownTimer {
    private final static String TAG = "::AlarmCountdownTimer";
    public static final String COUNTDOWN_BROADCAST = "pl.appnode.napwatch";

    Intent mBI = new Intent(COUNTDOWN_BROADCAST);
    int notifyId = 0;
    NotificationManager mNM;
    NotificationCompat.Builder mNotify;
    int mAlarmId;
    String mAlarmName;
    int mAlarmDuration;
    int mTimeUnitFactor;
    String mAlarmUnit;
    Uri mAlert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
    Ringtone mRingtone;
    Context mContext;
    boolean isFinished = false;
    long mTimeUntilFinished;

    public AlarmCountDownTimer (long millisInFuture, long countDownInterval, int alarmId, String title, String alarmUnit, int alarmDuration, Context context) {
        super(millisInFuture, countDownInterval);
        mAlarmId = alarmId;
        mAlarmName = title;
        mAlarmUnit = alarmUnit;
        mContext = context;
        mAlarmDuration = alarmDuration;
        mTimeUnitFactor = (int) countDownInterval;
        Log.d(TAG, "TimeFactor in timer: " + mTimeUnitFactor);
        if (mAlert == null) {
            mAlert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            if (mAlert == null) {
                mAlert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            }
        }
        Intent resultIntent = new Intent(mContext, MainActivity.class);
        resultIntent.setAction(Intent.ACTION_MAIN);
        resultIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(mContext, 0, resultIntent, 0);
        notifyId = mAlarmId;
        mNM = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotify = new NotificationCompat.Builder(mContext)
                .setContentTitle(mAlarmDuration + mContext.getResources().getString(R.string.notification_title))
                .setContentText(mAlarmName + mContext.getResources().getString(R.string.notification_text02) + mAlarmDuration + mAlarmUnit + mContext.getResources().getString(R.string.notification_text03))
                .setSmallIcon(R.drawable.ic_alarm_add_grey600_24dp)
                .setContentIntent(resultPendingIntent); // TODO: use resources in smarter way :) !
        mNM.notify(notifyId, mNotify.build());
        mRingtone = RingtoneManager.getRingtone(mContext.getApplicationContext(), mAlert);

        Log.d(TAG, "Starting timer for [" + mAlarmId + "] = " + mAlarmName  + " with duration " + mAlarmDuration + " " + mAlarmUnit);
    }

    @Override
    public void onTick(long millisUntilFinished) {
        Log.d(TAG, "Countdown time remaining: " + millisUntilFinished / mTimeUnitFactor);
        mTimeUntilFinished = millisUntilFinished;
        mBI.putExtra("AlarmID", mAlarmId);
        mBI.putExtra("countdown", (millisUntilFinished ) / mTimeUnitFactor);
        mContext.sendBroadcast(mBI);
        mNotify.setContentTitle(millisUntilFinished / mTimeUnitFactor + mAlarmUnit + mContext.getResources().getString(R.string.notification_title));
        mNM.notify(notifyId, mNotify.build());
    }

    @Override
    public void onFinish() {
        mRingtone.play();
        mNotify.setContentTitle(mAlarmName + mContext.getResources().getString(R.string.notification_text02) + mAlarmDuration + mAlarmUnit + mContext.getResources().getString(R.string.notification_text03_finished))
                .setContentText(mContext.getResources().getString(R.string.notification_text_finished));
        mNM.notify(notifyId, mNotify.build());
        Log.d(TAG, "Timer [" + mAlarmId + "] finished.");
        mBI.putExtra("AlarmID", mAlarmId);
        mBI.putExtra("countdown", Long.valueOf(0));
        mContext.sendBroadcast(mBI);
        Log.d(TAG, "Countdown finished.");
        isFinished = true;
    }

    public void broadcastTimeUntilFinished() {
        mBI.putExtra("AlarmID", mAlarmId);
        if (isFinished == true) { mBI.putExtra("countdown", Long.valueOf(0));}
            else mBI.putExtra("countdown", (mTimeUntilFinished ) / mTimeUnitFactor);
        mContext.sendBroadcast(mBI);
        Log.d(TAG, "Countdown time broadcasted on command.");
    }

    public void stopRingtone () {
        mRingtone.stop();
    }
}