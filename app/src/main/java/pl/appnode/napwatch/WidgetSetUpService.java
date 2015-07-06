package pl.appnode.napwatch;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

import static pl.appnode.napwatch.StateConstants.ALARMS_PREFS_FILE;
import static pl.appnode.napwatch.StateConstants.DEFAULT_TIMER_DURATION;
import static pl.appnode.napwatch.StateConstants.DEFAULT_TIMER_DURATION_MODIFIER;
import static pl.appnode.napwatch.StateConstants.MINUTE;
import static pl.appnode.napwatch.StateConstants.SECOND;
import static pl.appnode.napwatch.StateConstants.TIMERS_COUNT;
import static pl.appnode.napwatch.StateConstants.WIDGET_BUTTONS;
import static pl.appnode.napwatch.StateConstants.WIDGET_BUTTON_ACTION;

public class WidgetSetUpService extends Service {

    private static final String TAG = "WidgetSetUpService";

    private int mOrientation;
    private static RemoteViews sWidgetViews = null;
    private static ComponentName sWidget = null;
    private static AppWidgetManager sWidgetManager = null;
    private static boolean sWidgetUpdateServiceRunning = false;

    @Override
    public void onCreate() {
        super.onCreate();
        mOrientation = this.getResources().getConfiguration().orientation;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int startMode = START_STICKY;
        if (!sWidgetUpdateServiceRunning) {
            MainActivity.setIsWidgetUpdateService(true);
            sWidgetUpdateServiceRunning = true;
        }
        Log.d(TAG, "WidgetSetUpService Start.");
        setUpWidget(AppContext.getContext());
        return startMode;
    }

    private static void getWidget(Context context) {
        sWidgetViews = new RemoteViews(context.getPackageName(), R.layout.napwatch_widget);
        sWidget = new ComponentName(context, NapWatchWidgetProvider.class);
        sWidgetManager = AppWidgetManager.getInstance(context);
    }

    private static void setUpWidget(Context context) {
        getWidget(context);
        assignWidgetButtons(context);
        if (MainActivity.mAA != null) {setUpFromAlarmList();}
            else {setUpFromSharedPrefs(context);}
        sWidgetManager.updateAppWidget(sWidget, sWidgetViews);
        Log.d(TAG, "Widget updated.");
    }

    private static void setUpFromAlarmList () {
        for (int i = 0; i < TIMERS_COUNT; i++) {
            AlarmInfo alarm = MainActivity.mAA.mAlarmList.get(i);
            if (!alarm.mIsOn) {
                if (alarm.mDurationCounter == 0) {
                    sWidgetViews.setInt(WIDGET_BUTTONS[i + 1], "setBackgroundResource", R.drawable.round_button_red);
                    sWidgetViews.setTextViewText(WIDGET_BUTTONS[i + 1], alarm.mDurationCounter + alarm.mTimeUnitSymbol);
                    Log.d(TAG, "Widget button #" + WIDGET_BUTTONS[i + 1] + " : alarm finished = " + alarm.mName +
                            " // durationCounter = " + alarm.mDurationCounter + alarm.mTimeUnitSymbol);
                } else {
                    sWidgetViews.setInt(WIDGET_BUTTONS[i + 1], "setBackgroundResource", R.drawable.round_button_green);
                    sWidgetViews.setTextViewText(WIDGET_BUTTONS[i + 1], alarm.mDuration + alarm.mTimeUnitSymbol);
                    Log.d(TAG, "Widget button #" + WIDGET_BUTTONS[i + 1] + " : alarm ready = " + alarm.mName +
                            " // duration = " + alarm.mDuration + alarm.mTimeUnitSymbol);
                }
            } else if (alarm.mIsOn) {
                sWidgetViews.setInt(WIDGET_BUTTONS[i + 1], "setBackgroundResource", R.drawable.round_button_orange);
                sWidgetViews.setTextViewText(WIDGET_BUTTONS[i + 1], alarm.mDurationCounter + alarm.mTimeUnitSymbol);
                Log.d(TAG, "Widget button #" + WIDGET_BUTTONS[i + 1] + " : alarm running = " + alarm.mName +
                        " // durationCounter = " + alarm.mDurationCounter + alarm.mTimeUnitSymbol);
            }
        }
    }

    private static void setUpFromSharedPrefs (Context context) {
        int timeUnit;
        String timeUnitSymbol = context.getString(R.string.time_unit_seconds);
        SharedPreferences alarmsPrefs = context.getSharedPreferences(ALARMS_PREFS_FILE, 0);
        String alarmPrefix;
        for (int i = 1; i <= TIMERS_COUNT; i++) {
            alarmPrefix = "Alarm_" + i;
            timeUnit = alarmsPrefs.getInt(alarmPrefix + "_TimeUnit", SECOND);
            switch (timeUnit) {
                case SECOND:  timeUnitSymbol = context.getString(R.string.time_unit_seconds);
                    break;
                case MINUTE:  timeUnitSymbol = context.getString(R.string.time_unit_minutes);
                    break;
            }
            sWidgetViews.setTextViewText(WIDGET_BUTTONS[i], alarmsPrefs.getInt(alarmPrefix + "_Duration",
                    DEFAULT_TIMER_DURATION + (i * DEFAULT_TIMER_DURATION_MODIFIER))
                    + timeUnitSymbol);
            if (alarmsPrefs.getBoolean(alarmPrefix + "_State", false) & MainActivity.isAlarmBroadcastService()) {
                sWidgetViews.setInt(WIDGET_BUTTONS[i], "setBackgroundResource", R.drawable.round_button_red);
            } else sWidgetViews.setInt(WIDGET_BUTTONS[i], "setBackgroundResource", R.drawable.round_button_green);
            Log.d(TAG, "Shared Prefs widget button #" + WIDGET_BUTTONS[i]);
        }
    }

    private static void assignWidgetButtons(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        sWidgetViews.setOnClickPendingIntent(WIDGET_BUTTONS[0], pendingIntent);
        Log.d(TAG, "WidgetSetUp Service reassigning app button.");
        for (int i = 1; i <= TIMERS_COUNT; i++) {
            sWidgetViews.setOnClickPendingIntent(WIDGET_BUTTONS[i], getPendingSelfIntent(context, WIDGET_BUTTON_ACTION[i]));
            Log.d(TAG, "WidgetSetUp Service reassigning timer #" + i + " button for action: " + WIDGET_BUTTON_ACTION[i]);
        }
    }

    private static PendingIntent getPendingSelfIntent(Context context, String action) {
        Intent intent = new Intent(context, NapWatchWidgetProvider.class);
        intent.setAction(action);
        Log.d(TAG, "WidgetSetUp Service pendingSelfIntent for action: " + action);
        return PendingIntent.getBroadcast(context, 0, intent, 0);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        Log.d(TAG, "WidgetSetUp Service configuration change.");
        if(newConfig.orientation != mOrientation)
        {
            mOrientation = newConfig.orientation;
            Log.d(TAG, "WidgetSetUp Service orientation change.");
            setUpWidget(AppContext.getContext());
        }
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }
}