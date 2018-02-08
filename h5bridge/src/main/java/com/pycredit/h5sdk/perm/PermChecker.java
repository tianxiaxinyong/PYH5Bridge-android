package com.pycredit.h5sdk.perm;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionGroupInfo;
import android.content.pm.PermissionInfo;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.PermissionChecker;
import android.text.TextUtils;

import com.pycredit.h5sdk.perm.support.CNPermChecker;
import com.pycredit.h5sdk.perm.support.ManufacturerSupportUtil;
import com.pycredit.h5sdk.perm.support.PermissionsPageManager;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 权限检查
 * Created by huangx on 2017/10/19.
 */

public class PermChecker implements ActivityDelegate {


    public interface RequestPermCallback {
        /**
         * 请求权限成功
         */
        void onRequestSuccess();

        /**
         * 请求权限失败
         */
        void onRequestFail();
    }

    protected final int SETTING_REQUEST_CODE = 0X1112;

    protected WeakReference<Context> contextRef;
    protected WeakReference<Activity> activityRef;
    protected WeakReference<Fragment> fragmentRef;

    protected RequestPermCallback callback;

    protected String[] requestPerms;
    protected int permRequestCode;

    public int settingLaunchMode = -1;

    public PermChecker(Activity activity) {
        activityRef = new WeakReference<>(activity);
        contextRef = new WeakReference<Context>(activity);
    }

    public PermChecker(Fragment fragment) {
        fragmentRef = new WeakReference<>(fragment);
        contextRef = new WeakReference<>(fragment.getContext());
    }

