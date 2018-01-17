package com.pycredit.h5sdk.js;

import android.webkit.GeolocationPermissions;

/**
 * @author huangx
 * @date 2018/1/16
 */

public interface OnGeolocationPermissionsShowPromptDelegate {
    void onGeolocationPermissionsShowPrompt(final String origin, final GeolocationPermissions.Callback callback);
}
