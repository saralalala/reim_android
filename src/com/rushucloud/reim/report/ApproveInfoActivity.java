package com.rushucloud.reim.report;

import netUtils.HttpConnectionCallback;
import netUtils.Request.Report.ApproveInfoRequest;
import netUtils.Response.Report.ApproveInfoResponse;

import com.rushucloud.reim.R;
import com.umeng.analytics.MobclickAgent;

import classes.utils.Utils;
import classes.widget.ReimProgressDialog;
import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

public class ApproveInfoActivity extends Activity
{	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_report_approve_info);
		initView();
	}

	protected void onResume()
	{
		super.onResume();
		MobclickAgent.onPageStart("ApproveInfoActivity");		
		MobclickAgent.onResume(this);
		ReimProgressDialog.setProgressDialog(this);
		initData();
	}

	protected void onPause()
	{
		super.onPause();
		MobclickAgent.onPageEnd("ApproveInfoActivity");
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
	
	private void initData()
	{
		int reportServerID = getIntent().getIntExtra("reportServerID", -1);
		if (reportServerID == -1)
		{
			Utils.showToast(this, R.string.failed_to_get_data);
		}
		else 
		{
			sendGetApproveInfoRequest(reportServerID);
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
	}
	
	private void sendGetApproveInfoRequest(int reportServerID)
	{
		ReimProgressDialog.show();
		ApproveInfoRequest request = new ApproveInfoRequest(reportServerID);
		request.sendRequest(new HttpConnectionCallback()
		{
			public void execute(Object httpResponse)
			{
				final ApproveInfoResponse response = new ApproveInfoResponse(httpResponse);
				if (response.getStatus())
				{
					runOnUiThread(new Runnable()
					{
						public void run()
						{
							ReimProgressDialog.dismiss();
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
							Utils.showToast(ApproveInfoActivity.this, R.string.failed_to_get_data, response.getErrorMessage());
						}
					});
				}
			}
		});
	}
}