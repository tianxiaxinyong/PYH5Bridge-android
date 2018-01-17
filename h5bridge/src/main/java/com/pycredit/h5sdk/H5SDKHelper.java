package com.pycredit.h5sdk;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.GeolocationPermissions;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.pycredit.h5sdk.capture.Capture;
import com.pycredit.h5sdk.impl.BannerCallback;
import com.pycredit.h5sdk.impl.DefaultWebChromeClient;
import com.pycredit.h5sdk.impl.DefaultWebViewClient;
import com.pycredit.h5sdk.impl.OnBackPressedDelegate;
import com.pycredit.h5sdk.impl.PYCreditJsBridge;
import com.pycredit.h5sdk.impl.PYCreditJsCallAppProcessor;
import com.pycredit.h5sdk.js.BaseBridge;
import com.pycredit.h5sdk.js.OnGeolocationPermissionsShowPromptDelegate;
import com.pycredit.h5sdk.js.OnShowFileChooserDelegate;
import com.pycredit.h5sdk.js.ShouldOverrideUrlLoadingDelegate;

import java.lang.ref.WeakReference;

/**
 * Created by huangx on 2017/10/13.
 */

public class H5SDKHelper implements OnBackPressedDelegate, ShouldOverrideUrlLoadingDelegate, OnShowFileChooserDelegate, OnGeolocationPermissionsShowPromptDelegate {

    private PYCreditJsCallAppProcessor processor;

    private WeakReference<WebView> webViewRef;

    private H5JsHelper h5JsHelper;

    public H5SDKHelper(@NonNull Activity activity, @NonNull WebView webView) {
        processor = new PYCreditJsCallAppProcessor(activity);
        h5JsHelper = new H5JsHelper(activity);
        init(webView);
    }

    public H5SDKHelper(@NonNull Fragment fragment, @NonNull WebView webView) {
        processor = new PYCreditJsCallAppProcessor(fragment);
        h5JsHelper = new H5JsHelper(fragment);
        init(webView);
    }

    private void init(final WebView webView) {
        webViewRef = new WeakReference<>(webView);
        PYCreditJsBridge bridge = new PYCreditJsBridge(webView, processor);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setGeolocationEnabled(true);
        webView.getSettings().setGeolocationDatabasePath(webView.getContext().getFilesDir().getPath());
        //android 默认5.0以上不支持mixed content。主要是为了解决图片显示的问题；//http https
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.setAcceptThirdPartyCookies(webView, true);
        }
        webView.addJavascriptInterface(bridge, BaseBridge.INTERFACE_NAME);
        webView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                PackageManager packageManager = webView.getContext().getPackageManager();
                if (intent.resolveActivity(packageManager) != null) {
                    webView.getContext().startActivity(intent);
                }
            }
        });
    }

    public void initDefaultSettings() {
        if (webViewRef != null && webViewRef.get() != null) {
            webViewRef.get().setWebViewClient(new DefaultWebViewClient(this));
            webViewRef.get().setWebChromeClient(new DefaultWebChromeClient(this));
        }
    }

    /**
     * 设置广告
     *
     * @param bannerImageUrl
     * @param callback
     */
    public void setBanner(String bannerImageUrl, BannerCallback callback) {
        processor.setBanner(bannerImageUrl, callback);
    }

    /**
     * 设置自定义拍照实现
     *
     * @param capture
     */
    public void setCapture(Capture capture) {
        processor.setCapture(capture);
    }

    /**
     * 用于拍照回调
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Deprecated
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    }

    /**
     * 用于申请必要权限时使用
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Deprecated
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    }

    @Override
    public boolean onBackPressed() {
        if (webViewRef != null && webViewRef.get() != null) {
            if (webViewRef.get().canGoBack()) {
                webViewRef.get().goBack();
                return true;
            }
        }
        return false;
    }

    public void onResume() {
        if (webViewRef != null && webViewRef.get() != null) {
            webViewRef.get().onResume();
        }
    }

    public void onPause() {
        if (webViewRef != null && webViewRef.get() != null) {
            webViewRef.get().onPause();
        }
    }

    public void onDestroy() {
        if (webViewRef != null && webViewRef.get() != null) {
            webViewRef.get().loadUrl("about:blank");
            ViewParent parent = webViewRef.get().getParent();
            if (parent != null && parent instanceof ViewGroup) {
                ViewGroup vg = (ViewGroup) parent;
                vg.removeAllViews();
            }
            webViewRef.get().removeAllViews();
            webViewRef.get().destroy();
        }
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        return h5JsHelper.shouldOverrideUrlLoading(view, url);
    }

    @Override
    public void openFileChooser(ValueCallback<Uri> uploadFile, String acceptType, String capture) {
        h5JsHelper.openFileChooser(uploadFile, acceptType, capture);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams) {
        return h5JsHelper.onShowFileChooser(webView, filePathCallback, fileChooserParams);
    }

    @Override
    public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
        h5JsHelper.onGeolocationPermissionsShowPrompt(origin, callback);
    }

    public static String getSdkVersion() {
        return BuildConfig.VERSION_NAME;
    }
}
