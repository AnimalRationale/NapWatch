package pl.appnode.napwatch;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.RemoteViews;

public class NapWatchWidgetProvider extends AppWidgetProvider {

    public static final String ALARMS_PREFS_FILE = "AlarmsPrefsFile";

    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        SharedPreferences alarmsPrefs = context.getSharedPreferences(ALARMS_PREFS_FILE, 0);
        String alarmPrefix;
        final int N = appWidgetIds.length;
        for (int i = 0; i < N; i++) {
            int appWidgetId = appWidgetIds[i];
            Intent intent = new Intent(context, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.napwatch_widget);
            views.setOnClickPendingIntent(R.id.widget_round_btn1, pendingIntent);
            for (int j = 1; j <= 4; j++) {
                alarmPrefix = "Alarm_" + j;

            }
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

}
