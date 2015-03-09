package pl.appnode.napwatch;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;

public class AlarmReceiver extends BroadcastReceiver {

    private static final String LOCK_TAG = "pl.appnode.napwatch";
    private static PowerManager.WakeLock sWakeLock = null;

    private static synchronized void acquireWakeLock (Context context) {
        if (sWakeLock == null) {
            PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            sWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, LOCK_TAG);
            sWakeLock.setReferenceCounted(true);
        }
        sWakeLock.acquire();
    }
    @Override
    public void onReceive(Context context, Intent alarmIntent) {

    }

}
