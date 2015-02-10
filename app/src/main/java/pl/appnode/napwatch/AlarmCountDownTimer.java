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

    private final static String TAG = "::Service.AlarmCountdownTimer";
    public static final String COUNTDOWN_BROADCAST = "pl.appnode.napwatch";
    Intent mBI = new Intent(COUNTDOWN_BROADCAST);
    private int mAlarmId;
    String mAlarmName;
    int mAlarmDuration;
    Context mContext;

    Uri mAlert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
    Ringtone mRingtone;

    public AlarmCountDownTimer (long millisInFuture, long countDownInterval, int alarmId, Context context) {
        super(millisInFuture, countDownInterval);
        mAlarmId = alarmId;
        mContext = context;
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
