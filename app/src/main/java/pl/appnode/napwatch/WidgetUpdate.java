package pl.appnode.napwatch;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class WidgetUpdate {

    private static final String TAG = "WidgetUpdate";

    public static void widgetUpdate() {
        if (MainActivity.isWidgetUpdateService()) {
            Context context = AppContext.getContext();
            context.startService(new Intent(context, WidgetSetUpService.class));
            Log.d(TAG, "Widget update call");
        }
    }
}