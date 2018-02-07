package com.pycredit.h5sdk.perm.support.manufacturer;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;

/**
 * @author huangx
 * @date 2018/2/6
 */

public class Samsung implements PermissionsPage {
    private final Activity activity;

    public Samsung(Activity activity) {
        this.activity = activity;
    }

    @Override
    public Intent settingIntent() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return new Intent(Settings.ACTION_SETTINGS);//到设置找应用程序许可
        }
        return null;
    }
}
