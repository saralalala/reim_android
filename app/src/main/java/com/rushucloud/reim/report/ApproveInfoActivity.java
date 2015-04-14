package com.rushucloud.reim.report;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.rushucloud.reim.R;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.List;

import classes.ApproveInfo;
import classes.Report;
import classes.User;
import classes.adapter.ApproveInfoListViewAdapter;
import classes.utils.DBManager;
import classes.utils.PhoneUtils;
import classes.utils.Utils;
import classes.utils.ViewUtils;
import classes.widget.ReimProgressDialog;
import netUtils.HttpConnectionCallback;
import netUtils.NetworkConstant;
import netUtils.request.DownloadImageRequest;
import netUtils.request.report.ApproveInfoRequest;
import netUtils.response.DownloadImageResponse;
import netUtils.response.report.ApproveInfoResponse;

public class ApproveInfoActivity extends Activity
{
	private TextView timeTextView;
	private ApproveInfoListViewAdapter adapter;
	
	private DBManager dbManager;
	
	private int reportServerID;
	private Report report;
	private List<ApproveInfo> infoList = new ArrayList<ApproveInfo>();
	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_report_approve_info);
		initData();
		initView();
	}

	protected void onResume()
	{
		super.onResume();
		MobclickAgent.onPageStart("ApproveInfoActivity");		
		MobclickAgent.onResume(this);
		ReimProgressDialog.setContext(this);
		refreshView();
	}

	protected void onPause()
	{
		super.onPause();
		MobclickAgent.onPageEnd("ApproveInfoActivity");
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
	
	private void initData()
	{
		dbManager = DBManager.getDBManager();

		reportServerID = getIntent().getIntExtra("reportServerID", -1);
		report = dbManager.getReportByServerID(reportServerID);
		if (report == null)
		{
			report = dbManager.getOthersReport(reportServerID);
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
                goBack();
			}
		});
		
		TextView statusTextView = (TextView) findViewById(R.id.statusTextView);
		statusTextView.setText(report.getStatusString());
		
		TextView senderTextView = (TextView) findViewById(R.id.senderTextView);
		senderTextView.setText(report.getSender().getNickname());
		
		timeTextView = (TextView) findViewById(R.id.timeTextView);
		
		adapter = new ApproveInfoListViewAdapter(this, report, infoList);

		ListView infoListView = (ListView) findViewById(R.id.infoListView);
		infoListView.setAdapter(adapter);
	}

	private void refreshView()
	{
		if (PhoneUtils.isNetworkConnected())
		{
			sendGetApproveInfoRequest(reportServerID);			
		} 
		else
		{
			ViewUtils.showToast(this, R.string.error_get_data_network_unavailable);
		}
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
                    int senderID = report.getSender().getServerID();

                    ApproveInfo submitInfo = new ApproveInfo();
                    submitInfo.setUserID(senderID);
                    submitInfo.setStatus(Report.STATUS_SUBMITTED + 100);
                    submitInfo.setApproveDate(response.getSubmitDate().substring(0, 10));
                    submitInfo.setApproveTime(response.getSubmitDate().substring(11, 16));
                    submitInfo.setStep(0);

                    infoList.clear();
                    infoList.addAll(response.getInfoList());
                    infoList.add(submitInfo);
					adapter.setInfoList(infoList);
					
					for (ApproveInfo info : infoList)
					{
                        info.setReportSenderID(senderID);
						User user = dbManager.getUser(info.getUserID());
						if (user != null && user.hasUndownloadedAvatar())
						{
							sendDownloadAvatarRequest(user);
						}
					}
					
					runOnUiThread(new Runnable()
					{
						public void run()
						{
							ReimProgressDialog.dismiss();
							timeTextView.setText(response.getSubmitDate());
							adapter.notifyDataSetChanged();
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
                            ViewUtils.showToast(ApproveInfoActivity.this, R.string.failed_to_get_data, response.getErrorMessage());
                        }
                    });
				}
			}
		});
	}
	
    private void sendDownloadAvatarRequest(final User user)
    {
    	DownloadImageRequest request = new DownloadImageRequest(user.getAvatarID(), DownloadImageRequest.IMAGE_QUALITY_VERY_HIGH);
    	request.sendRequest(new HttpConnectionCallback()
		{
			public void execute(Object httpResponse)
			{
				DownloadImageResponse response = new DownloadImageResponse(httpResponse);
				if (response.getBitmap() != null)
				{
					String avatarPath = PhoneUtils.saveOriginalBitmapToFile(response.getBitmap(), NetworkConstant.IMAGE_TYPE_AVATAR);
					user.setAvatarLocalPath(avatarPath);
					user.setLocalUpdatedDate(Utils.getCurrentTime());
					user.setServerUpdatedDate(user.getLocalUpdatedDate());
					dbManager.updateUser(user);
					
					runOnUiThread(new Runnable()
					{
						public void run()
						{
							adapter.notifyDataSetChanged();
						}
					});	
				}
			}
		});
    }

    private void goBack()
    {
        ViewUtils.goBack(this);
    }
}