package com.pycredit.h5sdk.utils;

import android.annotation.SuppressLint;
import android.os.Build;
import android.webkit.WebView;

import java.text.MessageFormat;

public class WebViewUtils {

    public static final String JSCODE_FORMAT = "window[\"{0}\"] && {1}(\"{2}\")";

    @SuppressLint("NewApi")
    public static void evaluateJavascript(final WebView webView, String pureJsCode) {
        String jsCodeFormat = "javascript:{0}";
        final String jsCode = MessageFormat.format(jsCodeFormat, pureJsCode);
        webView.post(new Runnable() {
            @Override
            public void run() {
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {

                        webView.evaluateJavascript(jsCode, null);
                    } else {
                        webView.loadUrl(jsCode);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
