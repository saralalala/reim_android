package com.rushucloud.reim.report;

import java.io.Serializable;
import java.util.List;

import netUtils.HttpConnectionCallback;
import netUtils.Response.Report.ApproveReportResponse;
import netUtils.Request.Report.ApproveReportRequest;
import classes.ReimApplication;
import classes.Report;
import classes.User;
import classes.utils.AppPreference;
import classes.utils.ViewUtils;
import classes.widget.ReimProgressDialog;

import com.rushucloud.reim.R;
import com.umeng.analytics.MobclickAgent;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class FollowingActivity extends Activity
{
	private static final int PICK_MANAGER = 0;
	private static final int PICK_CC = 1;
	
	private TextView managerTextView;	
	private TextView ccTextView;
	
	private Report report;
	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_report_following);
		initData();
		initView();
	}
	
	protected void onResume()
	{
		super.onResume();
		MobclickAgent.onPageStart("FollowingActivity");		
		MobclickAgent.onResume(this);
		ReimProgressDialog.setProgressDialog(this);
	}

	protected void onPause()
	{
		super.onPause();
		MobclickAgent.onPageEnd("FollowingActivity");
		MobclickAgent.onPause(this);
	}
	
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if (keyCode == KeyEvent.KEYCODE_BACK)
		{
			goBackToMainActivity();
		}
		return super.onKeyDown(keyCode, event);
	}
	
	@SuppressWarnings("unchecked")
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (resultCode == RESULT_OK)
		{
			switch (requestCode)
			{
				case PICK_MANAGER:
				{
					List<User> managerList = (List<User>) data.getSerializableExtra("managers");
					report.setManagerList(managerList);
					managerTextView.setText(report.getManagersName());
					break;
				}
				case PICK_CC:
				{
					List<User> ccList = (List<User>) data.getSerializableExtra("ccs");
					report.setCCList(ccList);
					ccTextView.setText(report.getCCsName());
					break;
				}
				default:
					break;
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private void initData()
	{
		report = (Report) getIntent().getSerializableExtra("report");
		report.setManagerList(AppPreference.getAppPreference().getCurrentUser().constructListWithManager());
	}
	
	private void initView()
	{
		getActionBar().hide();
		
		ImageView backImageView = (ImageView) findViewById(R.id.backImageView);
		backImageView.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				goBackToMainActivity();
			}	
		});

		managerTextView = (TextView) findViewById(R.id.managerTextView);
		managerTextView.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				MobclickAgent.onEvent(FollowingActivity.this, "UMENG_REPORT_NEXT_SEND");
				Intent intent = new Intent(FollowingActivity.this, PickManagerActivity.class);
				intent.putExtra("managers", (Serializable) report.getManagerList());
				intent.putExtra("sender", report.getSender().getServerID());
				intent.putExtra("fromFollowing", true);
				startActivityForResult(intent, PICK_MANAGER);
			}
		});
		managerTextView.setText(report.getManagersName());
		
		ccTextView = (TextView) findViewById(R.id.ccTextView);
		ccTextView.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				MobclickAgent.onEvent(FollowingActivity.this, "UMENG_REPORT_NEXT_CC");
				Intent intent = new Intent(FollowingActivity.this, PickCCActivity.class);
				intent.putExtra("ccs", (Serializable) report.getCCList());
				intent.putExtra("sender", report.getSender().getServerID());
				intent.putExtra("fromFollowing", true);
				startActivityForResult(intent, PICK_CC);	
			}
		});
		
		Button finishButton = (Button) findViewById(R.id.finishButton);
		finishButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				goBackToMainActivity();
			}
		});	

		Button submitButton = (Button) findViewById(R.id.submitButton);
		submitButton.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				sendChooseFollowingReportRequest();
			}
		});
	}
  
    private void sendChooseFollowingReportRequest()
    {
    	ReimProgressDialog.show();
    	ApproveReportRequest request = new ApproveReportRequest(report);
    	request.sendRequest(new HttpConnectionCallback()
		{
			public void execute(Object httpResponse)
			{
				final ApproveReportResponse response = new ApproveReportResponse(httpResponse);
				runOnUiThread(new Runnable()
				{
					public void run()
					{
						if (response.getStatus())
						{
							ReimProgressDialog.dismiss();
							ViewUtils.showToast(FollowingActivity.this, R.string.succeed_in_choosing_following);
							goBackToMainActivity();
						}
						else
						{
							ViewUtils.showToast(FollowingActivity.this, R.string.error_choose_following, response.getErrorMessage());
						}
					}
				});
			}
		});
    }
 
    private void goBackToMainActivity()
    {
    	ReimApplication.setTabIndex(1);
    	ReimApplication.setReportTabIndex(0);
		finish();
    }
}