package pl.appnode.napwatch;

import static pl.appnode.napwatch.StateConstants.OFF;
import static pl.appnode.napwatch.StateConstants.SWITCHING;
import static pl.appnode.napwatch.StateConstants.ON;

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
    int notifyID = 0;
    int mAlarmId;
    String mAlarmName;
    int mAlarmDuration;
    String mAlarmUnit;

    Uri mAlert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
    Ringtone mRingtone;
    Context mContext;

    public AlarmCountDownTimer (long millisInFuture, long countDownInterval, int alarmId, String title, String alarmUnit, int alarmDuration, Context context) {
        super(millisInFuture, countDownInterval);
        mAlarmId = alarmId;
        mAlarmName = title;
        mAlarmUnit = alarmUnit;
        mContext = context;
        mAlarmDuration = alarmDuration;

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
        notifyID = mAlarmId;
        final NotificationManager mNM = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        final NotificationCompat.Builder mNotify = new NotificationCompat.Builder(mContext)
                .setContentTitle(mAlarmDuration + mContext.getResources().getString(R.string.notification_title))
                .setContentText(mAlarmName + mContext.getResources().getString(R.string.notification_text02) + mAlarmDuration + mAlarmUnit + mContext.getResources().getString(R.string.notification_text03))
                .setSmallIcon(R.drawable.ic_alarm_add_grey600_24dp)
                .setContentIntent(resultPendingIntent); // TODO: use resources in smarter way :) !
        mNM.notify(notifyID, mNotify.build());

        mRingtone = RingtoneManager.getRingtone(mContext.getApplicationContext(), mAlert);

        Log.d(TAG, "Starting timer for [" + mAlarmId + "] = " + mAlarmName  + " with duration " + mAlarmDuration + " " + mAlarmUnit);

    }

    @Override
    public void onTick(long millisUntilFinished) {

        Log.d(TAG, "Countdown seconds remaining: " + millisUntilFinished / 1000);
        mBI.putExtra("AlarmID", mAlarmId);
        mBI.putExtra("countdown", millisUntilFinished / 1000);
        mContext.sendBroadcast(mBI);
    }

    @Override
    public void onFinish() {
        mRingtone.play();
        Log.d(TAG, "Timer [" + mAlarmId + "] finished.");
    }
}
