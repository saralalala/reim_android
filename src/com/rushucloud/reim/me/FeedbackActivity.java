package com.rushucloud.reim.me;

import netUtils.HttpConnectionCallback;
import netUtils.Request.FeedbackRequest;
import netUtils.Response.FeedbackResponse;

import classes.Utils.Utils;

import com.rushucloud.reim.R;
import com.umeng.analytics.MobclickAgent;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class FeedbackActivity extends Activity
{
	private EditText feedbackEditText;
	private EditText contactEditText;	
	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_me_feedback);
		initView();
	}

	protected void onResume()
	{
		super.onResume();
		MobclickAgent.onPageStart("FeedbackActivity");		
		MobclickAgent.onResume(this);
	}

	protected void onPause()
	{
		super.onPause();
		MobclickAgent.onPageEnd("FeedbackActivity");
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
		
		feedbackEditText = (EditText)findViewById(R.id.feedbackEditText);
		feedbackEditText.setOnFocusChangeListener(Utils.getEditTextFocusChangeListener());
		
		contactEditText = (EditText)findViewById(R.id.contactEditText);
		contactEditText.setOnFocusChangeListener(Utils.getEditTextFocusChangeListener());

		Button submitButton = (Button)findViewById(R.id.submitButton);
		submitButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				hideSoftKeyboard();
				if (Utils.isNetworkConnected())
				{
					sendFeedBack();
				}
				else
				{
					Utils.showToast(FeedbackActivity.this, "网络未连接，无法发送反馈");
				}
			}
		});
		submitButton = Utils.resizeLongButton(submitButton);
		
		LinearLayout layout = (LinearLayout)findViewById(R.id.baseLayout);
		layout.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				hideSoftKeyboard();
			}
		});
	}
	
    private void hideSoftKeyboard()
    {
		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE); 
		imm.hideSoftInputFromWindow(feedbackEditText.getWindowToken(), 0);					
		imm.hideSoftInputFromWindow(contactEditText.getWindowToken(), 0);  	
    }
    
    private void sendFeedBack()
    {
		final String feedback = feedbackEditText.getText().toString();
		final String contactInfo = contactEditText.getText().toString();
		if (feedback.equals("") && contactInfo.equals(""))
		{
			Utils.showToast(this, "意见或联系方式不能均为空");
		}
		else
		{
	    	sendFeedbackRequest(feedback, contactInfo, Utils.getCurrentVersion());
		}    	
    }
    
    private void sendFeedbackRequest(String feedback, String contactInfo, String versionName)
    {
    	FeedbackRequest request = new FeedbackRequest(feedback, contactInfo, versionName);
    	request.sendRequest(new HttpConnectionCallback()
		{
			public void execute(Object httpResponse)
			{
				final FeedbackResponse response = new FeedbackResponse(httpResponse);
				runOnUiThread(new Runnable()
				{
					public void run()
					{
						if (response.getStatus())
						{
							Builder builder = new Builder(FeedbackActivity.this);
							builder.setTitle(R.string.tip);
							builder.setMessage(R.string.prompt_feedback_sent);
							builder.setNegativeButton(R.string.confirm, new DialogInterface.OnClickListener()
																{
																	public void onClick(DialogInterface dialog, int which)
																	{
																		finish();
																	}
																});
							builder.create().show();
						}
						else
						{
							Utils.showToast(FeedbackActivity.this, "反馈发送失败！" + response.getErrorMessage());
						}
					}						
				});
			}
		});
    }
}