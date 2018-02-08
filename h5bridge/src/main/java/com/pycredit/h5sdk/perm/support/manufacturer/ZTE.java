package com.pycredit.h5sdk.perm.support.manufacturer;

import android.app.Activity;
import android.content.Intent;

/**
 * @author huangx
 * @date 2018/2/8
 */

public class ZTE implements PermissionsPage {
    private final Activity activity;

    public ZTE(Activity activity) {
        this.activity = activity;
    }

    @Override
    public Intent settingIntent() {
        Intent intent = new Intent();
        intent.setAction("com.zte.heartyservice.intent.action.startActivity.PERMISSION_SCANNER");
        return intent;
    }
}
