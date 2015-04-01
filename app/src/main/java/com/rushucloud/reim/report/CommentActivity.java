package com.rushucloud.reim.report;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.rushucloud.reim.R;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.List;

import classes.Comment;
import classes.Report;
import classes.User;
import classes.adapter.CommentListViewAdapter;
import classes.utils.AppPreference;
import classes.utils.DBManager;
import classes.utils.PhoneUtils;
import classes.utils.ReimBroadcastReceiver;
import classes.utils.Utils;
import classes.utils.ViewUtils;
import classes.widget.ReimProgressDialog;
import netUtils.HttpConnectionCallback;
import netUtils.NetworkConstant;
import netUtils.request.DownloadImageRequest;
import netUtils.request.report.GetReportRequest;
import netUtils.request.report.ModifyReportRequest;
import netUtils.response.DownloadImageResponse;
import netUtils.response.report.GetReportResponse;
import netUtils.response.report.ModifyReportResponse;

public class CommentActivity extends Activity
{
	private TextView commentTextView;
	private ListView commentListView;
	private CommentListViewAdapter adapter;
	private EditText commentEditText;

	private DBManager dbManager;
	private Report report;
	private List<Comment> commentList = new ArrayList<Comment>();
	private boolean myReport;
	private boolean newReport;
	private boolean fromPush;
	private int pushType;
	
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
		ReimProgressDialog.setContext(this);
		if (fromPush)
		{
			sendGetReportRequest(report.getServerID());
		}
		else
		{
			refreshView();			
		}
	}

	protected void onPause()
	{
		super.onPause();
		MobclickAgent.onPageEnd("CommentActivity");
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
		
		Bundle bundle = getIntent().getExtras();
		if (bundle != null)
		{
			report = (Report) bundle.getSerializable("report");
			fromPush = bundle.getBoolean("fromPush", false);
			myReport = bundle.getBoolean("myReport", false);
			newReport = bundle.getBoolean("newReport", false);			
			pushType = bundle.getInt("pushType");
			
			if (myReport)
			{
				commentList = dbManager.getReportComments(report.getLocalID());
			}
			else
			{
				commentList = dbManager.getOthersReportComments(report.getServerID());	
			}
			
			if (commentList != null && !commentList.isEmpty())
			{
				Comment.sortByCreateDate(commentList);
			}
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
				if (myReport)
				{
					MobclickAgent.onEvent(CommentActivity.this, "UMENG_REPORT_MINE_COMMENT_CLOSE");
					String event = newReport? "UMENG_REPORT_NEW_COMMENT_CANCEL" : "UMENG_REPORT_EDIT_COMMENT_CANCEL";
					MobclickAgent.onEvent(CommentActivity.this, event);
				}
				else 
				{
					MobclickAgent.onEvent(CommentActivity.this, "UMENG_REPORT_OTHER_COMMENT_CLOSE");					
				}
				goBack();
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
				if (myReport)
				{
					MobclickAgent.onEvent(CommentActivity.this, "UMENG_REPORT_MINE_COMMENT_SEND");
					String event = newReport? "UMENG_REPORT_NEW_COMMENT_SUBMIT" : "UMENG_REPORT_EDIT_COMMENT_SUBMIT";
					MobclickAgent.onEvent(CommentActivity.this, event);
				}
				else 
				{
					MobclickAgent.onEvent(CommentActivity.this, "UMENG_REPORT_OTHER_COMMENT_SEND");					
				}
				
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

            adapter.setComments(commentList);
            adapter.notifyDataSetChanged();

			if (PhoneUtils.isNetworkConnected())
			{
				for (Comment comment : commentList)
				{
					User user = comment.getReviewer();
					if (user != null && user.hasUndownloadedAvatar())
					{
						sendDownloadAvatarRequest(comment, user);
					}
				}			
			}
		}
	}

    private void sendGetReportRequest(final int reportServerID)
    {
		ReimProgressDialog.show();
    	GetReportRequest request = new GetReportRequest(reportServerID);
    	request.sendRequest(new HttpConnectionCallback()
		{
			public void execute(Object httpResponse)
			{
				final GetReportResponse response = new GetReportResponse(httpResponse);
				if (response.getStatus())
				{
					report = new Report(response.getReport());
					commentList.clear();
					commentList.addAll(report.getCommentList());

                    if (myReport)
                    {
                        Report localReport = dbManager.getReportByServerID(reportServerID);
                        if (localReport != null)
                        {
                            int reportLocalID = localReport.getLocalID();
                            dbManager.deleteReportComments(reportLocalID);
                            for (Comment comment : report.getCommentList())
                            {
                                comment.setReportID(reportLocalID);
                                dbManager.insertComment(comment);
                            }
                        }
                    }
                    else
                    {
                        dbManager.deleteOthersReportComments(reportServerID);
                        for (Comment comment : report.getCommentList())
                        {
                            comment.setReportID(reportServerID);
                            dbManager.insertOthersComment(comment);
                        }
                    }

					runOnUiThread(new Runnable()
					{
						public void run()
						{
							ReimProgressDialog.dismiss();
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
							ViewUtils.showToast(CommentActivity.this, R.string.failed_to_get_data, response.getErrorMessage());
						}
					});
				}
			}
		});
    }
    
	private void sendDownloadAvatarRequest(final Comment comment, final User user)
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
					
					comment.setReviewer(user);
					
					runOnUiThread(new Runnable()
					{
						public void run()
						{
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
					User user = AppPreference.getAppPreference().getCurrentUser();
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
    
    private void goBack()
    {
    	if (fromPush)
		{
    		Bundle bundle = new Bundle();
    		bundle.putSerializable("report", report);
			bundle.putBoolean("fromPush", fromPush);
			bundle.putBoolean("myReport", myReport);

        	Intent intent = new Intent();
        	intent.putExtras(bundle);
        	
        	if (pushType == ReimBroadcastReceiver.REPORT_MINE_REJECTED_ONLY_COMMENT)
			{
				intent.setClass(this, EditReportActivity.class);
			}
        	else if (pushType == ReimBroadcastReceiver.REPORT_OTHERS_APPROVED_ONLY_COMMENT)
			{
				intent.setClass(this, ApproveReportActivity.class);				
			}
        	else
        	{
        		intent.setClass(this, ShowReportActivity.class);
        	}
        	
        	startActivity(intent);
        	finish();
		}
    	else
    	{
			finish();
		}
    }
}