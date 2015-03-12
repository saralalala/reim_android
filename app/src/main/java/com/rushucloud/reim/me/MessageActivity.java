package com.rushucloud.reim.me;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.rushucloud.reim.MainActivity;
import com.rushucloud.reim.R;
import com.umeng.analytics.MobclickAgent;

import classes.Invite;
import classes.ReimApplication;
import classes.User;
import classes.utils.AppPreference;
import classes.utils.DBManager;
import classes.utils.PhoneUtils;
import classes.utils.Utils;
import classes.utils.ViewUtils;
import classes.widget.ReimProgressDialog;
import netUtils.HttpConnectionCallback;
import netUtils.NetworkConstant;
import netUtils.Request.User.InviteReplyRequest;
import netUtils.Response.User.InviteReplyResponse;

public class MessageActivity extends Activity
{	
	private Invite invite;
	
	private boolean fromPush;
	
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
		MobclickAgent.onPageStart("MessageActivity");
		MobclickAgent.onResume(this);
		ReimProgressDialog.setProgressDialog(this);
	}

	protected void onPause()
	{
		super.onPause();
		MobclickAgent.onPageEnd("MessageActivity");
		MobclickAgent.onPause(this);
	}
	
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if (keyCode == KeyEvent.KEYCODE_BACK)
		{
			goBack();
		}
		return super.onKeyDown(keyCode, event);
	}
	
	private void initData()
	{
		Bundle bundle = getIntent().getExtras();
		if (bundle != null)
		{
			invite = (Invite) bundle.getSerializable("invite");
			fromPush = bundle.getBoolean("fromPush", false);
		}
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
		
		TextView inviteTextView = (TextView) findViewById(R.id.inviteTextView);
		inviteTextView.setText(invite.getMessage());

		TextView dateTextView = (TextView) findViewById(R.id.dateTextView);
		dateTextView.setText(Utils.secondToStringUpToDay(invite.getUpdateTime()));
		
		Button agreeButton = (Button) findViewById(R.id.agreeButton);
		agreeButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				if (PhoneUtils.isNetworkConnected())
				{
					sendInviteReplyRequest(Invite.TYPE_ACCEPTED, invite.getInviteCode());					
				}
				else
				{
					ViewUtils.showToast(MessageActivity.this, R.string.error_send_reply_network_unavailable);
				}
			}
		});
		
		Button rejectButton = (Button) findViewById(R.id.rejectButton);
		rejectButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				if (PhoneUtils.isNetworkConnected())
				{
					sendInviteReplyRequest(Invite.TYPE_REJECTED, invite.getInviteCode());				
				}
				else
				{
					ViewUtils.showToast(MessageActivity.this, R.string.error_send_reply_network_unavailable);
				}
			}
		});

        String currentNickname = AppPreference.getAppPreference().getCurrentUser().getNickname();
		if (invite.getTypeCode() != Invite.TYPE_NEW || invite.getInvitor().equals(currentNickname))
		{
			agreeButton.setVisibility(View.GONE);
			rejectButton.setVisibility(View.GONE);
		}
	}
	
    private void sendInviteReplyRequest(final int agree, String inviteCode)
    {
		ReimProgressDialog.show();
    	InviteReplyRequest request = new InviteReplyRequest(agree, inviteCode);
    	request.sendRequest(new HttpConnectionCallback()
		{
			public void execute(Object httpResponse)
			{
				final InviteReplyResponse response = new InviteReplyResponse(httpResponse);
				if (response.getStatus())
				{
					if (agree == Invite.TYPE_ACCEPTED)
					{
                        int currentUserID = response.getCurrentUser().getServerID();
                        int currentGroupID = -1;

                        DBManager dbManager = DBManager.getDBManager();
                        AppPreference appPreference = AppPreference.getAppPreference();
                        appPreference.setServerToken(response.getServerToken());
                        appPreference.setCurrentUserID(currentUserID);
                        appPreference.setSyncOnlyWithWifi(true);
                        appPreference.setEnablePasswordProtection(true);

                        if (response.getGroup() != null)
                        {
                            currentGroupID = response.getGroup().getServerID();

                            // update AppPreference
                            appPreference.setCurrentGroupID(currentGroupID);
                            appPreference.saveAppPreference();

                            // update members
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
                        }
                        else
                        {
                            // update AppPreference
                            appPreference.setCurrentGroupID(currentGroupID);
                            appPreference.saveAppPreference();

                            // update current user
                            dbManager.syncUser(response.getCurrentUser());

                            // update categories
                            dbManager.updateGroupCategories(response.getCategoryList(), currentGroupID);
                        }

                        runOnUiThread(new Runnable()
                        {
                            public void run()
                            {
                                ReimProgressDialog.dismiss();
                                ViewUtils.showToast(MessageActivity.this, R.string.prompt_invite_reply_sent);
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
                                ViewUtils.showToast(MessageActivity.this, R.string.prompt_invite_reply_sent);
                                goBack();
							}
						});
					}
				}
				else
				{
					runOnUiThread(new Runnable()
					{
						public void run()
						{
							ReimProgressDialog.dismiss();
					    	ViewUtils.showToast(MessageActivity.this, R.string.failed_to_send_reply, response.getErrorMessage());
                            if (response.getCode() == NetworkConstant.ERROR_INVITE_DONE)
                            {
                                goBack();
                            }
						}						
					});
				}
			}
		});
    }

    private void goBack()
    {
    	if (fromPush)
		{
        	ReimApplication.setTabIndex(3);
        	Intent intent = new Intent(MessageActivity.this, MainActivity.class);
    		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
    		Intent intent2 = new Intent(MessageActivity.this, MessageListActivity.class);
    		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        	startActivities(new Intent[] {intent, intent2});
        	finish();
		}
    	else
    	{
			finish();
		}
    }
}