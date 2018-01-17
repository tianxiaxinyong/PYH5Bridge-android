package com.pycredit.h5sdk;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.webkit.GeolocationPermissions;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import com.pycredit.h5sdk.js.OnGeolocationPermissionsShowPromptDelegate;
import com.pycredit.h5sdk.js.OnShowFileChooserDelegate;
import com.pycredit.h5sdk.js.ShouldOverrideUrlLoadingDelegate;
import com.pycredit.h5sdk.perm.PermChecker;
import com.pycredit.h5sdk.ui.FileChooseActivity;
import com.pycredit.h5sdk.ui.PermRequestActivity;

import java.lang.ref.WeakReference;

/**
 * Created by huangx on 2017/10/19.
 */

public class H5JsHelper implements ShouldOverrideUrlLoadingDelegate, OnShowFileChooserDelegate, OnGeolocationPermissionsShowPromptDelegate {

    protected WeakReference<Context> contextRef;
    protected WeakReference<Activity> activityRef;
    protected WeakReference<Fragment> fragmentRef;

    public H5JsHelper(Activity activity) {
        activityRef = new WeakReference<>(activity);
        contextRef = new WeakReference<Context>(activity);
    }

    public H5JsHelper(Fragment fragment) {
        fragmentRef = new WeakReference<>(fragment);
        contextRef = new WeakReference<>(fragment.getContext());
    }

    @SuppressLint("MissingPermission")
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        if (url.startsWith("tel:") || url.startsWith("mailto:") || url.startsWith("sms:")) {
            if (contextRef != null && contextRef.get() != null) {
                final Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                PackageManager packageManager = contextRef.get().getPackageManager();
                if (intent.resolveActivity(packageManager) != null) {
                    contextRef.get().startActivity(intent);
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public void openFileChooser(final ValueCallback<Uri> uploadFile, String acceptType, String capture) {
        if (contextRef != null && contextRef.get() != null) {
            FileChooseActivity.startFileChoose(contextRef.get(), acceptType, !TextUtils.isEmpty(capture), new FileChooseActivity.FileChooseCallback() {
                @Override
                public void onSuccess(Uri uri) {
                    uploadFile.onReceiveValue(uri);
                }

                @Override
                public void onFail() {
                    uploadFile.onReceiveValue(null);
                }
            });
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public boolean onShowFileChooser(WebView webView, final ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams) {
        if (contextRef != null && contextRef.get() != null) {
            String acceptType = "*/*";
            boolean captureEnabled = fileChooserParams.isCaptureEnabled();
            String[] acceptTypes = fileChooserParams.getAcceptTypes();
            if (acceptTypes != null && acceptTypes.length > 0) {
                acceptType = acceptTypes[0];
            }
            FileChooseActivity.startFileChoose(contextRef.get(), acceptType, captureEnabled, new FileChooseActivity.FileChooseCallback() {
                @Override
                public void onSuccess(Uri uri) {
                    filePathCallback.onReceiveValue(new Uri[]{uri});
                }

                @Override
                public void onFail() {
                    filePathCallback.onReceiveValue(null);
                }
            });
            return true;
        }
        return false;
    }

    @Override
    public void onGeolocationPermissionsShowPrompt(final String origin, final GeolocationPermissions.Callback callback) {
        if (contextRef != null && contextRef.get() != null) {
            if (PermChecker.hasPermissionAppOps(contextRef.get(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION})) {
                callback.invoke(origin, true, true);
            } else {
                PermRequestActivity.requestPermissions(contextRef.get(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, new PermChecker.RequestPermCallback() {
                    @Override
                    public void onRequestSuccess() {
                        callback.invoke(origin, true, true);
                    }

                    @Override
                    public void onRequestFail() {
                        callback.invoke(origin, false, false);
                    }
                });
            }
        }
    }
}
