package pl.appnode.napwatch;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.util.Log;

public class AlarmReceiver extends BroadcastReceiver {

    private static final String TAG = "AlarmReceiver";
    private static final String LOCK_TAG = "pl.appnode.napwatch";
    private static PowerManager.WakeLock sWakeLock = null;

    private static synchronized void acquireWakeLock (Context context) {
        if (sWakeLock == null) {
            Log.d(TAG, "Wake lock == null, setting up.");
            PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            sWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, LOCK_TAG);
            sWakeLock.setReferenceCounted(true);
        }
        sWakeLock.acquire();
        Log.d(TAG, "Wake lock acquired.");
    }

     public static synchronized void releaseLock() {
         if (sWakeLock != null) {
             sWakeLock.release();
             Log.d(TAG, "Wake lock released.");
         }
     }

    @Override
    public void onReceive(Context context, Intent alarmIntent) {
        acquireWakeLock(context);
        Intent serviceIntent = new Intent(context, AlarmBroadcastService.class);
        context.startService(serviceIntent);
        Log.d(TAG, "Service started.");
    }
}