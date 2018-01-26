package com.pycredit.h5sdk.perm.support.manufacturer;

import android.app.Activity;
import android.content.Intent;

import com.pycredit.h5sdk.perm.support.ManufacturerSupportUtil;
import com.pycredit.h5sdk.perm.support.Version;

/**
 * support:
 * 1.Y55A androi:6.0.1/Funtouch 2.6
 * 2.Xplay5A android: 5.1.1/Funtouch 3
 * <p>
 * manager home page, or {@link Protogenesis#settingIntent()}
 * <p>
 * Created by joker on 2017/8/4.
 */

public class VIVO implements PermissionsPage {
    private final String PKG = "com.iqoo.secure";//FuntouchOs 3.0以下
    private final String MAIN_CLS = "com.iqoo.secure.safeguard.SoftPermissionDetailActivity";
    private final String PKG_3 = "com.vivo.permissionmanager";//FuntouchOs 3.0及以上
    private final String MAIN_CLS_3 = "com.vivo.permissionmanager.activity.SoftPermissionDetailActivity";
    private final Activity context;

    public VIVO(Activity context) {
        this.context = context;
    }

    @Override
    public Intent settingIntent() {
        String funtouchOsVersion = getFuntouchOsVersion();
        Version version = new Version(funtouchOsVersion);
        if (version.isLowerThan("3.0")) {
            Intent intentForActivity = ManufacturerSupportUtil.getIntentForActivity(context, PKG, MAIN_CLS);
            if (intentForActivity != null) {
                intentForActivity.putExtra("packagename", context.getPackageName());
                return intentForActivity;
            }
        } else {
            Intent intentForActivity = ManufacturerSupportUtil.getIntentForActivity(context, PKG_3, MAIN_CLS_3);
            if (intentForActivity != null) {
                intentForActivity.putExtra("packagename", context.getPackageName());
                return intentForActivity;
            }
        }
        return null;
    }


    private static String getFuntouchOsVersion() {
        return ManufacturerSupportUtil.getSystemProperty("ro.vivo.os.version");
    }
}
