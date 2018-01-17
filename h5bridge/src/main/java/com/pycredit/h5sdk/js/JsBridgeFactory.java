package com.pycredit.h5sdk.js;

import android.webkit.WebView;

/**
 * Created by huangx on 2017/2/16.
 */

public interface JsBridgeFactory {
    /**
     * 提供JsBridge
     *
     * @param webView
     */
    Object createJsBridge(WebView webView);
}
