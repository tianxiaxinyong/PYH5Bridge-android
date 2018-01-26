package com.pycredit.h5sdk.perm.support;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static com.pycredit.h5sdk.perm.support.PermissionsPageManager.MANUFACTURER_MEIZU;
import static com.pycredit.h5sdk.perm.support.PermissionsPageManager.MANUFACTURER_OPPO;
import static com.pycredit.h5sdk.perm.support.PermissionsPageManager.MANUFACTURER_XIAOMI;


/**
 * Created by joker on 2017/9/16.
 */

public class ManufacturerSupportUtil {
    private static String[] forceManufacturers = {MANUFACTURER_XIAOMI.toUpperCase(), MANUFACTURER_MEIZU.toUpperCase()};
    private static Set<String> forceSet = new HashSet<>(Arrays.asList(forceManufacturers));
    private static String[] underMHasPermissionsRequestManufacturer = {MANUFACTURER_XIAOMI.toUpperCase(),
            MANUFACTURER_MEIZU.toUpperCase(), MANUFACTURER_OPPO.toUpperCase()};
    private static Set<String> underMSet = new HashSet<>(Arrays.asList
            (underMHasPermissionsRequestManufacturer));

    /**
     * those manufacturer that need request by some special measures, above
     * {@link Build.VERSION_CODES#M}
     *
     * @return
     */
    public static boolean isForceManufacturer() {
        return forceSet.contains(PermissionsPageManager.getManufacturer().toUpperCase());
    }

    /**
     * those manufacturer that need request permissions under {@link Build.VERSION_CODES#M},
     * above {@link Build.VERSION_CODES#LOLLIPOP}
     *
     * @return
     */
    public static boolean isUnderMHasPermissionRequestManufacturer() {
        return underMSet.contains(PermissionsPageManager.getManufacturer().toUpperCase());
    }

    public static boolean isLocationMustNeedGpsManufacturer() {
        return PermissionsPageManager.getManufacturer().equalsIgnoreCase(MANUFACTURER_OPPO);
    }

    /**
     * 1.is under {@link Build.VERSION_CODES#M}, above
     * {@link Build.VERSION_CODES#LOLLIPOP}
     * 2.has permissions check
     * 3.open under check
     * <p>
     * now, we know {@link PermissionsPageManager#isXIAOMI()}, {@link PermissionsPageManager#isMEIZU()}
     *
     * @param isUnderMNeedChecked
     * @return
     */
    public static boolean isUnderMNeedChecked(boolean isUnderMNeedChecked) {
        return isUnderMHasPermissionRequestManufacturer() && isUnderMNeedChecked &&
                isAndroidL();
    }

    /**
     * Build version code is under 6.0 but above 5.0
     *
     * @return
     */
    public static boolean isAndroidL() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES
                .LOLLIPOP && Build.VERSION.SDK_INT < Build.VERSION_CODES.M;
    }

    public static Set<String> getForceSet() {
        return forceSet;
    }

    public static Set<String> getUnderMSet() {
        return underMSet;
    }

    /**
     * 获取系统属性值
     *
     * @param propertyName
     * @return
     */
    public static String getSystemProperty(String propertyName) {
        String line = "";
        BufferedReader input = null;
        try {
            Process p = Runtime.getRuntime().exec("getprop " + propertyName);
            input = new BufferedReader(new InputStreamReader(p.getInputStream()), 1024);
            line = input.readLine();
        } catch (IOException ex) {
            ex.printStackTrace();
            return "";
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return line;
    }

    /**
     * @param context
     * @param packageName
     * @param activityName
     * @return
     */
    public static ActivityInfo getActivityInfo(Context context, String packageName, String activityName) {
        try {
            PackageInfo pi = context.getPackageManager().getPackageInfo(packageName,
                    PackageManager.GET_ACTIVITIES);
            for (ActivityInfo activityInfo : pi.activities) {
                if (activityInfo.name.equals(activityName)) {
                    return activityInfo;
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }

    /**
     * 获取跳转到目标Activity的Intent
     *
     * @param context
     * @param packageName
     * @param activityName
     * @return
     */
    public static Intent getIntentForActivity(Context context, String packageName, String activityName) {
        ActivityInfo activityInfo = getActivityInfo(context, packageName, activityName);
        if (activityInfo != null) {
            Intent intent = new Intent();
            intent.putExtra(PermissionsPageManager.LAUNCH_MODE, activityInfo.launchMode);
            ComponentName comp = new ComponentName(packageName, activityName);
            intent.setComponent(comp);
            return intent;
        }
        return null;
    }
}
