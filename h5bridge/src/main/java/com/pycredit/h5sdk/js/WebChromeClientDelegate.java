package com.pycredit.h5sdk.js;

import android.net.Uri;
import android.webkit.GeolocationPermissions;
import android.webkit.PermissionRequest;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

/**
 * @author huangx
 * @date 2018/1/17
 */

public interface WebChromeClientDelegate {

    void openFileChooser(ValueCallback<Uri> uploadFile, String acceptType, String capture);

    boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams);

    void onGeolocationPermissionsShowPrompt(final String origin, final GeolocationPermissions.Callback callback);

    void onPermissionRequest(PermissionRequest request);
}
