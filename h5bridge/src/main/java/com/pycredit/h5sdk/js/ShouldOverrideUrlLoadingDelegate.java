package com.pycredit.h5sdk.js;

import android.webkit.WebView;

/**
 * Created by huangx on 2017/10/19.
 */

public interface ShouldOverrideUrlLoadingDelegate {
    boolean shouldOverrideUrlLoading(WebView view, String url);
}
