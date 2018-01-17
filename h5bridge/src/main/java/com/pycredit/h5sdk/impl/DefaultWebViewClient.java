package com.pycredit.h5sdk.impl;

import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.pycredit.h5sdk.H5SDKHelper;

/**
 * @author huangx
 * @date 2018/1/16
 */

public class DefaultWebViewClient extends WebViewClient {
    protected H5SDKHelper h5SDKHelper;

    public DefaultWebViewClient(H5SDKHelper h5SDKHelper) {
        this.h5SDKHelper = h5SDKHelper;
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        if (h5SDKHelper.shouldOverrideUrlLoading(view, url)) {
            return true;
        }
        return super.shouldOverrideUrlLoading(view, url);
    }
}
