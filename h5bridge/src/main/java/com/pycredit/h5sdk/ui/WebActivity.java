package com.pycredit.h5sdk.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.HashMap;
import java.util.Map;

import static com.pycredit.h5sdk.js.JsCallAppErrorCode.ERROR_PAY_APP_NOT_INSTALL;
import static com.pycredit.h5sdk.js.JsCallAppErrorCode.ERROR_PAY_USER_CANCEL;

/**
 * 支付中转页面
 * Created by huangx on 2017/10/14.
 */

public class WebActivity extends Activity {

    public static final int REQUEST_CODE_WEIXIN = 2001;
    public static final int REQUEST_CODE_ALIPAY = 2002;

    private WebView webView;

    private boolean noError;

    private Handler handler;

    private static Callback callback;

    /**
     * 支付回调
     */
    public interface Callback {
        /**
         * 成功
         */
        void onSuccess();

        /**
         * 失败
         *
         * @param errCode
         * @param errMsg
         */
        void onFail(String errCode, String errMsg);
    }

    public static void setCallback(Callback callback) {
        WebActivity.callback = callback;
    }

    public static void startPay(Context context, String url, String referer, Callback callback) {
        WebActivity.setCallback(callback);
        Intent intent = new Intent(context, WebActivity.class);
        intent.putExtra("url", url);
        intent.putExtra("referer", referer);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handler = new Handler();
        webView = new WebView(this.getApplicationContext());
        setContentView(webView, new ViewGroup.LayoutParams(0, 0));
        webView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (handler != null) {
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (!noError) {//中转页面有支付报的错
                                if (callback != null) {
                                    callback.onFail("-1", "支付出错");
                                }
                                finish();
                            }
                        }
                    }, 500);
                }
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                PackageManager packageManager = getPackageManager();
                if (url.startsWith("weixin://")) {//微信支付
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(url));
                    if (intent.resolveActivity(packageManager) != null) {
                        startActivityForResult(intent, REQUEST_CODE_WEIXIN);
                    } else {
                        if (callback != null) {
                            callback.onFail(ERROR_PAY_APP_NOT_INSTALL.getCode(), ERROR_PAY_APP_NOT_INSTALL.getMsg());
                        }
                        finish();
                    }
                    noError = true;
                    return true;
                } else if (parseScheme(url)) {//支付宝支付(暂未支持)
                    try {
                        Intent intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                        if (intent != null) {
                            intent.addCategory("android.intent.category.BROWSABLE");
                            intent.setComponent(null);
                            if (intent.resolveActivity(packageManager) != null) {
                                startActivityForResult(intent, REQUEST_CODE_ALIPAY);
                            } else {
                                if (callback != null) {
                                    callback.onFail(ERROR_PAY_APP_NOT_INSTALL.getCode(), ERROR_PAY_APP_NOT_INSTALL.getMsg());
                                }
                                finish();
                            }
                            return true;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    noError = true;
                    return true;
                }
                return super.shouldOverrideUrlLoading(view, url);
            }
        });
        webView.setWebChromeClient(new WebChromeClient());
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        // 禁用部分可能存在外部调用风险的属性和js接口调用配置
        webSettings.setSavePassword(false);
        webSettings.setAllowFileAccess(false);
        webSettings.setAllowFileAccessFromFileURLs(false);
        webSettings.setAllowUniversalAccessFromFileURLs(false);
        webView.removeJavascriptInterface("accessibility");
        webView.removeJavascriptInterface("accessibilityTraversal");
        webView.removeJavascriptInterface("searchBoxJavaBridge_");
        String url = getIntent().getStringExtra("url");
        String referer = getIntent().getStringExtra("referer");
        Map<String, String> extraHeaders = new HashMap<>();
        if (!TextUtils.isEmpty(referer)) {
            extraHeaders.put("Referer", referer);
        }
        webView.loadUrl(url, extraHeaders);
    }

    @Override
    protected void onDestroy() {
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
            handler = null;
        }
        if (callback != null) {
            callback = null;
        }
        super.onDestroy();
    }

    public boolean parseScheme(String url) {
        if (url.contains("platformapi/startapp")) {
            return true;
        } else if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M && url.contains("platformapi") && url.contains("startapp")) {
            return true;
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_WEIXIN) {
            if (callback != null) {
                callback.onSuccess();
            }
            finish();
        } else if (requestCode == REQUEST_CODE_ALIPAY) {
            if (callback != null) {
                callback.onSuccess();
            }
            finish();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
