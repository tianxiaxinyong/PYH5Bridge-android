package com.pycredit.h5sdk.perm;

import android.support.annotation.NonNull;

/**
 * Created by huangx on 2017/10/13.
 */

public interface OnRequestPermissionsResultDelegate {
    void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults);
}
