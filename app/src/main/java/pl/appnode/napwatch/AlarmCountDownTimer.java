package pl.appnode.napwatch;

import static pl.appnode.napwatch.StateConstants.OFF;
import static pl.appnode.napwatch.StateConstants.SWITCHING;
import static pl.appnode.napwatch.StateConstants.ON;

import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.CountDownTimer;
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
