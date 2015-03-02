package com.rushucloud.reim.report;

import java.util.List;

import netUtils.HttpConnectionCallback;
import netUtils.NetworkConstant;
import netUtils.Response.DownloadImageResponse;
import netUtils.Response.Report.ModifyReportResponse;
import netUtils.Request.DownloadImageRequest;
import netUtils.Request.Report.ModifyReportRequest;

import com.rushucloud.reim.R;
import com.umeng.analytics.MobclickAgent;

import classes.Comment;
import classes.Report;
import classes.User;
import classes.adapter.CommentListViewAdapter;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class CommentActivity extends Activity
{
	private TextView commentTextView;
	private ListView commentListView;
	private CommentListViewAdapter adapter;
	private EditText commentEditText;

	private DBManager dbManager;
	private Report report;
	private List<Comment> commentList;
	private int reportID;
	private boolean myReport;
	private String source;
	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_report_comment);
		initData();
		initView();
	}

	protected void onResume()
	{
		super.onResume();
		MobclickAgent.onPageStart("CommentActivity");		
		MobclickAgent.onResume(this);
		refreshView();
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
		
		Bundle bundle = getIntent().getExtras();
		source = bundle.getString("source", "");
		if (source.equals("EditReportActivity"))
		{
			reportID = bundle.getInt("reportLocalID", -1);
			report = dbManager.getReportByLocalID(reportID);
			myReport = true;
		}
		else if (source.equals("ShowReportActivity"))
		{
			reportID = bundle.getInt("reportLocalID", -1);
			if (reportID == -1)
			{
				myReport = false;
				reportID = bundle.getInt("reportServerID", -1);
				report = dbManager.getOthersReport(reportID);
			}
			else
			{
				myReport = true;
				report = dbManager.getReportByLocalID(reportID);
			}
		}
		else // source.equals("ApproveReportActivity")
		{
			reportID = bundle.getInt("reportServerID", -1);
			report = dbManager.getOthersReport(reportID);
			myReport = false;
		}
		
		// init comment list
		commentList = myReport ? dbManager.getReportComments(reportID) : dbManager.getOthersReportComments(reportID);	
		
		if (commentList != null || !commentList.isEmpty())
		{
			Comment.sortByCreateDate(commentList);
		}
	}
	
	private void initView()
	{
		getActionBar().hide();
		
		ImageView backImageView = (ImageView) findViewById(R.id.backImageView);
		backImageView.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				finish();
			}
		});

		commentTextView = (TextView) findViewById(R.id.commentTextView);
		
		adapter = new CommentListViewAdapter(this, commentList);
		commentListView = (ListView) findViewById(R.id.commentListView);
		commentListView.setAdapter(adapter);		

		commentEditText = (EditText) findViewById(R.id.commentEditText);
		commentEditText.setOnFocusChangeListener(ViewUtils.onFocusChangeListener);
		
		TextView sendTextView = (TextView) findViewById(R.id.sendTextView);
		sendTextView.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE); 
				imm.hideSoftInputFromWindow(commentEditText.getWindowToken(), 0);
				
				String comment = commentEditText.getText().toString();
				if (!PhoneUtils.isNetworkConnected())
				{
					ViewUtils.showToast(CommentActivity.this, R.string.error_comment_network_unavailable);
				}
				else if (comment.isEmpty())
				{
					ViewUtils.showToast(CommentActivity.this, R.string.error_comment_empty);
				}
				else
				{
					sendCommentRequest(comment);
				}
			}
		});
	}

	private void refreshView()
	{
		if (commentList == null || commentList.isEmpty())
		{
			commentListView.setVisibility(View.GONE);
			commentTextView.setVisibility(View.VISIBLE);
		}
		else
		{
			commentListView.setVisibility(View.VISIBLE);
			commentTextView.setVisibility(View.GONE);
			
			if (PhoneUtils.isNetworkConnected())
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
    	DownloadImageRequest request = new DownloadImageRequest(user.getAvatarID(), DownloadImageRequest.IMAGE_QUALITY_VERY_HIGH);
    	request.sendRequest(new HttpConnectionCallback()
		{
			public void execute(Object httpResponse)
			{
				DownloadImageResponse response = new DownloadImageResponse(httpResponse);
				if (response.getBitmap() != null)
				{
					String avatarPath = PhoneUtils.saveBitmapToFile(response.getBitmap(), NetworkConstant.IMAGE_TYPE_AVATAR);
					user.setAvatarPath(avatarPath);
					user.setLocalUpdatedDate(Utils.getCurrentTime());
					user.setServerUpdatedDate(user.getLocalUpdatedDate());
					dbManager.updateUser(user);
					
					runOnUiThread(new Runnable()
					{
						public void run()
						{
							commentList = myReport ? dbManager.getReportComments(reportID) : dbManager.getOthersReportComments(reportID);
							adapter.setComments(commentList);
							adapter.notifyDataSetChanged();
						}
					});	
				}
			}
		});
    }

    private void sendCommentRequest(final String commentContent)
    {
		ReimProgressDialog.show();
		
    	ModifyReportRequest request = new ModifyReportRequest(report, commentContent);
    	request.sendRequest(new HttpConnectionCallback()
		{
			public void execute(Object httpResponse)
			{
				final ModifyReportResponse response = new ModifyReportResponse(httpResponse);
				if (response.getStatus())
				{					
					User user = dbManager.getUser(AppPreference.getAppPreference().getCurrentUserID());
					int currentTime = Utils.getCurrentTime();
					
					Comment comment = new Comment();
					comment.setContent(commentContent);
					comment.setCreatedDate(currentTime);
					comment.setLocalUpdatedDate(currentTime);
					comment.setServerUpdatedDate(currentTime);
					comment.setReviewer(user);
					
					if (myReport)
					{
						comment.setReportID(report.getLocalID());
						dbManager.insertComment(comment);			
					}
					else
					{
						comment.setReportID(report.getServerID());
						dbManager.insertOthersComment(comment);
					}
					
					commentList.add(comment);
					Comment.sortByCreateDate(commentList);
					
					runOnUiThread(new Runnable()
					{
						public void run()
						{
							ReimProgressDialog.dismiss();
							ViewUtils.showToast(CommentActivity.this, R.string.succeed_in_sending_comment);
							commentEditText.setText("");
							adapter.setComments(commentList);
							adapter.notifyDataSetChanged();
							refreshView();
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
							ViewUtils.showToast(CommentActivity.this, R.string.failed_to_send_comment, response.getErrorMessage());
						}
					});					
				}
			}
		});
    }
}