    /**
     * 检查是否有权限
     *
     * @param context
     * @param perms
     * @return
     */
    public static boolean hasPermissions(Context context, String[] perms) {
        for (String perm : perms) {
            //普通处理
            if (getTargetVersion(context) >= Build.VERSION_CODES.M) {
                if (ContextCompat.checkSelfPermission(context, perm) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            } else {
                if (PermissionChecker.checkSelfPermission(context, perm) != PermissionChecker.PERMISSION_GRANTED) {
                    return false;
                }
            }
            //针对国内厂商处理
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {//Android 6.0以上的要特殊检查的
                if (ManufacturerSupportUtil.upMNeedCNCheck()) {
                    if (!CNPermChecker.isPermissionGranted(context, perm)) {
                        return false;
                    }
                }
            } else {//Android 6.0以下要特殊检查的
                if (PermissionsPageManager.isOPPO() && Manifest.permission.RECORD_AUDIO.equals(perm)) {//OPPO A59m 、R9m  5.1录音检查总是失败,所以默认他是成功的
                    if (PermissionsPageManager.getColorOsVersion().isEqual("3.0") || PermissionsPageManager.getColorOsVersion().isHigherThan("3.0")) {//A59m 、R9m ColorOs为3.0.0以上
                        return true;
                    }
                }
                if (ManufacturerSupportUtil.underMNeedCNCheck()) {
                    if (!CNPermChecker.isPermissionGranted(context, perm)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * 获取目标SDK版本
     *
     * @param context
     * @return
     */
    private static int getTargetVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return packageInfo.applicationInfo.targetSdkVersion;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return 0;
    }


    /**
     * 请求权限
     *
     * @param requestCode
     * @param perms
     */
    public void requestPermissions(int requestCode, @NonNull String[] perms, RequestPermCallback callback) {
        permRequestCode = requestCode;
        requestPerms = perms;
        this.callback = callback;
        if (fragmentRef != null && fragmentRef.get() != null) {
            fragmentRef.get().requestPermissions(perms, requestCode);
        } else if (activityRef != null && activityRef.get() != null) {
            ActivityCompat.requestPermissions(activityRef.get(), perms, requestCode);
        }
    }

    /**
     * 是否需要解释
     *
     * @param perm
     * @return
     */
    private boolean shouldShowRequestPermissionRationale(@NonNull String perm) {
        if (fragmentRef != null && fragmentRef.get() != null) {
            return fragmentRef.get().shouldShowRequestPermissionRationale(perm);
        } else if (activityRef != null && activityRef.get() != null) {
            return ActivityCompat.shouldShowRequestPermissionRationale(activityRef.get(), perm);
        }
        return false;
    }

    /**
     * 部分权限被永久拒绝(只能在请求权限回调后调用)
     *
     * @param perms
     * @return
     */
    public boolean somePermissionPermanentlyDenied(@NonNull List<String> perms) {
        Iterator var2 = perms.iterator();
        String deniedPermission;
        do {
            if (!var2.hasNext()) {
                return false;
            }
            deniedPermission = (String) var2.next();
        } while (!this.permissionPermanentlyDenied(deniedPermission));
        return true;
    }

    /**
     * 权限被永久拒绝(只能在请求权限回调后调用)
     *
     * @param perms
     * @return
     */
    public boolean permissionPermanentlyDenied(@NonNull String perms) {
        return !this.shouldShowRequestPermissionRationale(perms);
    }

    @Override
    public void onResume() {
        if (settingLaunchMode == ActivityInfo.LAUNCH_SINGLE_TASK || settingLaunchMode == ActivityInfo.LAUNCH_SINGLE_INSTANCE) {
            if (hasPermissions(contextRef.get(), requestPerms)) {
                if (callback != null) {
                    callback.onRequestSuccess();
                }
            } else {
                if (callback != null) {
                    callback.onRequestFail();
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == permRequestCode) {
            if (grantResults != null) {
                List<String> granted = new ArrayList();
                List<String> denied = new ArrayList();
                for (int i = 0; i < permissions.length; ++i) {
                    String perm = permissions[i];
                    if (grantResults[i] == 0) {
                        granted.add(perm);
                    } else {
                        denied.add(perm);
                    }
                }
                if (!denied.isEmpty()) {//有部份没有权限
                    if (somePermissionPermanentlyDenied(denied)) {
                        //跳到设置页面
                        showSettings();
                    } else {
                        if (hasPermissions(contextRef.get(), requestPerms)) { //有权限了
                            if (callback != null) {
                                callback.onRequestSuccess();
                            }
                        } else {
                            //跳到设置页面
                            showSettings();
                        }
                    }
                } else {
                    if (!hasPermissions(contextRef.get(), requestPerms)) {
                        //跳到设置页面
                        showSettings();
                    } else {
                        //有权限了
                        if (callback != null) {
                            callback.onRequestSuccess();
                        }
                    }
                }
            }
        }
    }

    /**
     * 显示权限设置页面
     *
     * @return
     */
    public void showSettings() {
        if (contextRef != null && contextRef.get() != null) {
            StringBuilder sb = new StringBuilder();
            sb.append("部分权限被禁止，请到权限设置页面打开以下权限：\n");
            CharSequence permGroupLabel = getPermGroupLabel(contextRef.get(), requestPerms);
            if (!TextUtils.isEmpty(permGroupLabel)) {
                sb.append(permGroupLabel);
            }
            new AlertDialog.Builder(contextRef.get())
                    .setMessage(sb.toString())
                    .setPositiveButton("去设置", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent settingsIntent = getPermSettingsIntent();
                            if (settingsIntent != null) {
                                if (fragmentRef != null && fragmentRef.get() != null) {
                                    try {
                                        settingLaunchMode = settingsIntent.getIntExtra(PermissionsPageManager.LAUNCH_MODE, ActivityInfo.LAUNCH_MULTIPLE);
                                        fragmentRef.get().startActivityForResult(settingsIntent, SETTING_REQUEST_CODE);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        settingLaunchMode = ActivityInfo.LAUNCH_MULTIPLE;
                                        fragmentRef.get().startActivityForResult(getSettingsIntent(), SETTING_REQUEST_CODE);
                                    }
                                } else if (activityRef != null && activityRef.get() != null) {
                                    try {
                                        settingLaunchMode = settingsIntent.getIntExtra(PermissionsPageManager.LAUNCH_MODE, ActivityInfo.LAUNCH_MULTIPLE);
                                        activityRef.get().startActivityForResult(settingsIntent, SETTING_REQUEST_CODE);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        settingLaunchMode = ActivityInfo.LAUNCH_MULTIPLE;
                                        activityRef.get().startActivityForResult(getSettingsIntent(), SETTING_REQUEST_CODE);
                                    }
                                }
                            } else {
                                if (callback != null) {
                                    callback.onRequestFail();
                                }
                            }
                        }
                    }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (callback != null) {
                        callback.onRequestFail();
                    }
                }
            }).setCancelable(false).show();

        }
    }

    public Intent getPermSettingsIntent() {
        if (contextRef != null && contextRef.get() != null) {
            Intent intent = PermissionsPageManager.getIntent((Activity) contextRef.get());
            return intent != null ? intent : getSettingsIntent();
        }
        return null;
    }

    /**
     * 获取设置页面intent
     *
     * @return
     */
    public Intent getSettingsIntent() {
        if (contextRef.get() != null) {
            Intent intent = new Intent("android.settings.APPLICATION_DETAILS_SETTINGS");
            intent.setData(Uri.fromParts("package", contextRef.get().getPackageName(), (String) null));
            return intent;
        }
        return null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SETTING_REQUEST_CODE && settingLaunchMode != ActivityInfo.LAUNCH_SINGLE_TASK && settingLaunchMode != ActivityInfo.LAUNCH_SINGLE_INSTANCE) {
            if (hasPermissions(contextRef.get(), requestPerms)) {
                if (callback != null) {
                    callback.onRequestSuccess();
                }
            } else {
                if (callback != null) {
                    callback.onRequestFail();
                }
            }
        }
    }

    /**
     * 获取权限名称
     *
     * @param context
     * @param perms
     * @return
     */
    public static CharSequence getPermGroupLabel(Context context, String... perms) {
        List<CharSequence> permGroupLabelList = new ArrayList<>();
        PackageManager packageManager = context.getPackageManager();
        if (perms != null && perms.length > 0) {
            try {
                for (String perm : perms) {
                    PermissionInfo permissionInfo = packageManager.getPermissionInfo(perm, 0);
                    if (permissionInfo != null) {
                        PermissionGroupInfo permissionGroupInfo = packageManager.getPermissionGroupInfo(permissionInfo.group, 0);
                        if (permissionGroupInfo != null) {
                            CharSequence label = permissionGroupInfo.loadLabel(packageManager);
                            if (!permGroupLabelList.contains(label)) {
                                permGroupLabelList.add(label);
                            }
                        }
                    }
                }
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        if (!permGroupLabelList.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < permGroupLabelList.size(); i++) {
                sb.append(permGroupLabelList.get(i));
                if (i != permGroupLabelList.size() - 1) {
                    sb.append("\n");
                }
            }
            return sb.toString();
        }
        return null;
    }
}
