package com.rushucloud.reim.report;

import java.io.Serializable;
import java.util.List;

import netUtils.HttpConnectionCallback;
import netUtils.Response.Report.ModifyReportResponse;
import netUtils.Request.Report.ModifyReportRequest;
import classes.ReimApplication;
import classes.Report;
import classes.User;
import classes.utils.DBManager;
import classes.utils.Utils;
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
	
	private DBManager dbManager;
	
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
		dbManager = DBManager.getDBManager();
		report = (Report) getIntent().getExtras().getSerializable("report");
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
				Intent intent = new Intent(FollowingActivity.this, PickManagerActivity.class);
				intent.putExtra("managers", (Serializable) report.getManagerList());
				startActivityForResult(intent, PICK_MANAGER);	
			}
		});
		
		ccTextView = (TextView) findViewById(R.id.ccTextView);
		ccTextView.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				Intent intent = new Intent(FollowingActivity.this, PickCCActivity.class);
				intent.putExtra("ccs", (Serializable) report.getCCList());
				startActivityForResult(intent, PICK_CC);	
			}
		});
		
		Button finishButton = (Button)findViewById(R.id.finishButton);
		finishButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
			
			}
		});	

		Button submitButton = (Button) findViewById(R.id.submitButton);
		submitButton.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				sendModifyReportRequest(Report.STATUS_DRAFT);
			}
		});
	}
  
    private void sendModifyReportRequest(final int originalStatus)
    {
    	ModifyReportRequest request = new ModifyReportRequest(report);
    	request.sendRequest(new HttpConnectionCallback()
		{
			public void execute(Object httpResponse)
			{
				final ModifyReportResponse response = new ModifyReportResponse(httpResponse);
				if (response.getStatus())
				{
					int currentTime = Utils.getCurrentTime();
					
					report.setServerUpdatedDate(currentTime);
					report.setLocalUpdatedDate(currentTime);
					dbManager.updateReportByLocalID(report);
										
					runOnUiThread(new Runnable()
					{
						public void run()
						{
							ReimProgressDialog.dismiss();
							ViewUtils.showToast(FollowingActivity.this, R.string.succeed_in_submitting_report);
							finish();
						}
					});
				}
				else
				{
					report.setStatus(originalStatus);
					dbManager.updateReportByLocalID(report);
					
					runOnUiThread(new Runnable()
					{
						public void run()
						{
							ReimProgressDialog.dismiss();
							ViewUtils.showToast(FollowingActivity.this, R.string.failed_to_submit_report, response.getErrorMessage());
						}
					});					
				}
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