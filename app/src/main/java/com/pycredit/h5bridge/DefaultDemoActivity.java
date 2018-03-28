package com.pycredit.h5bridge;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.pycredit.h5sdk.impl.BannerCallback;
import com.pycredit.h5sdk.ui.DefaultWebFragment;

/**
 * @author huangx
 * @date 2018/2/28
 */

public class DefaultDemoActivity extends AppCompatActivity {

    private DefaultWebFragment fragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_default_demo);
        fragment = DefaultWebFragment.newInstance("**使用鹏元提供的渠道**");
        fragment.setBanner("https://apk-txxy.oss-cn-shenzhen.aliyuncs.com/test_ad.png", new BannerCallback() {
            @Override
            public void onBannerClick() {
                Toast.makeText(getApplicationContext(), "广告被点击了", Toast.LENGTH_SHORT).show();
            }
        });
        getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment).commitAllowingStateLoss();
    }

    @Override
    public void onBackPressed() {
        if (fragment.onBackPressed()) {
            return;
        }
        super.onBackPressed();
    }
}
