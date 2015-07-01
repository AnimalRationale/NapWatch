package pl.appnode.napwatch;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

import static pl.appnode.napwatch.StateConstants.WIDGET_BUTTONS;
import static pl.appnode.napwatch.StateConstants.WIDGET_BUTTON_ACTION;

public class WidgetSetUpService extends Service {

    private static final String TAG = "WidgetSetUpService";
    private int mStartMode = START_STICKY;
    private int mOrientation;
    private static RemoteViews sWidgetViews = null;
    private static ComponentName sWidget = null;
    private static AppWidgetManager sWidgetManager = null;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mOrientation = this.getResources().getConfiguration().orientation;
        Log.d(TAG, "WidgetSetUpService Start.");
        return mStartMode;
    }

    private static void getWidget(Context context) {
        sWidgetViews = new RemoteViews(context.getPackageName(), R.layout.napwatch_widget);
        sWidget = new ComponentName(context, NapWatchWidgetProvider.class);
        sWidgetManager = AppWidgetManager.getInstance(context);
    }

    public void reassignWidgetButtons(Context context) {
        getWidget(context);
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        sWidgetViews.setOnClickPendingIntent(WIDGET_BUTTONS[0], pendingIntent);
        Log.d(TAG, "WidgetSetUpService Service reassigning app button.");
        for (int i = 1; i <= 4; i++) {
            sWidgetViews.setOnClickPendingIntent(WIDGET_BUTTONS[i], getPendingSelfIntent(context, WIDGET_BUTTON_ACTION[i]));
            Log.d(TAG, "WidgeSetUp Service reassigning timer #" + i + " button.");
        }
    }

    private PendingIntent getPendingSelfIntent(Context context, String action) {
        Intent intent = new Intent(context, getClass());
        intent.setAction(action);
        Log.d(TAG, "WidgetSetUpService Service pendingSelfIntent.");
        return PendingIntent.getBroadcast(context, 0, intent, 0);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        Log.d(TAG, "WidgetSetUpService Service configuration change.");
        if(newConfig.orientation != mOrientation)
        {
            mOrientation = newConfig.orientation;
            reassignWidgetButtons(this);
        }
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }
}