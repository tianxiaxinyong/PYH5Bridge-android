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
import android.webkit.PermissionRequest;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import com.alipay.sdk.app.H5PayCallback;
import com.alipay.sdk.app.PayTask;
import com.alipay.sdk.util.H5PayResultModel;
import com.pycredit.h5sdk.js.WebChromeClientDelegate;
import com.pycredit.h5sdk.js.WebViewClientDelegate;
import com.pycredit.h5sdk.perm.PermChecker;
import com.pycredit.h5sdk.ui.FileChooseActivity;
import com.pycredit.h5sdk.ui.PermRequestActivity;

import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by huangx on 2017/10/19.
 */

public class H5JsHelper implements WebViewClientDelegate, WebChromeClientDelegate {

    public static final boolean hasAlipayLib;

    protected WeakReference<Context> contextRef;
    protected WeakReference<Activity> activityRef;
    protected WeakReference<Fragment> fragmentRef;

    private Object mPayTask;

    static {
        boolean tag = true;
        try {
            Class.forName("com.alipay.sdk.app.PayTask");
        } catch (ClassNotFoundException e) {
            tag = false;
        }
        hasAlipayLib = tag;
    }


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
        if ((url.startsWith("http://") || url.startsWith("https://")) && isAlipay(view, url)) {
            return true;
        }
        if (url.startsWith("tel:") || url.startsWith("mailto:") || url.startsWith("sms:") || url.startsWith("weixin://") || url.startsWith("alipays://") || url.startsWith("alipay")) {
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
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < acceptTypes.length; i++) {
                    if (TextUtils.isEmpty(acceptTypes[i])) {
                        continue;
                    }
                    sb.append(acceptTypes[i]);
                    if (i != acceptTypes.length - 1) {
                        sb.append(",");
                    }
                }
                String type = sb.toString();
                if (!TextUtils.isEmpty(type)) {
                    acceptType = type;
                }
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

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onPermissionRequest(final PermissionRequest request) {
        List<String> permList = new ArrayList<>();
        String[] resources = request.getResources();
        if (resources != null && resources.length > 0) {
            for (String resource : resources) {
                if (resource.equals(PermissionRequest.RESOURCE_VIDEO_CAPTURE)) {
                    if (!permList.contains(Manifest.permission.CAMERA)) {
                        permList.add(Manifest.permission.CAMERA);
                    }
                } else if (resource.equals(PermissionRequest.RESOURCE_AUDIO_CAPTURE)) {
                    if (!permList.contains(Manifest.permission.RECORD_AUDIO)) {
                        permList.add(Manifest.permission.RECORD_AUDIO);
                    }
                    if (!permList.contains(Manifest.permission.MODIFY_AUDIO_SETTINGS)) {
                        permList.add(Manifest.permission.MODIFY_AUDIO_SETTINGS);
                    }
                } else if (resource.equals(PermissionRequest.RESOURCE_PROTECTED_MEDIA_ID)) {
                    if (!permList.contains(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                        permList.add(Manifest.permission.READ_EXTERNAL_STORAGE);
                    }
                }
            }
        }
        if (permList.isEmpty()) {
            request.grant(request.getResources());
        } else {
            if (contextRef != null && contextRef.get() != null) {
                String[] perms = permList.toArray(new String[permList.size()]);
                if (PermChecker.hasPermissionAppOps(contextRef.get(), perms)) {
                    request.grant(request.getResources());
                } else {
                    PermRequestActivity.requestPermissions(contextRef.get(), perms, new PermChecker.RequestPermCallback() {
                        @Override
                        public void onRequestSuccess() {
                            request.grant(request.getResources());
                        }

                        @Override
                        public void onRequestFail() {
                            request.deny();
                        }
                    });
                }
            } else {
                request.grant(request.getResources());
            }
        }
    }


    public boolean isAlipay(final WebView webView, String url) {
        if (hasAlipayLib && contextRef != null && contextRef.get() != null && contextRef.get() instanceof Activity) {
            if (mPayTask == null) {
                try {
                    Class clazz = Class.forName("com.alipay.sdk.app.PayTask");
                    Constructor<?> mConstructor = clazz.getConstructor(Activity.class);
                    mPayTask = mConstructor.newInstance((Activity) contextRef.get());
                    PayTask task = (PayTask) mPayTask;
                    boolean isIntercepted = task.payInterceptorWithUrl(url, true, new H5PayCallback() {
                        @Override
                        public void onPayResult(H5PayResultModel h5PayResultModel) {
                            final String returnUrl = h5PayResultModel.getReturnUrl();
                            if (!TextUtils.isEmpty(returnUrl)) {
                                if (contextRef != null && contextRef.get() != null && contextRef.get() instanceof Activity) {
                                    ((Activity) contextRef.get()).runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            webView.loadUrl(returnUrl);
                                        }
                                    });
                                }
                            }
                        }
                    });
                    return isIntercepted;
                } catch (Throwable e) {
                }
            }
        }
        return false;
    }
}
