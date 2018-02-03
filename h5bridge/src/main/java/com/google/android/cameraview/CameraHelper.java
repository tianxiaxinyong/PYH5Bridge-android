package com.google.android.cameraview;

import android.media.CamcorderProfile;

/**
 * @author huangx
 * @date 2018/1/31
 */

public class CameraHelper {

    public static CamcorderProfile chooseOptimalCamcorderProfile(int cameraId, int width, int height) {
        int minHeightDiff = Integer.MAX_VALUE;
        int optimalQuality = -1;
        for (int i = 0; i < 9; i++) {
            if (CamcorderProfile.hasProfile(cameraId, i)) {
                CamcorderProfile profile = CamcorderProfile.get(cameraId, i);
                if (profile.videoFrameHeight < height) {
                    continue;
                }
                int heightDiff = Math.abs(profile.videoFrameHeight - height);
                if (heightDiff < minHeightDiff) {
                    minHeightDiff = heightDiff;
                    optimalQuality = i;
                }
            }
        }
        if (optimalQuality >= 0) {
            return CamcorderProfile.get(cameraId, optimalQuality);
        }
        return null;
    }
}
