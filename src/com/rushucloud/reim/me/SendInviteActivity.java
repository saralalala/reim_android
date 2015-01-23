package com.rushucloud.reim.me;

import netUtils.HttpConnectionCallback;
import netUtils.Response.CommonResponse;
import netUtils.Response.User.InviteResponse;
import netUtils.Request.CommonRequest;
import netUtils.Request.User.InviteRequest;

import com.rushucloud.reim.R;
import com.umeng.analytics.MobclickAgent;

import classes.User;
import classes.utils.AppPreference;
import classes.utils.DBManager;
import classes.utils.PhoneUtils;
import classes.utils.Utils;
import classes.utils.ViewUtils;
import classes.widget.ReimProgressDialog;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class SendInviteActivity extends Activity
{	
	private EditText usernameEditText;
	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_me_send_invite);
		initView();
	}

	protected void onResume()
	{
		super.onResume();
		MobclickAgent.onPageStart("SendInviteActivity");		
		MobclickAgent.onResume(this);
		ReimProgressDialog.setProgressDialog(this);
	}

	protected void onPause()
	{
		super.onPause();
		MobclickAgent.onPageEnd("SendInviteActivity");
		MobclickAgent.onPause(this);
	}
	
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if (keyCode == KeyEvent.KEYCODE_BACK)
		{
	    	finish();
		}
		return super.onKeyDown(keyCode, event);
	}
	
	private void initView()
	{		
		getActionBar().hide();
		
		ImageView backImageView = (ImageView) findViewById(R.id.backImageView);
		backImageView.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				finish();
			}
		});
		
		usernameEditText = (EditText)findViewById(R.id.usernameEditText);
		usernameEditText.setOnFocusChangeListener(ViewUtils.onFocusChangeListener);

        Button inviteButton = (Button) findViewById(R.id.inviteButton);
        inviteButton.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				MobclickAgent.onEvent(SendInviteActivity.this, "UMENG_MINE_INVITE");

				String username = usernameEditText.getText().toString();
				if (!PhoneUtils.isNetworkConnected())
				{
					ViewUtils.showToast(SendInviteActivity.this, R.string.error_send_invite_network_unavailable);			
				}
				if (username.equals(""))
				{
					ViewUtils.showToast(SendInviteActivity.this, R.string.error_email_or_phone_empty);
				}
				else if (!Utils.isEmailOrPhone(username))
				{
					ViewUtils.showToast(SendInviteActivity.this, R.string.error_email_or_phone_wrong_format);
				}
				else
				{
					hideSoftKeyboard();
					sendInviteRequest(username);
				}
			}
		});
        inviteButton = ViewUtils.resizeLongButton(inviteButton);
        
        LinearLayout baseLayout = (LinearLayout)findViewById(R.id.baseLayout);
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
					sendCommonRequest();
				}
				else
				{
					runOnUiThread(new Runnable()
					{
						public void run()
						{
							ReimProgressDialog.dismiss();
							ViewUtils.showToast(SendInviteActivity.this, R.string.failed_to_send_invite, response.getErrorMessage());
						}
					});
				}
			}
		});
    }
    
    private void sendCommonRequest()
    {
    	CommonRequest request = new CommonRequest();
    	request.sendRequest(new HttpConnectionCallback()
		{
			public void execute(Object httpResponse)
			{
				final CommonResponse response = new CommonResponse(httpResponse);
				if (response.getStatus())
				{
					int currentGroupID = response.getGroup().getServerID();

					// update AppPreference
					AppPreference appPreference = AppPreference.getAppPreference();
					appPreference.setCurrentGroupID(currentGroupID);
					appPreference.saveAppPreference();

					// update members
					DBManager dbManager = DBManager.getDBManager();
					User currentUser = response.getCurrentUser();
					User localUser = dbManager.getUser(response.getCurrentUser().getServerID());
					if (localUser != null && currentUser.getAvatarID() == localUser.getAvatarID())
					{
						currentUser.setAvatarPath(localUser.getAvatarPath());
					}
					
					dbManager.updateGroupUsers(response.getMemberList(), currentGroupID);

					dbManager.syncUser(currentUser);

					// update categories
					dbManager.updateGroupCategories(response.getCategoryList(), currentGroupID);

					// update tags
					dbManager.updateGroupTags(response.getTagList(), currentGroupID);

					// update group info
					dbManager.syncGroup(response.getGroup());
					
					runOnUiThread(new Runnable()
					{
						public void run()
						{
							ReimProgressDialog.dismiss();
							ViewUtils.showToast(SendInviteActivity.this, R.string.succeed_in_sending_invite);
							finish();
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
							ViewUtils.showToast(SendInviteActivity.this, R.string.failed_to_send_invite, response.getErrorMessage());
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
}