package com.pycredit.h5sdk.perm.support;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.pycredit.h5sdk.perm.support.manufacturer.HUAWEI;
import com.pycredit.h5sdk.perm.support.manufacturer.MEIZU;
import com.pycredit.h5sdk.perm.support.manufacturer.OPPO;
import com.pycredit.h5sdk.perm.support.manufacturer.PermissionsPage;
import com.pycredit.h5sdk.perm.support.manufacturer.Protogenesis;
import com.pycredit.h5sdk.perm.support.manufacturer.Samsung;
import com.pycredit.h5sdk.perm.support.manufacturer.VIVO;
import com.pycredit.h5sdk.perm.support.manufacturer.XIAOMI;
import com.pycredit.h5sdk.perm.support.manufacturer.ZTE;


/**
 * Created by joker on 2017/8/4.
 */

public class PermissionsPageManager {

    public static final String LAUNCH_MODE = "launchMode";
    /**
     * Build.MANUFACTURER
     */
    static final String MANUFACTURER_HUAWEI = "HUAWEI";
    static final String MANUFACTURER_XIAOMI = "XIAOMI";
    static final String MANUFACTURER_OPPO = "OPPO";
    static final String MANUFACTURER_VIVO = "vivo";
    static final String MANUFACTURER_MEIZU = "meizu";
    static final String MANUFACTURER_SMARTISAN = "smartisan";
    static final String MANUFACTURER_SAMSUNG = "samsung";
    static final String MANUFACTURER_ZTE = "ZTE";
    static final String manufacturer = Build.MANUFACTURER;

    public static String getManufacturer() {
        return manufacturer;
    }

    public static Intent getIntent(Activity activity) {
        PermissionsPage permissionsPage = new Protogenesis(activity);
        try {
            if (MANUFACTURER_HUAWEI.equalsIgnoreCase(manufacturer)) {
                permissionsPage = new HUAWEI(activity);
            } else if (MANUFACTURER_OPPO.equalsIgnoreCase(manufacturer)) {
                permissionsPage = new OPPO(activity);
            } else if (MANUFACTURER_VIVO.equalsIgnoreCase(manufacturer)) {
                permissionsPage = new VIVO(activity);
            } else if (MANUFACTURER_XIAOMI.equalsIgnoreCase(manufacturer)) {
                permissionsPage = new XIAOMI(activity);
            } else if (MANUFACTURER_MEIZU.equalsIgnoreCase(manufacturer)) {
                permissionsPage = new MEIZU(activity);
            } else if (MANUFACTURER_SAMSUNG.equalsIgnoreCase(manufacturer)) {
                permissionsPage = new Samsung(activity);
            } else if (MANUFACTURER_ZTE.equalsIgnoreCase(manufacturer)) {
                permissionsPage = new ZTE(activity);
            }

            return permissionsPage.settingIntent();
        } catch (Exception e) {
            Log.e("Permissions4M", "手机品牌为：" + manufacturer + "异常抛出，：" + e.getMessage());
            permissionsPage = new Protogenesis(activity);
            return ((Protogenesis) permissionsPage).settingIntent();
        }
    }

    public static Intent getSettingIntent(Activity activity) {
        return new Protogenesis(activity).settingIntent();
    }

    public static boolean isXIAOMI() {
        return getManufacturer().equalsIgnoreCase(MANUFACTURER_XIAOMI);
    }

    public static boolean isOPPO() {
        return getManufacturer().equalsIgnoreCase(MANUFACTURER_OPPO);
    }

    public static Version getColorOsVersion() {
        if (isOPPO()) {
            String version = OPPO.getColorOsVersion();
            if (version != null && version.length() > 0) {
                return new Version(version.substring(1));
            }
        }
        return null;
    }

    public static boolean isVIVO() {
        return getManufacturer().equalsIgnoreCase(MANUFACTURER_VIVO);
    }

    public static boolean isMEIZU() {
        return getManufacturer().equalsIgnoreCase(MANUFACTURER_MEIZU);
    }

    public static boolean isSMARTISAN() {
        return getManufacturer().equalsIgnoreCase(MANUFACTURER_SMARTISAN);
    }
}
