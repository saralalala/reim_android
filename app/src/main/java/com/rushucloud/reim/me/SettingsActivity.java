package com.rushucloud.reim.me;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.rushucloud.reim.R;
import com.umeng.analytics.MobclickAgent;
import com.umeng.update.UmengUpdateAgent;
import com.umeng.update.UmengUpdateListener;
import com.umeng.update.UpdateResponse;
import com.umeng.update.UpdateStatus;

import classes.utils.ViewUtils;
import classes.widget.ReimProgressDialog;

public class SettingsActivity extends Activity
{
    // View
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_me_settings);
        initView();
    }

    protected void onResume()
    {
        super.onResume();
        MobclickAgent.onPageStart("SettingsActivity");
        MobclickAgent.onResume(this);
        ReimProgressDialog.setContext(this);
    }

    protected void onPause()
    {
        super.onPause();
        MobclickAgent.onPageEnd("SettingsActivity");
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

//        RelativeLayout languageLayout = (RelativeLayout) findViewById(R.id.languageLayout);
//        languageLayout.setOnClickListener(new View.OnClickListener()
//        {
//            public void onClick(View v)
//            {
//                ViewUtils.goForward(SettingsActivity.this, LanguageActivity.class);
//            }
//        });

        TextView updateTextView = (TextView) findViewById(R.id.updateTextView);
        updateTextView.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                ReimProgressDialog.show();
                UmengUpdateAgent.setUpdateAutoPopup(false);
                UmengUpdateAgent.setUpdateOnlyWifi(false);
                UmengUpdateAgent.setUpdateListener(new UmengUpdateListener()
                {
                    public void onUpdateReturned(int updateStatus, final UpdateResponse updateInfo)
                    {
                        ReimProgressDialog.dismiss();
                        switch (updateStatus)
                        {
                            case UpdateStatus.Yes:
                            {
                                UmengUpdateAgent.showUpdateDialog(SettingsActivity.this, updateInfo);
                                break;
                            }
                            case UpdateStatus.No:
                            {
                                ViewUtils.showToast(SettingsActivity.this, R.string.prompt_latest_version);
                                break;
                            }
                            case UpdateStatus.NoneWifi:
                            {
                                ViewUtils.showToast(SettingsActivity.this, R.string.error_no_wifi);
                                break;
                            }
                            case UpdateStatus.Timeout:
                            {
                                ViewUtils.showToast(SettingsActivity.this, R.string.error_timeout);
                                break;
                            }
                        }
                    }
                });
                UmengUpdateAgent.forceUpdate(SettingsActivity.this);
            }
        });

        TextView aboutTextView = (TextView) findViewById(R.id.aboutTextView);
        aboutTextView.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                MobclickAgent.onEvent(SettingsActivity.this, "UMENG_MINE_SETTING_ABOUT");
                ViewUtils.goForward(SettingsActivity.this, AboutActivity.class);
            }
        });
    }

    private void goBack()
    {
        ViewUtils.goBack(this);
    }
}