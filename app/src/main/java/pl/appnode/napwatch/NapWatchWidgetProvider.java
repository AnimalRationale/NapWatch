package pl.appnode.napwatch;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import static pl.appnode.napwatch.StateConstants.WIDGET_BUTTON_ACTION;

public class NapWatchWidgetProvider extends AppWidgetProvider {

    private static final String TAG = "AppWidgetProvider";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
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
}