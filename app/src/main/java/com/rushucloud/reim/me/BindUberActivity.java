package com.rushucloud.reim.me;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.rushucloud.reim.R;
import com.rushucloud.reim.item.DidiExpenseActivity;
import com.umeng.analytics.MobclickAgent;

import java.io.Serializable;

import classes.model.User;
import classes.utils.AppPreference;
import classes.utils.DBManager;
import classes.utils.LogUtils;
import classes.utils.PhoneUtils;
import classes.utils.Utils;
import classes.utils.ViewUtils;
import classes.widget.ClearEditText;
import classes.widget.ReimProgressDialog;
import netUtils.common.HttpConnectionCallback;
import netUtils.common.URLDef;
import netUtils.request.item.DidiSignInRequest;
import netUtils.request.item.DidiVerifyCodeRequest;
import netUtils.request.user.BindDidiRequest;
import netUtils.response.item.DidiSignInResponse;
import netUtils.response.item.DidiVerifyCodeResponse;
import netUtils.response.user.BindDidiResponse;

public class BindUberActivity extends Activity
{
    // Widgets
    private ProgressBar progressBar;
    private WebView uberWebView;

    // Local Data
    private User currentUser;

    // View
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_me_bind_uber);
        initData();
        initView();
    }

    protected void onResume()
    {
        super.onResume();
        MobclickAgent.onPageStart("BindUberActivity");
        MobclickAgent.onResume(this);
        ReimProgressDialog.setContext(this);
        uberWebView.loadUrl(URLDef.URL_UBER_AUTH);
    }

    protected void onPause()
    {
        super.onPause();
        MobclickAgent.onPageEnd("BindUberActivity");
        MobclickAgent.onPause(this);
    }

    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_BACK)
        {
            goBack();
        }
        return super.onKeyDown(keyCode, event);
    }

    private void initView()
    {
        ImageView backImageView = (ImageView) findViewById(R.id.backImageView);
        backImageView.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                goBack();
            }
        });

        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        uberWebView = (WebView) findViewById(R.id.uberWebView);
        uberWebView.loadDataWithBaseURL(null, "", "text/html", "utf-8", null);
        uberWebView.getSettings().setJavaScriptEnabled(true);
        uberWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        uberWebView.setWebViewClient(new WebViewClient()
        {
            public boolean shouldOverrideUrlLoading(WebView view, String url)
            {
                LogUtils.tempPrint(url);
                view.loadUrl(url);
                return true;
            }
        });
        uberWebView.setWebChromeClient(new WebChromeClient()
        {
            public void onProgressChanged(WebView view, int newProgress)
            {
                if (newProgress == 100)
                {
                    progressBar.setVisibility(View.INVISIBLE);
                }
                else
                {
                    if (progressBar.getVisibility() == View.INVISIBLE)
                    {
                        progressBar.setVisibility(View.VISIBLE);
                    }
                    progressBar.setProgress(newProgress);
                }
                super.onProgressChanged(view, newProgress);
            }
        });
    }

    private void goBack()
    {
        ViewUtils.goBack(this);
    }

    // Data
    private void initData()
    {
        currentUser = AppPreference.getAppPreference().getCurrentUser();
    }

    // Network
}