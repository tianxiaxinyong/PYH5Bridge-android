package com.pycredit.h5sdk.impl;


import com.pycredit.h5sdk.js.Js2AppInfo;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * JS调用APP时所传对象
 * Created by huangx on 2017/2/14.
 */

public class PYCreditJs2AppInfo extends Js2AppInfo {
    private static final String JS_ARGS_FLAG = "args";

    private static final String JS_SUCCESS_CALLBACK_FLAG = "success";

    private static final String JS_ERROR_CALLBACK_FLAG = "error";

    private static final String JS_PROGRESS_CALLBACK_FLAG = "progress";

    private static final String JS_ARGS_DATA_FLAG = "data";

    private String successName = "";

    private String errorName = "";

    private String progressName = "";

    private String dataParam = "";

    private String requestParam = "";

    private JSONObject dataObj = null;

    private JSONArray dataArray = null;

    public PYCreditJs2AppInfo(String params) {
        try {
            requestParam = params;
            JSONObject callbackInfo = new JSONObject(requestParam);
            JSONObject argsObj = callbackInfo.optJSONObject(JS_ARGS_FLAG);
            if (argsObj != null && argsObj.length() > 0) {
                if (argsObj.opt(JS_ARGS_DATA_FLAG) instanceof JSONObject) {
                    dataObj = argsObj.optJSONObject(JS_ARGS_DATA_FLAG);
                    if (dataObj != null) {
                        progressName = dataObj.optString(JS_PROGRESS_CALLBACK_FLAG);
                    }
                } else if (argsObj.opt(JS_ARGS_DATA_FLAG) instanceof JSONArray) {
                    dataArray = argsObj.optJSONArray(JS_ARGS_DATA_FLAG);
                }
            }
            successName = callbackInfo.optString(JS_SUCCESS_CALLBACK_FLAG);
            errorName = callbackInfo.optString(JS_ERROR_CALLBACK_FLAG);
            dataParam = dataObj != null ? dataObj.toString() : dataArray != null ? dataArray.toString() : "";
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getSuccessName() {
        return successName;
    }

    public String getErrorName() {
        return errorName;
    }

    public String getProgressName() {
        return progressName;
    }

    public String getDataParam() {
        return dataParam;
    }

    public String getRequestParam() {
        return requestParam;
    }

    public JSONObject getDataObj() {
        return dataObj;
    }

    public JSONArray getDataArray() {
        return dataArray;
    }
}
