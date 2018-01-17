package com.pycredit.h5sdk.perm;

import android.content.Intent;

/**
 * 监听onActivityResult，传递方向：根结点-->子结点
 * <p>
 * Created by huangx on 2016/7/1.
 */
public interface OnActivityResultDelegate {
    void onActivityResult(int requestCode, int resultCode, Intent data);
}
