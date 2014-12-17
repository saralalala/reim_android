package com.rushucloud.reim.start;

import netUtils.HttpConnectionCallback;
import netUtils.Request.User.ForgotPasswordRequest;
import netUtils.Response.User.ForgotPasswordResponse;

import classes.Utils;

import com.rushucloud.reim.R;
import com.umeng.analytics.MobclickAgent;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class EmailFindActivity extends Activity
{	
	private EditText emailEditText;
	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_start_find_by_email);
		initView();
	}

	protected void onResume()
	{
		super.onResume();
		MobclickAgent.onPageStart("EmailFindActivity");		
		MobclickAgent.onResume(this);
	}

	protected void onPause()
	{
		super.onPause();
		MobclickAgent.onPageEnd("EmailFindActivity");
		MobclickAgent.onPause(this);
	}
	
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if (keyCode == KeyEvent.KEYCODE_BACK)
		{
			startActivity(new Intent(EmailFindActivity.this, SignInActivity.class));
			finish();
		}
		return super.onKeyDown(keyCode, event);
	}

    private void initView()
    {
    	getActionBar().hide();
    	
		ImageView backImageView = (ImageView) findViewById(R.id.backImageView);
		backImageView.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				startActivity(new Intent(EmailFindActivity.this, SignInActivity.class));
				finish();
			}
		});
    	
    	emailEditText = (EditText)findViewById(R.id.emailEditText);
    	emailEditText.setOnFocusChangeListener(Utils.getEditTextFocusChangeListener());
		
    	Button confirmButton = (Button)findViewById(R.id.confirmButton);
    	confirmButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				if (Utils.isNetworkConnected())
				{
					String emailAddress = emailEditText.getText().toString();
					if (Utils.isEmail(emailAddress))
					{
						hideSoftKeyboard();
						sendResetEmail();
					}
					else
					{
						Utils.showToast(EmailFindActivity.this, "邮箱格式不正确");
						emailEditText.requestFocus();	
					}					
				}
				else
				{
					Utils.showToast(EmailFindActivity.this, "网络未连接，无法发送请求");
				}
			}
		});
    	confirmButton = Utils.resizeLongButton(confirmButton);
		
    	RelativeLayout baseLayout=(RelativeLayout)findViewById(R.id.baseLayout);
    	baseLayout.setOnClickListener(new View.OnClickListener()
    	{
			public void onClick(View v)
			{
				hideSoftKeyboard();
			}
		});
    }
    
    private void sendResetEmail()
    {
		ForgotPasswordRequest request = new ForgotPasswordRequest(0, emailEditText.getText().toString());
		request.sendRequest(new HttpConnectionCallback()
		{
			public void execute(Object httpResponse)
			{
				final ForgotPasswordResponse response = new ForgotPasswordResponse(httpResponse);
				if (response.getStatus())
				{
					runOnUiThread(new Runnable()
					{
						public void run()
						{
							Builder builder = new Builder(EmailFindActivity.this);
							builder.setTitle(R.string.tip);
							builder.setMessage(getString(R.string.email_sent_prompt));
							builder.setNegativeButton(R.string.confirm, new OnClickListener()
														{
															public void onClick(DialogInterface dialog, int which)
															{
																startActivity(new Intent(EmailFindActivity.this, SignInActivity.class));
																finish();
															}
														});
							builder.create().show();
						}
					});					
				}
				else
				{
					runOnUiThread(new Runnable()
					{
						public void run()
						{
							Utils.showToast(EmailFindActivity.this, "邮件发送失败！"+response.getErrorMessage());
						}
					});
				}
			}
		});
    }
    
    private void hideSoftKeyboard()
    {
		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE); 
		imm.hideSoftInputFromWindow(emailEditText.getWindowToken(), 0);
    }
}
