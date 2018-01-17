package com.pycredit.h5sdk.js;


import android.util.Log;

/**
 * Created by huangx on 2017/2/28.
 */

public abstract class JsCallAppRunner<T extends JsCallAppProcess> implements Runnable {
    protected T process;
    protected String params;
    protected JsCallAppCallback callback;

    public JsCallAppRunner(T process, String params, JsCallAppCallback callback) {
        this.process = process;
        this.params = params;
        this.callback = callback;
        Log.d("JsCallAppRunner", "jsCallApp:" + params);
    }
}
