package com.pycredit.h5sdk.impl;


import com.pycredit.h5sdk.js.App2JsInfo;

/**
 * APP回应JS调用所传对象
 * Created by huangx on 2017/2/14.
 */

public class PYCreditApp2JsInfo<T> extends App2JsInfo {
    private T data;

    public PYCreditApp2JsInfo(T data) {
        this.data = data;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
