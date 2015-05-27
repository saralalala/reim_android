package com.rushucloud.reim.guide;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.rushucloud.reim.R;
import com.rushucloud.reim.start.SignInActivity;
import com.umeng.analytics.MobclickAgent;

import java.io.Serializable;
import java.util.ArrayList;

import classes.model.User;
import classes.utils.AppPreference;
import classes.utils.Utils;
import classes.utils.ViewUtils;

public class GuideStartActivity extends Activity
{
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide_start);
        initView();
    }

    protected void onResume()
    {
        super.onResume();
        MobclickAgent.onPageStart("GuideStartActivity");
        MobclickAgent.onResume(this);
    }

    protected void onPause()
    {
        super.onPause();
        MobclickAgent.onPageEnd("GuideStartActivity");
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
            public void onClick(View view)
            {
                goBack();
            }
        });

        Button joinButton = (Button) findViewById(R.id.joinButton);
        joinButton.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                Intent intent = new Intent(GuideStartActivity.this, SetNicknameActivity.class);
                intent.putExtra("nickname", "");
                intent.putExtra("group", (Serializable) null);
                intent.putExtra("join", true);
                ViewUtils.goForwardAndFinish(GuideStartActivity.this, intent);
            }
        });

        TextView createTextView = (TextView) findViewById(R.id.createTextView);
        createTextView.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG | Paint.FAKE_BOLD_TEXT_FLAG);
        createTextView.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                Bundle bundle = new Bundle();
                bundle.putString("nickname", "");
                bundle.putString("companyName", "");
                bundle.putStringArrayList("inputList", new ArrayList<String>());
                bundle.putStringArrayList("inputChosenList", new ArrayList<String>());
                bundle.putSerializable("contactChosenList", new ArrayList<User>());
                Intent intent = new Intent(GuideStartActivity.this, SetNicknameActivity.class);
                intent.putExtras(bundle);
                ViewUtils.goForwardAndFinish(GuideStartActivity.this, intent);
            }
        });
    }

    private void goBack()
    {
        Intent intent = new Intent(GuideStartActivity.this, SignInActivity.class);
        if (Utils.isEmailOrPhone(AppPreference.getAppPreference().getUsername()))
        {
            intent.putExtra("username", AppPreference.getAppPreference().getUsername());
            intent.putExtra("password", AppPreference.getAppPreference().getPassword());
        }
        ViewUtils.goBackWithIntent(GuideStartActivity.this, intent);
    }
}