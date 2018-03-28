package com.pycredit.h5sdk.impl;


import android.text.TextUtils;

import com.pycredit.h5sdk.js.JsParser;
import com.pycredit.h5sdk.utils.JsonUtils;

import java.text.MessageFormat;
import java.util.Map;

/**
 * Created by huangx on 2017/2/17.
 */

public class PYCreditJsParser implements JsParser<PYCreditJs2AppInfo, PYCreditApp2JsInfo> {

    public static final String JSCODE_FORMAT = "window[\"{0}\"] && {1}({2})";

    /**
     * 将String解析成Js2AppInfo
     *
     * @param params
     * @return
     */
    @Override
    public PYCreditJs2AppInfo decode(String params) {
        return new PYCreditJs2AppInfo(params);
    }

    /**
     * 将App2JsInfo封装成String
     *
     * @param success
     * @param js2AppInfo
     * @param app2JsInfo
     * @return
     */
    @Override
    public String encode(boolean success, PYCreditJs2AppInfo js2AppInfo, PYCreditApp2JsInfo app2JsInfo) {
        String callbackName = success ? js2AppInfo.getSuccessName() : js2AppInfo.getErrorName();
        return encode(callbackName, js2AppInfo, app2JsInfo);
    }

    /**
     * 将App2JsInfo封装成String
     *
     * @param type
     * @param js2AppInfo
     * @param app2JsInfo
     * @return
     */
    @Override
    public String encode(int type, PYCreditJs2AppInfo js2AppInfo, PYCreditApp2JsInfo app2JsInfo) {
        String callbackName = null;
        if (type == TYPE_PROGRESS) {
            callbackName = js2AppInfo.getProgressName();
        }
        if (!TextUtils.isEmpty(callbackName)) {
            return encode(callbackName, js2AppInfo, app2JsInfo);
        }
        return null;
    }

    public String encode(String callbackName, PYCreditJs2AppInfo js2AppInfo, PYCreditApp2JsInfo app2JsInfo) {
        String resultData = "";
        if (app2JsInfo.getData() != null) {
            if (app2JsInfo.getData() instanceof Map) {
                resultData = JsonUtils.toJsonString(app2JsInfo.getData());
            } else if (app2JsInfo.getData() instanceof String) {
                resultData = (String) app2JsInfo.getData();
            }
        }
        String jsCode = MessageFormat.format(JSCODE_FORMAT, callbackName, callbackName,
                resultData);
        return jsCode;
    }
}
