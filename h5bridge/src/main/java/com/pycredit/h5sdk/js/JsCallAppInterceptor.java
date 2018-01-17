package com.pycredit.h5sdk.js;

/**
 * 拦截器
 * Created by huangx on 2017/2/28.
 */

public interface JsCallAppInterceptor {
    /**
     * 拦截
     *
     * @param js2AppInfo
     * @return true 拦截，false，不拦截
     */
    boolean intercept(Js2AppInfo js2AppInfo);
}
