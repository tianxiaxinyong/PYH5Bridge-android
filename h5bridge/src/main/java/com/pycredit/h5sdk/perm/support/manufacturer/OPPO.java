package com.pycredit.h5sdk.perm.support.manufacturer;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;

import com.pycredit.h5sdk.perm.support.ManufacturerSupportUtil;
import com.pycredit.h5sdk.perm.support.Version;

/**
 * support:
 * 1. oppo a57 android 6.0.1/coloros3.0
 * <p>
 * manager home page, permissions manage page does not work!!!, or
 * {@link Protogenesis#settingIntent()}
 * <p>
 * Created by joker on 2017/8/4.
 */

public class OPPO implements PermissionsPage {
    private final Activity context;

    private final String PKG = "com.oppo.safe";//ColorOS 2.0
    private final String MANAGER_OUT_CLS = "com.oppo.safe.permission.PermissionSettingsActivity";
    private final String PKG_2 = "com.color.safecenter"; //ColorOS 2.1
    private final String MANAGER_OUT_CLS_2 = "com.color.safecenter.permission.PermissionManagerActivity";
    private final String PKG_3 = "com.coloros.safecenter"; //ColorOS 3.0
    private final String MANAGER_OUT_CLS_3_1 = "com.coloros.safecenter.permission.singlepage.PermissionSinglePageActivity";
    private final String MANAGER_OUT_CLS_3_2 = "com.coloros.safecenter.permission.PermissionManagerActivity";

    public OPPO(Activity context) {
        this.context = context;
    }

    @Override
    public Intent settingIntent() {
        String colorOsVersion = getColorOsVersion();
        Version version = new Version(colorOsVersion.substring(1));
        if (version.isLowerThan("3.0")) {//ColorOS3.0以下
            Intent intentForActivity = ManufacturerSupportUtil.getIntentForActivity(context, PKG_2, MANAGER_OUT_CLS_2);
            if (intentForActivity != null) {
                intentForActivity.putExtra(PACK_TAG, context.getPackageName());
                return intentForActivity;
            }
            intentForActivity = ManufacturerSupportUtil.getIntentForActivity(context, PKG, MANAGER_OUT_CLS);
            if (intentForActivity != null) {
                intentForActivity.putExtra(PACK_TAG, context.getPackageName());
                return intentForActivity;
            }
        } else {//ColorOS3.0以上
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                // Android6.0 以上进入应用详情页面的权限管理更合适
                Intent intentForActivity = ManufacturerSupportUtil.getIntentForActivity(context, PKG_3, MANAGER_OUT_CLS_3_1);
                if (intentForActivity != null) {
                    intentForActivity.putExtra(PACK_TAG, context.getPackageName());
                    return intentForActivity;
                }
                intentForActivity = ManufacturerSupportUtil.getIntentForActivity(context, PKG_3, MANAGER_OUT_CLS_3_2);
                if (intentForActivity != null) {
                    intentForActivity.putExtra(PACK_TAG, context.getPackageName());
                    return intentForActivity;
                }
            }
        }
        return null;
    }

    public static String getColorOsVersion() {
        return ManufacturerSupportUtil.getSystemProperty("ro.build.version.opporom");
    }
}
