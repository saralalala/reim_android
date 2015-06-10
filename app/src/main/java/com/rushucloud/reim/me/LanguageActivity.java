package com.rushucloud.reim.me;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.rushucloud.reim.main.MainActivity;
import com.rushucloud.reim.R;
import com.umeng.analytics.MobclickAgent;

import java.util.Locale;

import classes.utils.AppPreference;
import classes.utils.ViewUtils;
import classes.widget.ReimProgressDialog;

public class LanguageActivity extends Activity
{
    private ImageView chineseImageView;
    private ImageView englishImageView;

    private boolean hasAction = false;

    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_me_language);
        initView();
    }

    protected void onResume()
    {
        super.onResume();
        MobclickAgent.onPageStart("LanguageActivity");
        MobclickAgent.onResume(this);
        ReimProgressDialog.setContext(this);
    }

    protected void onPause()
    {
        super.onPause();
        MobclickAgent.onPageEnd("LanguageActivity");
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

        TextView confirmTextView = (TextView) findViewById(R.id.confirmTextView);
        confirmTextView.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                Configuration config = getResources().getConfiguration();
                config.locale = chineseImageView.getVisibility() == View.VISIBLE? Locale.CHINESE : Locale.ENGLISH;
                getResources().updateConfiguration(config, getResources().getDisplayMetrics());

                if (hasAction)
                {
                    String language = chineseImageView.getVisibility() == View.VISIBLE? "zh" : "en";
                    AppPreference appPreference = AppPreference.getAppPreference();
                    appPreference.setLanguage(language);
                    appPreference.saveAppPreference();
                }

                Intent intent = new Intent(LanguageActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                ViewUtils.goBackWithIntent(LanguageActivity.this, intent);
            }
        });

        chineseImageView = (ImageView) findViewById(R.id.chineseImageView);
        englishImageView = (ImageView) findViewById(R.id.englishImageView);

        LinearLayout chineseLayout = (LinearLayout) findViewById(R.id.chineseLayout);
        chineseLayout.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                hasAction = true;
                chineseImageView.setVisibility(View.VISIBLE);
                englishImageView.setVisibility(View.INVISIBLE);
            }
        });

        LinearLayout englishLayout = (LinearLayout) findViewById(R.id.englishLayout);
        englishLayout.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                hasAction = true;
                chineseImageView.setVisibility(View.INVISIBLE);
                englishImageView.setVisibility(View.VISIBLE);
            }
        });

        if (Locale.getDefault().getLanguage().equals("zh"))
        {
            chineseImageView.setVisibility(View.VISIBLE);
            englishImageView.setVisibility(View.INVISIBLE);
        }
        else
        {
            chineseImageView.setVisibility(View.INVISIBLE);
            englishImageView.setVisibility(View.VISIBLE);
        }
    }

    private void goBack()
    {
        ViewUtils.goBack(this);
    }
}