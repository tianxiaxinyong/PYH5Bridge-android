package com.pycredit.h5sdk.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.pycredit.h5sdk.H5SDKHelper;
import com.pycredit.h5sdk.R;
import com.pycredit.h5sdk.impl.BannerCallback;

/**
 * @author huangx
 * @date 2018/2/28
 */

public class DefaultWebFragment extends Fragment {

    public static final String EXTRA_URL = "extra_url";

    protected WebView webView;
    protected H5SDKHelper h5SDKHelper;
    protected String url;
    protected String bannerUrl;
    protected BannerCallback bannerCallback;

    public static DefaultWebFragment newInstance(String url) {
        Bundle args = new Bundle();
        args.putString(EXTRA_URL, url);
        DefaultWebFragment fragment = new DefaultWebFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public void setBanner(String bannerImageUrl, BannerCallback callback) {
        this.bannerUrl = bannerImageUrl;
        this.bannerCallback = callback;
        if (h5SDKHelper != null) {
            h5SDKHelper.setBanner(bannerImageUrl, callback);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            url = getArguments().getString(EXTRA_URL);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_default_web, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        webView = (WebView) view.findViewById(R.id.webView);
        h5SDKHelper = new H5SDKHelper(this, webView);
        h5SDKHelper.initDefaultSettings();
        h5SDKHelper.setBanner(bannerUrl, bannerCallback);
        webView.loadUrl(url);
    }

    @Override
    public void onResume() {
        super.onResume();
        h5SDKHelper.onResume();
    }

    @Override
    public void onPause() {
        h5SDKHelper.onPause();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        h5SDKHelper.onDestroy();
        super.onDestroy();
    }

    public boolean onBackPressed() {
        if (h5SDKHelper != null) {
            return h5SDKHelper.onBackPressed();
        }
        return false;
    }
}
