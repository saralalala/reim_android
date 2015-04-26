package com.rushucloud.reim.me;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.rushucloud.reim.R;
import com.umeng.analytics.MobclickAgent;

import classes.utils.PhoneUtils;
import classes.utils.Utils;
import classes.utils.ViewUtils;
import classes.widget.ClearEditText;
import classes.widget.ReimProgressDialog;
import netUtils.HttpConnectionCallback;
import netUtils.request.user.InviteRequest;
import netUtils.response.user.InviteResponse;

public class InviteActivity extends Activity
{	
	private ClearEditText usernameEditText;
	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_me_invite);
		initView();
	}

	protected void onResume()
	{
		super.onResume();
		MobclickAgent.onPageStart("InviteActivity");
		MobclickAgent.onResume(this);
		ReimProgressDialog.setContext(this);
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
		
		usernameEditText = (ClearEditText) findViewById(R.id.usernameEditText);
        ViewUtils.requestFocus(this, usernameEditText);

        Button inviteButton = (Button) findViewById(R.id.inviteButton);
        inviteButton.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				MobclickAgent.onEvent(InviteActivity.this, "UMENG_MINE_INVITE");

				String username = usernameEditText.getText().toString();
				if (!PhoneUtils.isNetworkConnected())
				{
					ViewUtils.showToast(InviteActivity.this, R.string.error_send_invite_network_unavailable);
				}
				if (username.isEmpty())
				{
					ViewUtils.showToast(InviteActivity.this, R.string.error_email_or_phone_empty);
				}
				else if (!Utils.isEmailOrPhone(username))
				{
					ViewUtils.showToast(InviteActivity.this, R.string.error_email_or_phone_wrong_format);
				}
				else
				{
					hideSoftKeyboard();
					sendInviteRequest(username);
				}
			}
		});
        
        LinearLayout baseLayout = (LinearLayout) findViewById(R.id.baseLayout);
		baseLayout.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				hideSoftKeyboard();
			}
		});
	}
	
    private void sendInviteRequest(String username)
    {
		ReimProgressDialog.show();
    	InviteRequest request = new InviteRequest(username);
    	request.sendRequest(new HttpConnectionCallback()
		{
			public void execute(Object httpResponse)
			{
				final InviteResponse response = new InviteResponse(httpResponse);
				if (response.getStatus())
				{
                    runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            ReimProgressDialog.dismiss();
                            ViewUtils.showToast(InviteActivity.this, R.string.succeed_in_sending_invite);
                            goBack();
                        }
                    });
				}
				else
				{
					runOnUiThread(new Runnable()
					{
						public void run()
						{
							ReimProgressDialog.dismiss();
							ViewUtils.showToast(InviteActivity.this, R.string.failed_to_send_invite, response.getErrorMessage());
						}
					});
				}
			}
		});
    }
	
	private void hideSoftKeyboard()
	{
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(usernameEditText.getWindowToken(), 0);
	}

    private void goBack()
    {
        hideSoftKeyboard();
        ViewUtils.goBack(this);
    }
}