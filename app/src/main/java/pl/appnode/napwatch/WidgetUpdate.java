package pl.appnode.napwatch;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

import static pl.appnode.napwatch.StateConstants.WIDGET_BUTTONS;
import static pl.appnode.napwatch.StateConstants.WIDGET_BUTTON_ACTION;

public class WidgetUpdate {

    private static final String TAG = "WidgetUpdate";
    private static RemoteViews sWidgetViews = null;
    private static ComponentName sWidget = null;
    private static AppWidgetManager sWidgetManager = null;

    private static void getWidget(Context context) {
        sWidgetViews = new RemoteViews(context.getPackageName(), R.layout.napwatch_widget);
        sWidget = new ComponentName(context, NapWatchWidgetProvider.class);
        sWidgetManager = AppWidgetManager.getInstance(context);
    }

    private static void setButtonColor(int widgetButtonId, Context context, int background) {
        getWidget(context);
        sWidgetViews.setInt(WIDGET_BUTTONS[widgetButtonId], "setBackgroundResource", background);
        setUpWidget(context);
    }

    public static void setButtonOn(int widgetButtonId, Context context) {
        setButtonColor(widgetButtonId, context, R.drawable.round_button_orange);
    }

    public static void setButtonFinish(int widgetButtonId, Context context) {
        setButtonColor(widgetButtonId, context, R.drawable.round_button_red);
    }

    public static void setButtonOff(int widgetButtonId, Context context) {
        setButtonColor(widgetButtonId, context, R.drawable.round_button_green);
    }

    public static void buttonTime(int widgetButtonId, String timeToFinish, Context context) {
        getWidget(context);
        sWidgetViews.setTextViewText(WIDGET_BUTTONS[widgetButtonId], timeToFinish);
        setUpWidget(context);
    }

    private static void setUpWidget(Context context) {
        reassignWidgetButtons(context);
        sWidgetManager.updateAppWidget(sWidget, sWidgetViews);
    }

    private static void reassignWidgetButtons(Context context) {
        getWidget(context);
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        sWidgetViews.setOnClickPendingIntent(WIDGET_BUTTONS[0], pendingIntent);
        Log.d(TAG, "WidgetSetUpService Service reassigning app button.");
        for (int i = 1; i <= 4; i++) {
            sWidgetViews.setOnClickPendingIntent(WIDGET_BUTTONS[i], getPendingSelfIntent(context, WIDGET_BUTTON_ACTION[i]));
            Log.d(TAG, "WidgetSetUp Service reassigning timer #" + i + " button for action: " + WIDGET_BUTTON_ACTION[i]);
        }
        sWidgetManager.updateAppWidget(sWidget, sWidgetViews);
    }

    private static PendingIntent getPendingSelfIntent(Context context, String action) {
        Intent intent = new Intent(context, NapWatchWidgetProvider.class);
        intent.setAction(action);
        Log.d(TAG, "WidgetSetUpService Service pendingSelfIntent for action: " + action);
        return PendingIntent.getBroadcast(context, 0, intent, 0);
    }
}