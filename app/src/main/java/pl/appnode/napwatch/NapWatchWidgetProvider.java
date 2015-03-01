package pl.appnode.napwatch;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

public class NapWatchWidgetProvider extends AppWidgetProvider {

    public static String WIDGET_BUTTON = "pl.appnode.napwatch.WIDGET_BUTTON";

    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        final int N = appWidgetIds.length;
        for (int i=0; i<N; i++) {
            int appWidgetId = appWidgetIds[i];
            Intent intent = new Intent(context, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.napwatch_widget);
            views.setOnClickPendingIntent(R.id.widget_round_btn1, pendingIntent);
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

}
