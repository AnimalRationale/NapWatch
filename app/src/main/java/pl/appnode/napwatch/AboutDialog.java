package pl.appnode.napwatch;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

public class AboutDialog {

    private static String versionName;
    private static String versionCode;
    private static Drawable aboutIcon;

    private static void versionInfo(Context context) {
        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            versionName = info.versionName;
            versionCode = String.valueOf(info.versionCode);
            aboutIcon = manager.getApplicationIcon("pl.appnode.napwatch");
        }
        catch (PackageManager.NameNotFoundException ex) {
            versionName = context.getResources().getString(R.string.about_dialog_ver_name_err);
            versionCode = context.getResources().getString(R.string.about_dialog_ver_code_err);
            aboutIcon = null;
        }
    }

    public static void showDialog(Activity callingActivity) {
        versionInfo(callingActivity);
        String aboutVersion = versionName + "." + versionCode;
        LayoutInflater layoutInflater = LayoutInflater.from(callingActivity);
        View aboutDialog = layoutInflater.inflate(R.layout.about_dialog, null);
        TextView textAbout = (TextView) aboutDialog.findViewById(R.id.aboutDialogInfo);
        textAbout.setText(aboutVersion);
        new AlertDialog.Builder(callingActivity)
                .setTitle(callingActivity.getResources().getString(R.string.about_dialog_title)
                        + callingActivity.getString(R.string.app_name))
                .setIcon(R.drawable.ic_alarm_add_grey600_48dp)
                .setCancelable(true)
                .setPositiveButton(callingActivity.getResources().getString(R.string.about_dialog_ok), null)
                .setView(aboutDialog)
                .show();
    }
}