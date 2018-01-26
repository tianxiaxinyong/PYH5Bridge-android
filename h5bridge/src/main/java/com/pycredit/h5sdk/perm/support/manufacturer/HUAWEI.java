package com.pycredit.h5sdk.perm.support.manufacturer;

import android.app.Activity;
import android.content.Intent;

import com.pycredit.h5sdk.perm.support.ManufacturerSupportUtil;
import com.pycredit.h5sdk.perm.support.Version;

/**
 * support:
 * 1.mate7 android:6.0/emui 4.0.1
 * 2.畅享7 android:7.0/emui 5.1
 * <p>
 * manager permissions page, permissions manage page, or {@link Protogenesis#settingIntent()}
 * <p>
 * Created by joker on 2017/8/4.
 */

public class HUAWEI implements PermissionsPage {
    private final Activity context;
    private final String PKG = "com.huawei.systemmanager";
    private final String MANAGER_OUT_CLS = "com.huawei.permissionmanager.ui.MainActivity";

    private final String PKG_5 = "com.android.packageinstaller";
    private final String MANAGER_OUT_CLS_5 = "com.android.packageinstaller.permission.ui.ManagePermissionsActivity";

    public HUAWEI(Activity context) {
        this.context = context;
    }

    @Override
    public Intent settingIntent() {
        String emuiVersion = getEMUIVersion();
        Version version = new Version(emuiVersion.substring(10));
        if (version.isLowerThan("4.0")) {//4.0以下到权限管理页面
            Intent intentForActivity = ManufacturerSupportUtil.getIntentForActivity(context, PKG, MANAGER_OUT_CLS);
            if (intentForActivity != null) {
                intentForActivity.putExtra(PACK_TAG, context.getPackageName());
                return intentForActivity;
            }
        } else if (version.isLowerThan("5.0")) {//4.x就到默认应用详情页面，到权限页面没有存储权限设置 坑

        } else {//5.0以上就到默认应用详情页面

        }
        return null;
    }

    /**
     * 获取EMUI版本 format:EmotionUI_x.x.x
     *
     * @return
     */
    private static String getEMUIVersion() {
        return ManufacturerSupportUtil.getSystemProperty("ro.build.version.emui");
    }
}
