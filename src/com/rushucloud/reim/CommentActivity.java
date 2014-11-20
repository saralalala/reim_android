package com.rushucloud.reim;

import java.util.ArrayList;
import java.util.List;

import netUtils.HttpConnectionCallback;
import netUtils.HttpConstant;
import netUtils.Request.DownloadImageRequest;
import netUtils.Response.DownloadImageResponse;
import com.rushucloud.reim.R;
import com.umeng.analytics.MobclickAgent;

import classes.Comment;
import classes.User;
import classes.Utils;
import classes.Adapter.CommentListViewAdapater;
import database.DBManager;
import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

public class CommentActivity extends Activity
{
	private CommentListViewAdapater adapter;

	private DBManager dbManager;
	private List<Comment> commentList;
	private int reportID;
	private boolean myReport;
	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.report_comment);
		initData();
		initView();
	}

	protected void onResume()
	{
		super.onResume();
		MobclickAgent.onPageStart("CommentActivity");		
		MobclickAgent.onResume(this);
	}

	protected void onPause()
	{
		super.onPause();
		MobclickAgent.onPageEnd("CommentActivity");
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
		dbManager = DBManager.getDBManager();
		
		reportID = getIntent().getIntExtra("reportLocalID", -1);
		if (reportID == -1)
		{
			myReport = false;
			reportID = getIntent().getIntExtra("reportServerID", -1);
			if (reportID != -1)
			{
				commentList = dbManager.getOthersReportComments(reportID);				
			}
			else
			{
				commentList = new ArrayList<Comment>();
			}
		}
		else
		{
			myReport = true;
			commentList = dbManager.getReportComments(reportID);
		}
		
		if (commentList != null || commentList.size() > 0)
		{
			Comment.sortByCreateDate(commentList);
		}
	}
	
	private void initView()
	{
		ListView commentListView = (ListView)findViewById(R.id.commentListView);
		TextView commentTextView = (TextView)findViewById(R.id.commentTextView);
		if (commentList == null || commentList.size() == 0)
		{
			commentListView.setVisibility(View.GONE);
		}
		else
		{
			commentTextView.setVisibility(View.GONE);
			adapter = new CommentListViewAdapater(this, commentList);
			commentListView.setAdapter(adapter);
			
			if (Utils.isNetworkConnected())
			{
				for (Comment comment : commentList)
				{
					User user = comment.getReviewer();
					if (user.hasUndownloadedAvatar())
					{
						sendDownloadAvatarRequest(user);
					}
				}			
			}			
		}
	}

    private void sendDownloadAvatarRequest(final User user)
    {
    	DownloadImageRequest request = new DownloadImageRequest(user.getImageID(), DownloadImageRequest.IMAGE_QUALITY_VERY_HIGH);
    	request.sendRequest(new HttpConnectionCallback()
		{
			public void execute(Object httpResponse)
			{
				DownloadImageResponse response = new DownloadImageResponse(httpResponse);
				if (response.getBitmap() != null)
				{
					String avatarPath = Utils.saveBitmapToFile(response.getBitmap(), HttpConstant.IMAGE_TYPE_AVATAR);
					user.setAvatarPath(avatarPath);
					user.setLocalUpdatedDate(Utils.getCurrentTime());
					user.setServerUpdatedDate(user.getLocalUpdatedDate());
					dbManager.updateUser(user);
					runOnUiThread(new Runnable()
					{
						public void run()
						{
							if (myReport)
							{
								commentList = dbManager.getReportComments(reportID);
							}
							else
							{
								commentList = dbManager.getOthersReportComments(reportID);
							}
							adapter.setComments(commentList);
							adapter.notifyDataSetChanged();
						}
					});	
				}
			}
		});
    }
}