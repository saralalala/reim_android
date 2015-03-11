package com.rushucloud.reim.start;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.rushucloud.reim.R;
import com.umeng.analytics.MobclickAgent;

import classes.utils.PhoneUtils;
import classes.utils.Utils;
import classes.utils.ViewUtils;
import classes.widget.ReimProgressDialog;
import netUtils.HttpConnectionCallback;
import netUtils.Request.User.ForgotPasswordRequest;
import netUtils.Response.User.ForgotPasswordResponse;

public class PhoneFindActivity extends Activity
{	
	private EditText phoneEditText;
	private EditText codeEditText;
	private Button acquireCodeButton;
	
	private int cid = -1;
	private String code = "";
	private int waitingTime;
	private Thread thread;
	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_start_find_by_phone);
		initView();
	}

	protected void onResume()
	{
		super.onResume();
		MobclickAgent.onPageStart("PhoneFindActivity");		
		MobclickAgent.onResume(this);
		ReimProgressDialog.setProgressDialog(this);
	}

	protected void onPause()
	{
		super.onPause();
		MobclickAgent.onPageEnd("PhoneFindActivity");
		MobclickAgent.onPause(this);
	}
	
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if (keyCode == KeyEvent.KEYCODE_BACK)
		{
			startActivity(new Intent(PhoneFindActivity.this, SignInActivity.class));
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
				startActivity(new Intent(PhoneFindActivity.this, SignInActivity.class));
				finish();
			}
		});
    	
		phoneEditText = (EditText) findViewById(R.id.phoneEditText);
		phoneEditText.setOnFocusChangeListener(ViewUtils.onFocusChangeListener);
		
    	codeEditText = (EditText) findViewById(R.id.codeEditText);
    	codeEditText.setOnFocusChangeListener(ViewUtils.onFocusChangeListener);
    	
    	acquireCodeButton = (Button) findViewById(R.id.acquireCodeButton);
    	acquireCodeButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				MobclickAgent.onEvent(PhoneFindActivity.this, "UMENG_REGIST_FORGETPASSWORD_TEL-CAPTCHA");
				
				String phoneNumber = phoneEditText.getText().toString();
				if (!PhoneUtils.isNetworkConnected())
				{
					ViewUtils.showToast(PhoneFindActivity.this, R.string.error_request_network_unavailable);
				}
				else if (!Utils.isPhone(phoneNumber))
				{
					ViewUtils.showToast(PhoneFindActivity.this, R.string.error_phone_wrong_format);
					phoneEditText.requestFocus();	
				}
				else
				{
					hideSoftKeyboard();
					sendTextMessage();
				}
			}
		});
    	
    	Button nextButton = (Button) findViewById(R.id.nextButton);
    	nextButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				MobclickAgent.onEvent(PhoneFindActivity.this, "UMENG_REGIST_FORGETPASSWORD_TEL-SUBMIT");
				
				if (code.isEmpty())
				{
					ViewUtils.showToast(PhoneFindActivity.this, R.string.error_no_code);
				}
				else if (!codeEditText.getText().toString().equals(code))
				{
					ViewUtils.showToast(PhoneFindActivity.this, R.string.error_wrong_code);
				}
				else
				{
					Bundle bundle = new Bundle();
					bundle.putInt("cid", cid);
					bundle.putString("code", code);
					Intent intent = new Intent(PhoneFindActivity.this, ResetPasswordActivity.class);
					intent.putExtras(bundle);
					startActivity(intent);
					finish();					
				}
			}
		});
		
    	RelativeLayout baseLayout=(RelativeLayout) findViewById(R.id.baseLayout);
    	baseLayout.setOnClickListener(new View.OnClickListener()
    	{
			public void onClick(View v)
			{
				hideSoftKeyboard();
			}
		});
    }
    
    private void sendTextMessage()
    {
		waitingTime = 60;
		acquireCodeButton.setEnabled(false);
		acquireCodeButton.setText(waitingTime + "秒");
		thread = new Thread(new Runnable()
		{
			public void run()
			{
				try
				{
					while (waitingTime > 0)
					{
						java.lang.Thread.sleep(1000);
						waitingTime -= 1;
						runOnUiThread(new Runnable()
						{
							public void run()
							{
								acquireCodeButton.setText(waitingTime + "秒");
							}
						});	
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
				finally
				{
					runOnUiThread(new Runnable()
					{
						public void run()
						{
							acquireCodeButton.setText(R.string.acquire_code);
							acquireCodeButton.setEnabled(true);
						}
					});	
				}
			}
		});
		thread.start();
		
		ReimProgressDialog.show();
		ForgotPasswordRequest request = new ForgotPasswordRequest(1, phoneEditText.getText().toString());
		request.sendRequest(new HttpConnectionCallback()
		{
			public void execute(Object httpResponse)
			{
				final ForgotPasswordResponse response = new ForgotPasswordResponse(httpResponse);
				if (response.getStatus())
				{
					cid = response.getVerifyCodeID();
					code = response.getVerifyCode();
					
					runOnUiThread(new Runnable()
					{
						public void run()
						{
							ReimProgressDialog.dismiss();
							ViewUtils.showToast(PhoneFindActivity.this, R.string.prompt_message_sent);
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
							thread.interrupt();
							ViewUtils.showToast(PhoneFindActivity.this, R.string.failed_to_send_message, response.getErrorMessage());
						}
					});
				}
			}
		});
    }
	
    private void hideSoftKeyboard()
    {
		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE); 
		imm.hideSoftInputFromWindow(phoneEditText.getWindowToken(), 0);	
		imm.hideSoftInputFromWindow(codeEditText.getWindowToken(), 0);
    }
}