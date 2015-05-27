package com.rushucloud.reim.me;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.rushucloud.reim.R;
import com.umeng.analytics.MobclickAgent;

import classes.utils.PhoneUtils;
import classes.utils.ViewUtils;

public class AboutActivity extends Activity
{
    private long showTime;
    private int showCount;
    private PopupWindow surprisePopupWindow;

    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_me_about);
        initView();
    }

    protected void onResume()
    {
        super.onResume();
        MobclickAgent.onPageStart("AboutActivity");
        MobclickAgent.onResume(this);
    }

    protected void onPause()
    {
        super.onPause();
        MobclickAgent.onPageEnd("AboutActivity");
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

        TextView currentVersionTextView = (TextView) findViewById(R.id.currentVersionTextView);
        String versionPrompt = currentVersionTextView.getText() + PhoneUtils.getAppVersion();
        currentVersionTextView.setText(versionPrompt);
        currentVersionTextView.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                if (System.currentTimeMillis() - showTime > 2000)
                {
                    showCount = 3;
                }
                else if (showCount > 0)
                {
                    showCount--;
                }
                else
                {
                    showSurpriseWindow();
                    showCount = 3;
                }
                showTime = System.currentTimeMillis();
            }
        });

        initSurpriseWindow();
    }

    private void initSurpriseWindow()
    {
        View surpriseView = View.inflate(this, R.layout.window_surprise, null);

        Button okButton = (Button) surpriseView.findViewById(R.id.okButton);
        okButton.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                surprisePopupWindow.dismiss();
            }
        });

        surprisePopupWindow = ViewUtils.buildSurprisePopupWindow(this, surpriseView);
    }

    private void showSurpriseWindow()
    {
        surprisePopupWindow.showAtLocation(findViewById(R.id.containerLayout), Gravity.CENTER, 0, 0);
        surprisePopupWindow.update();
        ViewUtils.dimBackground(this);
    }

    private void goBack()
    {
        ViewUtils.goBack(this);
    }
}
