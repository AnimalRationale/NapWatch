package pl.appnode.napwatch;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
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
import static pl.appnode.napwatch.StateConstants.WIDGET_BUTTONS;
import static pl.appnode.napwatch.StateConstants.WIDGET_BUTTON_ACTION;

public class NapWatchWidgetProvider extends AppWidgetProvider {

    private static final String TAG = "AppWidgetProvider";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        SharedPreferences alarmsPrefs = context.getSharedPreferences(ALARMS_PREFS_FILE, 0);
        String alarmPrefix;
        int timeUnit;
        String timeUnitSymbol = context.getString(R.string.time_unit_seconds);
        final int N = appWidgetIds.length;
        for (int i = 0; i < N; i++) {
            int appWidgetId = appWidgetIds[i];
            Intent intent = new Intent(context, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
            RemoteViews widget = new RemoteViews(context.getPackageName(), R.layout.napwatch_widget);
            widget.setOnClickPendingIntent(WIDGET_BUTTONS[0], pendingIntent);
            for (int j = 1; j <= 4; j++) {
                alarmPrefix = "Alarm_" + j;
                timeUnit = alarmsPrefs.getInt(alarmPrefix + "_TimeUnit", SECOND);
                switch (timeUnit) {
                    case SECOND:  timeUnitSymbol = context.getString(R.string.time_unit_seconds);
                        break;
                    case MINUTE:  timeUnitSymbol = context.getString(R.string.time_unit_minutes);
                        break;
                }
                widget.setTextViewText(WIDGET_BUTTONS[j], alarmsPrefs.getInt(alarmPrefix + "_Duration",
                        DEFAULT_TIMER_DURATION + (i * DEFAULT_TIMER_DURATION_MODIFIER))
                        + timeUnitSymbol);
                if (alarmsPrefs.getBoolean(alarmPrefix + "_State", false) & MainActivity.isService()) {
                    widget.setInt(WIDGET_BUTTONS[j], "setBackgroundResource", R.drawable.round_button_red);
                } else widget.setInt(WIDGET_BUTTONS[j], "setBackgroundResource", R.drawable.round_button_green);
                widget.setOnClickPendingIntent(WIDGET_BUTTONS[j], getPendingSelfIntent(context, WIDGET_BUTTON_ACTION[j]));
            }
            appWidgetManager.updateAppWidget(appWidgetId, widget);
            Log.d(TAG, "Widget updated.");
        }
        context.startService(new Intent(context, WidgetSetUpService.class));
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        Log.d(TAG, "Widget onReceive.");
        int i = 1;
        while (i != 5) {
            if (WIDGET_BUTTON_ACTION[i].equals(intent.getAction())) {
                i = 5;
                int j = Integer.parseInt(intent.getAction());
                if (MainActivity.mAA != null) {MainActivity.mAA.alarmAction(Integer.parseInt(intent.getAction()));
                    Log.d(TAG, "mAA not null. Action: " + j);}
                else {
                    Log.d(TAG, "mAA null. Action: " + j);
                    Intent commandIntent = new Intent(context, MainActivity.class);
                    commandIntent.putExtra("buttonId", j);
                    commandIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(commandIntent);
                }
            } else i++;
        }
    }

    private PendingIntent getPendingSelfIntent(Context context, String action) {
        Intent intent = new Intent(context, getClass());
        intent.setAction(action);
        Log.d(TAG, "Widget pendingSelfIntent for action: " + action);
        return PendingIntent.getBroadcast(context, 0, intent, 0);
    }
}