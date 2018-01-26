package com.pycredit.h5sdk.perm.support.manufacturer;

import android.app.Activity;
import android.content.Intent;

import com.pycredit.h5sdk.perm.support.ManufacturerSupportUtil;


/**
 * Created by joker on 2017/8/24.
 */

public class MEIZU implements PermissionsPage {
    private final Activity activity;
    private final String MANAGER_OUT_CLS = "com.meizu.safe.security.AppSecActivity";
    private final String PKG = "com.meizu.safe";

    public MEIZU(Activity activity) {
        this.activity = activity;
    }

    @Override
    public Intent settingIntent() {
        Intent intentForActivity = ManufacturerSupportUtil.getIntentForActivity(activity, PKG, MANAGER_OUT_CLS);
        if (intentForActivity != null) {
            intentForActivity.putExtra("packageName", activity.getPackageName());
            return intentForActivity;
        }
        return null;
    }
}
