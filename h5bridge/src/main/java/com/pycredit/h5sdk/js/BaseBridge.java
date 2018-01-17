package com.pycredit.h5sdk.js;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import com.pycredit.h5sdk.utils.WebViewUtils;

import java.lang.ref.WeakReference;

/**
 * 通用JS调用APP 注入对像
 * Created by huangx on 2017/2/14.
 */

public abstract class BaseBridge<T extends JsCallAppProcess> implements JsCallAppCallback {

    private static final String TAG = "BaseBridge";

    public final static String INTERFACE_NAME = "PYCREDIT_BRIDGE";

    protected WeakReference<WebView> webViewWeakReference;
    /**
     * JS调用APP真正执行者
     */
    protected WeakReference<T> jsCallAppProcessReference;

    final Handler uiHandler = new Handler(Looper.getMainLooper());

    public BaseBridge(WebView webView, @NonNull T jsCallAppProcess) {
        webViewWeakReference = new WeakReference<WebView>(webView);
        this.jsCallAppProcessReference = new WeakReference<T>(jsCallAppProcess);
    }

    public T getJsCallAppProcess() {
        return jsCallAppProcessReference.get();
    }

    /**
     * 在主线程中运行(不能在WebView所在线程运行)
     *
     * @param runnable
     */
    protected void runOnUiThread(Runnable runnable) {
        uiHandler.post(runnable);
    }

    /**
     * 成功回调
     *
     * @param js2AppInfo
     * @param app2JsInfo
     * @param encoder
     */
    @Override
    public void jsCallAppSuccess(Js2AppInfo js2AppInfo, App2JsInfo app2JsInfo, JsParser encoder) {
        if (encoder != null) {
            String jsCode = encoder.encode(true, js2AppInfo, app2JsInfo);
            if (jsCode != null) {
                appBackJs(jsCode);
            }
        }
    }

    /**
     * 失败回调
     *
     * @param js2AppInfo
     * @param app2JsInfo
     * @param encoder
     */
    @Override
    public void jsCallAppFail(Js2AppInfo js2AppInfo, App2JsInfo app2JsInfo, JsParser encoder) {
        if (encoder != null) {
            String jsCode = encoder.encode(false, js2AppInfo, app2JsInfo);
            if (jsCode != null) {
                appBackJs(jsCode);
            }
        }
    }

    /**
     * APP回应JS调用
     *
     * @param jsCode
     */
    public void appBackJs(String jsCode) {
        Log.d(TAG, jsCode);
        if (webViewWeakReference.get() != null) {
            WebViewUtils.evaluateJavascript(webViewWeakReference.get(), jsCode);
        }
    }

    @Override
    @JavascriptInterface
    public String toString() {
        return INTERFACE_NAME;
    }
}
