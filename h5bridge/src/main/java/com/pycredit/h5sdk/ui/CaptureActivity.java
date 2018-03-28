package com.pycredit.h5sdk.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;

import com.pycredit.h5sdk.capture.CaptureCallback;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static com.pycredit.h5sdk.js.JsCallAppErrorCode.ERROR_CAMERA_USER_CANCEL;

/**
 * 拍照中转页面
 *
 * @author huangx
 * @date 2017/11/22
 */

public class CaptureActivity extends Activity {

    public static final String EXTRA_SAVE_PATH = "extra_save_path";
    public static final String EXTRA_CALLBACK_KEY = "extra_callback_key";

    private final int CAPTURE_REQUEST_CODE = 1003;

    private String savePath;

    private static Map<Long, CaptureCallback> callbackMap = new HashMap<>();

    private long currentCallbackKey;

    public static void setCallback(long callbackKey, CaptureCallback callback) {
        callbackMap.put(callbackKey, callback);
    }

    public static void startCapture(Context context, String savePath, CaptureCallback callback) {
        long callbackKey = System.currentTimeMillis();
        setCallback(callbackKey, callback);
        Intent intent = new Intent(context, CaptureActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(EXTRA_SAVE_PATH, savePath);
        intent.putExtra(EXTRA_CALLBACK_KEY, callbackKey);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        savePath = getIntent().getStringExtra(EXTRA_SAVE_PATH);
        currentCallbackKey = getIntent().getLongExtra(EXTRA_CALLBACK_KEY, 0);
        Intent captureIntent = getCaptureIntent(savePath);
        PackageManager packageManager = getPackageManager();
        if (captureIntent != null && captureIntent.resolveActivity(packageManager) != null) {
            try {
                startActivityForResult(captureIntent, CAPTURE_REQUEST_CODE);
            } catch (Exception e) {
                if (getCurrentCallback() != null) {
                    getCurrentCallback().onFail("-1", "无法调起系统相机");
                }
                finish();
            }
        } else {
            if (getCurrentCallback() != null) {
                getCurrentCallback().onFail("-1", "无法调起系统相机");
            }
            finish();
        }
    }

    private CaptureCallback getCurrentCallback() {
        return callbackMap.get(currentCallbackKey);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAPTURE_REQUEST_CODE) {
            if (getCurrentCallback() != null) {
                if (resultCode == Activity.RESULT_OK) {
                    getCurrentCallback().onSuccess(savePath);
                } else {
                    getCurrentCallback().onFail(ERROR_CAMERA_USER_CANCEL.getCode(), ERROR_CAMERA_USER_CANCEL.getMsg());
                }
            }
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        callbackMap.remove(currentCallbackKey);
        super.onDestroy();
    }

    public Intent getCaptureIntent(String filePath) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        PackageManager packageManager = getPackageManager();
        if (intent.resolveActivity(packageManager) != null) {
            try {
                File file = new File(filePath);
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {//如果api大于Android api23
                    Uri imageUri = FileProvider.getUriForFile(this, getPackageName() + ".h5sdk.fileprovider", file);//替换获取uri的获取方式
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);//加入flag
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                } else {
                    intent.addCategory(Intent.CATEGORY_DEFAULT);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
                }
                return intent;
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }
}
