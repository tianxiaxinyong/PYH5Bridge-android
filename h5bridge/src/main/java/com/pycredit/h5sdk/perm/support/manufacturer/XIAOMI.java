package com.pycredit.h5sdk.perm.support.manufacturer;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;

import com.pycredit.h5sdk.perm.support.ManufacturerSupportUtil;

/**
 * support:
 * 1.hongmi 5X android:6.0.1/miui 8.2
 * <p>
 * manager home page, or {@link Protogenesis#settingIntent()}
 * <p>
 * Created by joker on 2017/8/4.
 */

public class XIAOMI implements PermissionsPage {
    private final String PKG = "com.miui.securitycenter";
    // manager
    private final String MIUI7_MANAGER_OUT_CLS = "com.miui.permcenter.permissions" +
            ".AppPermissionsEditorActivity";
    // xiaomi permissions setting page
    private final String MIUI8_OUT_CLS_1 = "com.miui.permcenter.permissions.PermissionsEditorActivity";
    private final String MIUI8_OUT_CLS_2 = "com.miui.permcenter.permissions.AppPermissionsEditorActivity";//MIUI 8.0.1.0
    private final String MIUI8_OUT_CLS_3 = "com.miui.permcenter.permissions.RealAppPermissionsEditorActivity";//MIUI 8.1.3.0 (无效向前找MIUI8_OUT_CLS_4)
    private final String MIUI8_OUT_CLS_4 = "com.miui.permcenter.MainAcitivty";
    private final Activity context;

    public XIAOMI(Activity context) {
        this.context = context;
    }

    @Override
    public Intent settingIntent() throws ActivityNotFoundException {
        Intent intent = new Intent();
        String miuiInfo = getMIUIVersion();
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (miuiInfo.contains("7") || miuiInfo.contains("6")) {
            intent = new Intent("miui.intent.action.APP_PERM_EDITOR");
            intent.setClassName(PKG, MIUI7_MANAGER_OUT_CLS);
            intent.putExtra("extra_pkgname", context.getPackageName());
            return intent;
        } else {
            // miui 8
            Intent intentForActivity = ManufacturerSupportUtil.getIntentForActivity(context, PKG, MIUI8_OUT_CLS_1);
            if (intentForActivity != null) {
                intentForActivity.putExtra("extra_pkgname", context.getPackageName());
                return intentForActivity;
            }
            intentForActivity = ManufacturerSupportUtil.getIntentForActivity(context, PKG, MIUI8_OUT_CLS_3);
            if (intentForActivity != null) {
                intentForActivity = ManufacturerSupportUtil.getIntentForActivity(context, PKG, MIUI8_OUT_CLS_4);
                if (intentForActivity != null) {
                    intentForActivity.putExtra("extra_pkgname", context.getPackageName());
                    return intentForActivity;
                }
            }
            intentForActivity = ManufacturerSupportUtil.getIntentForActivity(context, PKG, MIUI8_OUT_CLS_2);
            if (intentForActivity != null) {
                intentForActivity.putExtra("extra_pkgname", context.getPackageName());
                return intentForActivity;
            }
        }
        return null;
    }

    private static String getMIUIVersion() {
        return ManufacturerSupportUtil.getSystemProperty("ro.miui.ui.version.name");
    }
}
