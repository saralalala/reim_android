package com.rushucloud.reim.me;

import netUtils.HttpConnectionCallback;
import netUtils.Request.CommonRequest;
import netUtils.Request.User.InviteRequest;
import netUtils.Response.CommonResponse;
import netUtils.Response.User.InviteResponse;
import com.rushucloud.reim.R;
import com.umeng.analytics.MobclickAgent;

import classes.User;
import classes.Utils.AppPreference;
import classes.Utils.DBManager;
import classes.Utils.Utils;
import classes.Widget.ReimProgressDialog;
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
		usernameEditText.setOnFocusChangeListener(Utils.getEditTextFocusChangeListener());

        Button inviteButton = (Button) findViewById(R.id.inviteButton);
        inviteButton.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				MobclickAgent.onEvent(SendInviteActivity.this, "UMENG_MINE_INVITE");

				String username = usernameEditText.getText().toString();
				if (!Utils.isNetworkConnected())
				{
					Utils.showToast(SendInviteActivity.this, "网络未连接，无法发送邀请");			
				}
				if (username.equals(""))
				{
					Utils.showToast(SendInviteActivity.this, "手机号或邮箱不能为空");
				}
				else if (!Utils.isEmailOrPhone(username))
				{
					Utils.showToast(SendInviteActivity.this, "手机号或邮箱格式不正确");
				}
				else
				{
					hideSoftKeyboard();
					sendInviteRequest(username);
				}
			}
		});
        inviteButton = Utils.resizeLongButton(inviteButton);
        
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
							Utils.showToast(SendInviteActivity.this, "邀请发送失败，" + response.getErrorMessage());
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
							Utils.showToast(SendInviteActivity.this, "邀请已发送");
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
							Utils.showToast(SendInviteActivity.this, "邀请发送失败，" + response.getErrorMessage());
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