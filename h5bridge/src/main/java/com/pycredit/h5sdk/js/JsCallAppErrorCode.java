package com.pycredit.h5sdk.js;

/**
 * Created by huangx on 2017/2/16.
 */

public enum JsCallAppErrorCode {
    SUCCESS("0", "操作成功"),
    ERROR_NO_CAMERA_PERM("error_1001", "拍照权限不足，请检查"),
    ERROR_CAMERA_USER_CANCEL("error_1002", "用户取消拍照"),
    ERROR_IMAGE_HANDLE("error_1003", "图片处理失败"),
    ERROR_UPLOAD_FAIL("error_2001", "网络错误，上传失败"),
    ERROR_PAY_APP_NOT_INSTALL("error_3001", "用户没有安装支付 App"),
    ERROR_PAY_USER_CANCEL("error_3002", "用户取消打开支付 App"),
    ERROR_PREVIEW_FAIL("error_4001", "预览失败"),
    ERROR_NO_BANNER_URL("error_5001", "没有广告 banner"),
    ERROR_NO_BANNER("error_6001", "没有广告");

    private String code;
    private String msg;

    JsCallAppErrorCode(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
