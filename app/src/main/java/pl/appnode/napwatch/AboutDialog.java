package pl.appnode.napwatch;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class AboutDialog {

    private String versionName(Context context) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(),0).versionName;
        }
        catch (PackageManager.NameNotFoundException ex) {
            return context.getResources().getString(R.string.about_dialog_ver_name_err);
        }
    }

    private String versionCode(Context context) {
        try {
            return String.valueOf(context.getPackageManager().getPackageInfo(context.getPackageName(),0).versionCode);
        }
        catch (PackageManager.NameNotFoundException ex) {
            return context.getResources().getString(R.string.about_dialog_ver_name_err);
        }
    }

    public void showDialog(Activity callingActivity) {
        String aboutVersion = callingActivity.getResources().getString(R.string.about_dialog_version)
                + versionName(callingActivity) + "."
                + versionCode(callingActivity);
        LayoutInflater inflater = callingActivity.getLayoutInflater();
        View aboutDialog = inflater.inflate(R.layout.about_dialog, (ViewGroup) callingActivity.findViewById(R.id.aboutDialog));
        TextView textAbout = (TextView) aboutDialog.findViewById(R.id.aboutDialogInfo);
        textAbout.setText(aboutVersion);
        new AlertDialog.Builder(callingActivity)
                .setTitle(callingActivity.getResources().getString(R.string.about_dialog_title)
                        + callingActivity.getString(R.string.app_name))
                .setCancelable(true)
                .setPositiveButton(callingActivity.getResources().getString(R.string.about_dialog_ok), null)
                .setView(aboutDialog)
                .show();
    }
}