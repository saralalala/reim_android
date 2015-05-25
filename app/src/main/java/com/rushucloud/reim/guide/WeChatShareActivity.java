package com.rushucloud.reim.guide;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.rushucloud.reim.R;
import com.umeng.analytics.MobclickAgent;

import classes.utils.AppPreference;
import classes.utils.ViewUtils;
import classes.utils.WeChatUtils;
import netUtils.URLDef;

public class WeChatShareActivity extends Activity
{
    private String nickname;
    private String companyName;
    private int count;

	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_guide_wechat);
        initData();
		initView();
	}

	protected void onResume()
	{
		super.onResume();
		MobclickAgent.onPageStart("WeChatShareActivity");
		MobclickAgent.onResume(this);
	}

	protected void onPause()
	{
		super.onPause();
		MobclickAgent.onPageEnd("WeChatShareActivity");
		MobclickAgent.onPause(this);
	}

    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event)
    {
        return keyCode != KeyEvent.KEYCODE_BACK && super.onKeyDown(keyCode, event);
    }

    private void initData()
    {
        nickname = AppPreference.getAppPreference().getCurrentUser().getNickname();
        companyName = getIntent().getStringExtra("companyName");
        count = getIntent().getIntExtra("count", 0);
    }

	private void initView()
	{
        TextView nextTextView = (TextView) findViewById(R.id.nextTextView);
        nextTextView.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                Intent intent = new Intent(WeChatShareActivity.this, CreateCompleteActivity.class);
                intent.putExtra("count", count);
                intent.putExtra("companyName", companyName);
                ViewUtils.goForwardAndFinish(WeChatShareActivity.this, intent);
            }
        });

        Button inviteButton = (Button) findViewById(R.id.inviteButton);
        inviteButton.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                String title = String.format(getString(R.string.wechat_share_title), nickname, companyName);
                String description = String.format(getString(R.string.wechat_invite_description), nickname, companyName);
                WeChatUtils.shareToWX(URLDef.URL_MAIN_PAGE, title, description, false);
            }
        });

        TextView copyTextView = (TextView) findViewById(R.id.copyTextView);
        copyTextView.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText(getString(R.string.app_name), URLDef.URL_MAIN_PAGE);
                clipboardManager.setPrimaryClip(clip);
                ViewUtils.showToast(WeChatShareActivity.this, R.string.prompt_copied_to_clipboard);
            }
        });
	}
}