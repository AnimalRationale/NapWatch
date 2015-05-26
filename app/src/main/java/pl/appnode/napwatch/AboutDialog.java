package pl.appnode.napwatch;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

public class AboutDialog {

    private static String sVersionName;
    private static String sVersionCode;

    private static void versionInfo(Context context) {
        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            sVersionName = info.versionName;
            sVersionCode = String.valueOf(info.versionCode);
        }
        catch (PackageManager.NameNotFoundException ex) {
            sVersionName = context.getResources().getString(R.string.about_dialog_ver_name_err);
            sVersionCode = context.getResources().getString(R.string.about_dialog_ver_code_err);
        }
    }

    public static void showDialog(Activity callingActivity) {
        int theme = 3; // Holo Light
        versionInfo(callingActivity);
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(callingActivity);
        if (settings.getBoolean("settings_checkbox_theme", false)) {
            theme = 2; // Holo Dark
        }
        String aboutVersion = sVersionName + "." + sVersionCode;
        LayoutInflater layoutInflater = LayoutInflater.from(callingActivity);
        View aboutDialog = layoutInflater.inflate(R.layout.about_dialog, null);
        TextView textAbout = (TextView) aboutDialog.findViewById(R.id.aboutDialogInfo);
        textAbout.setText(aboutVersion);
        new AlertDialog.Builder(callingActivity, theme)
                .setTitle(callingActivity.getResources().getString(R.string.about_dialog_title)
                        + callingActivity.getString(R.string.app_name))
                .setIcon(R.drawable.ic_launcher)
                .setCancelable(true)
                .setPositiveButton(callingActivity.getResources().getString(R.string.about_dialog_ok), null)
                .setView(aboutDialog)
                .show();
    }
}