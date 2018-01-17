package com.pycredit.h5sdk.impl;

import android.net.Uri;
import android.webkit.GeolocationPermissions;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import com.pycredit.h5sdk.H5SDKHelper;

/**
 * @author huangx
 * @date 2018/1/16
 */

public class DefaultWebChromeClient extends WebChromeClient {
    protected H5SDKHelper h5SDKHelper;

    public DefaultWebChromeClient(H5SDKHelper h5SDKHelper) {
        this.h5SDKHelper = h5SDKHelper;
    }

    //Android 5.0 以下 必须重写此方法
    public void openFileChooser(ValueCallback<Uri> uploadFile, String acceptType, String capture) {
        h5SDKHelper.openFileChooser(uploadFile, acceptType, capture);
    }

    //Android 5.0 及以上 必须重写此方法
    @Override
    public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
        if (h5SDKHelper.onShowFileChooser(webView, filePathCallback, fileChooserParams)) {
            return true;
        }
        return super.onShowFileChooser(webView, filePathCallback, fileChooserParams);
    }

    @Override
    public void onGeolocationPermissionsShowPrompt(final String origin, final GeolocationPermissions.Callback callback) {
        h5SDKHelper.onGeolocationPermissionsShowPrompt(origin, callback);
    }
}
