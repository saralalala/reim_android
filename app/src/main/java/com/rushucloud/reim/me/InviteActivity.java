package com.rushucloud.reim.me;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Base64;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.rushucloud.reim.R;
import com.rushucloud.reim.guide.InputContactActivity;
import com.umeng.analytics.MobclickAgent;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import classes.model.Group;
import classes.model.User;
import classes.utils.AppPreference;
import classes.utils.ViewUtils;
import classes.utils.WeChatUtils;
import netUtils.common.URLDef;

public class InviteActivity extends Activity
{
    // Local Data
    private String nickname = "";
    private String companyName = "";
    private String shareURL = "";

    // View
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_me_invite);
        initData();
        initView();
    }

    protected void onResume()
    {
        super.onResume();
        MobclickAgent.onPageStart("InviteActivity");
        MobclickAgent.onResume(this);
    }

    protected void onPause()
    {
        super.onPause();
        MobclickAgent.onPageEnd("InviteActivity");
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

        LinearLayout inputLayout = (LinearLayout) findViewById(R.id.inputLayout);
        inputLayout.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                Intent intent = new Intent(InviteActivity.this, InputContactActivity.class);
                intent.putExtra("fromMe", true);
                ViewUtils.goForward(InviteActivity.this, intent);
            }
        });

        LinearLayout wechatLayout = (LinearLayout) findViewById(R.id.wechatLayout);
        wechatLayout.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                if (!shareURL.isEmpty())
                {
                    String title = String.format(getString(R.string.wechat_share_title), nickname, companyName);
                    String description = String.format(getString(R.string.wechat_invite_description), nickname, companyName);
                    WeChatUtils.shareToWX(InviteActivity.this, shareURL, title, description, false);
                }
                else
                {
                    ViewUtils.showToast(InviteActivity.this, R.string.error_no_company_invite);
                }
            }
        });

        LinearLayout contactLayout = (LinearLayout) findViewById(R.id.contactLayout);
        contactLayout.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                ViewUtils.goForward(InviteActivity.this, ContactActivity.class);
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
        Group group = AppPreference.getAppPreference().getCurrentGroup();
        if (group != null)
        {
            User user = AppPreference.getAppPreference().getCurrentUser();
            nickname = user != null? user.getNickname() : "";
            companyName = group.getName();

            try
            {
                JSONObject jObject = new JSONObject();
                jObject.put("nickname", nickname);
                jObject.put("gid", AppPreference.getAppPreference().getCurrentGroupID());
                String params = Base64.encodeToString(jObject.toString().getBytes(), Base64.NO_WRAP);
                String redirectURI = URLEncoder.encode(URLDef.URL_SHARE_REDIRECT_URI_PREFIX + params, "UTF-8");
                shareURL = String.format(URLDef.URL_SHARE, redirectURI);
            }
            catch (JSONException | UnsupportedEncodingException e)
            {
                e.printStackTrace();
            }
        }
    }
}