package com.rushucloud.reim.me;

import netUtils.HttpConnectionCallback;
import netUtils.Request.FeedbackRequest;
import netUtils.Response.FeedbackResponse;

import classes.ReimApplication;

import com.rushucloud.reim.MainActivity;
import com.rushucloud.reim.R;
import com.umeng.analytics.MobclickAgent;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

public class FeedbackActivity extends Activity
{
	private EditText feedbackEditText;
	private EditText contactEditText;	
	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.profile_feedback);
		initView();
		initButton();
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
			goBackToMainActivity();
		}
		return super.onKeyDown(keyCode, event);
	}
	
	private void initView()
	{
		feedbackEditText = (EditText)findViewById(R.id.feedbackEditText);
		contactEditText = (EditText)findViewById(R.id.contactEditText);
		
		LinearLayout layout = (LinearLayout)findViewById(R.id.baseLayout);
		layout.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				hideSoftKeyboard();
			}
		});
	}
	
	private void initButton()
	{	
		Button submitButton = (Button)findViewById(R.id.submitButton);
		submitButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				hideSoftKeyboard();
				final String feedback = feedbackEditText.getText().toString();
				final String contactInfo = contactEditText.getText().toString();
				if (feedback.equals("") && contactInfo.equals(""))
				{
					AlertDialog mDialog = new AlertDialog.Builder(FeedbackActivity.this)
											.setTitle("错误")
											.setMessage("意见或联系方式不能均为空")
											.setNegativeButton(R.string.confirm, null)
											.create();
					mDialog.show();		
				}
				else
				{
					try
					{
						PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), 0);
				    	sendFeedbackRequest(feedback, contactInfo, info.versionName);
					}
					catch (NameNotFoundException e)
					{
						e.printStackTrace();
						Toast.makeText(FeedbackActivity.this, "获取版本号失败", Toast.LENGTH_SHORT).show();
					}
				}
			}
		});
		
		Button cancelButton = (Button)findViewById(R.id.cancelButton);
		cancelButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				hideSoftKeyboard();
				goBackToMainActivity();
			}
		});	
	}
	
    private void hideSoftKeyboard()
    {
		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE); 
		imm.hideSoftInputFromWindow(feedbackEditText.getWindowToken(), 0);					
		imm.hideSoftInputFromWindow(contactEditText.getWindowToken(), 0);  	
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
							AlertDialog mDialog = new AlertDialog.Builder(FeedbackActivity.this)
																.setTitle("成功")
																.setMessage("反馈已发送！")
																.setNegativeButton(R.string.confirm, 
																		new DialogInterface.OnClickListener()
																{
																	public void onClick(DialogInterface dialog, int which)
																	{
																		goBackToMainActivity();
																	}
																})
																.create();
							mDialog.show();
						}
						else
						{
							AlertDialog mDialog = new AlertDialog.Builder(FeedbackActivity.this)
																.setTitle("错误")
																.setMessage("反馈发送失败！" + response.getErrorMessage())
																.setNegativeButton(R.string.confirm, null)
																.create();
							mDialog.show();
						}
					}						
				});
			}
		});
    }

    private void goBackToMainActivity()
    {
    	ReimApplication.setTabIndex(3);
    	Intent intent = new Intent(FeedbackActivity.this, MainActivity.class);
    	intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    	startActivity(intent);
    	finish();
    }
}
