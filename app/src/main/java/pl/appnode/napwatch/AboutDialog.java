package pl.appnode.napwatch;

import android.content.Context;
import android.content.pm.PackageManager;

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
}
