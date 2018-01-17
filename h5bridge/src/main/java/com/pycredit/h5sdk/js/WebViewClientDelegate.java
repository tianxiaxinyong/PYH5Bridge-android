package com.pycredit.h5sdk.js;

import android.webkit.WebView;

/**
 * @author huangx
 * @date 2018/1/17
 */

public interface WebViewClientDelegate {
    boolean shouldOverrideUrlLoading(WebView view, String url);
}
