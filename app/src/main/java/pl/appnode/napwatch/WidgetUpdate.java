package pl.appnode.napwatch;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.widget.RemoteViews;

import static pl.appnode.napwatch.StateConstants.WIDGET_BUTTONS;

public class WidgetUpdate {

    private static RemoteViews sWidgetViews = null;
    private static ComponentName sWidget = null;
    private static AppWidgetManager sWidgetManager = null;

    private static void getWidget(Context context) {
        sWidgetViews = new RemoteViews(context.getPackageName(), R.layout.napwatch_widget);
        sWidget = new ComponentName(context, NapWatchWidgetProvider.class);
        sWidgetManager = AppWidgetManager.getInstance(context);
    }

    public static void setButtonOn(int widgetButtonId, Context context) {
        getWidget(context);
        sWidgetViews.setInt(WIDGET_BUTTONS[widgetButtonId], "setBackgroundResource", R.drawable.round_button_selected);
        sWidgetManager.updateAppWidget(sWidget, sWidgetViews);
    }

    public static void setButtonOff(int widgetButtonId, Context context) {
        getWidget(context);
        sWidgetViews.setInt(WIDGET_BUTTONS[widgetButtonId], "setBackgroundResource", R.drawable.round_button);
        sWidgetManager.updateAppWidget(sWidget, sWidgetViews);
    }

    public static void buttonTime(int widgetButtonId, String timeToFinish, Context context) {
        getWidget(context);
        sWidgetViews.setTextViewText(WIDGET_BUTTONS[widgetButtonId], timeToFinish);
        sWidgetManager.updateAppWidget(sWidget, sWidgetViews);
    }
}