package pl.appnode.napwatch;

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
    int mAlarmId;
    String mAlarmName;
    int mAlarmDuration;

    Uri mAlert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
    Ringtone mRingtone;

    public CountDownTimer (long millisInFuture, long countDownInterval) {
        super();
    }

    @Override
    public void onTick(long millisUntilFinished) {

        Log.d(TAG, "Countdown seconds remaining: " + millisUntilFinished / 1000);
        mBI.putExtra("AlarmID", mAlarmId);
        mBI.putExtra("countdown", millisUntilFinished / 1000);
        sendBroadcast(mBI);
    }

    @Override
    public void onFinish() {
        mRingtone.play();
        Log.d(TAG, "Timer [" + mAlarmId + "] finished.");
    }
}
