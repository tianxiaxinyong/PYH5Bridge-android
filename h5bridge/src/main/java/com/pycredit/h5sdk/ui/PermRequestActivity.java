package com.pycredit.h5sdk.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.pycredit.h5sdk.perm.PermChecker;

/**
 * 权限申请中转页面
 *
 * @author huangx
 * @date 2017/11/21
 */

public class PermRequestActivity extends Activity {
    public static final String EXTRA_PERM_ARRAY = "extra_perm_array";
    public static final int PERM_REQUEST_CODE = 0X1111;
    public static final int SETTING_REQUEST_CODE = 0X1112;

    private PermChecker permChecker;
    private String[] perms;
    private static PermChecker.RequestPermCallback callback;

    public static void setCallback(PermChecker.RequestPermCallback callback) {
        PermRequestActivity.callback = callback;
    }

    public static void requestPermissions(Context context, String[] perms, PermChecker.RequestPermCallback callback) {
        PermRequestActivity.setCallback(callback);
        Intent intent = new Intent(context, PermRequestActivity.class);
        intent.putExtra(EXTRA_PERM_ARRAY, perms);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        perms = getIntent().getStringArrayExtra(EXTRA_PERM_ARRAY);
        if (perms != null && perms.length > 0) {
            permChecker = new PermChecker(this);
            permChecker.requestPermissions(PERM_REQUEST_CODE, perms, new PermChecker.RequestPermCallback() {
                @Override
                public void onRequestSuccess() {
                    if (callback != null) {
                        callback.onRequestSuccess();
                    }
                    finish();
                }

                @Override
                public void onRequestFail() {
                    if (callback != null) {
                        callback.onRequestFail();
                    }
                    finish();
                }
            });
        } else {
            if (callback != null) {
                callback.onRequestFail();
            }
            finish();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERM_REQUEST_CODE) {
            if (permChecker != null) {
                permChecker.onRequestPermissionsResult(requestCode, permissions, grantResults);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SETTING_REQUEST_CODE) {
            if (permChecker != null) {
                permChecker.onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (callback != null) {
            callback = null;
        }
        super.onDestroy();
    }
}
