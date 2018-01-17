package com.pycredit.h5sdk.perm;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.AppOpsManagerCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 权限检查
 * Created by huangx on 2017/10/19.
 */

public class PermChecker implements OnRequestPermissionsResultDelegate, OnActivityResultDelegate {


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

    public PermChecker(Activity activity) {
        activityRef = new WeakReference<>(activity);
        contextRef = new WeakReference<Context>(activity);
    }

    public PermChecker(Fragment fragment) {
        fragmentRef = new WeakReference<>(fragment);
        contextRef = new WeakReference<>(fragment.getContext());
    }

    /**
     * 检查权限
     *
     * @param context
     * @param perms
     * @return
     */
    public static boolean hasPermissionAppOps(Context context, String[] perms) {
        for (String perm : perms) {
            String op = AppOpsManagerCompat.permissionToOp(perm);
            if (TextUtils.isEmpty(op)) {
                continue;
            }
            int result = AppOpsManagerCompat.noteProxyOp(context, op, context.getPackageName());//先从底层查
            if (result == AppOpsManagerCompat.MODE_IGNORED) {
                return false;
            }
            result = ContextCompat.checkSelfPermission(context, perm);
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
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
                        if (getSettingsIntent() != null) {
                            if (fragmentRef != null && fragmentRef.get() != null) {
                                fragmentRef.get().startActivityForResult(getSettingsIntent(), SETTING_REQUEST_CODE);
                            } else if (activityRef != null && activityRef.get() != null) {
                                activityRef.get().startActivityForResult(getSettingsIntent(), SETTING_REQUEST_CODE);
                            }
                        }
                    } else {
                        //没有获取到权限
                        if (callback != null) {
                            callback.onRequestFail();
                        }
                    }
                } else {
                    //有权限了
                    if (callback != null) {
                        callback.onRequestSuccess();
                    }
                }
            }
        }
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
        if (requestCode == SETTING_REQUEST_CODE) {
            if (hasPermissionAppOps(contextRef.get(), requestPerms)) {
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
}
