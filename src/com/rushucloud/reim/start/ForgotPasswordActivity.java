package com.rushucloud.reim.start;

import netUtils.HttpConnectionCallback;
import netUtils.Request.User.ForgotPasswordRequest;
import netUtils.Response.User.ForgotPasswordResponse;

import classes.Utils;

import com.rushucloud.reim.R;
import com.umeng.analytics.MobclickAgent;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TabHost;

public class ForgotPasswordActivity extends Activity
{	
	private TabHost tabHost;
	
	private int cid = -1;
	private String code = "";
	
	private EditText emailEditText;
	private EditText phoneEditText;
	private EditText codeEditText;
	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.start_forgot_password);
		initTabHost();
		initView();
		initButton();
	}

	protected void onResume()
	{
		super.onResume();
		MobclickAgent.onPageStart("ForgotPasswordActivity");		
		MobclickAgent.onResume(this);
	}

	protected void onPause()
	{
		super.onPause();
		MobclickAgent.onPageEnd("ForgotPasswordActivity");
		MobclickAgent.onPause(this);
	}
	
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if (keyCode == KeyEvent.KEYCODE_BACK)
		{
			startActivity(new Intent(ForgotPasswordActivity.this, SignInActivity.class));
			finish();
		}
		return super.onKeyDown(keyCode, event);
	}

    private void initTabHost()
    {
        tabHost = (TabHost)findViewById(android.R.id.tabhost);
		tabHost.setup();
        LayoutInflater inflater = LayoutInflater.from(this);  
        inflater.inflate(R.layout.start_find_by_email, tabHost.getTabContentView());
        inflater.inflate(R.layout.start_find_by_phone, tabHost.getTabContentView());  

        tabHost.addTab(tabHost.newTabSpec("findPasswordByEmail")
                .setIndicator(getResources().getText(R.string.findPasswordByEmail))
                .setContent(R.id.emailBaseLayout));
        
        tabHost.addTab(tabHost.newTabSpec("findPasswordByPhone")
            .setIndicator(getResources().getText(R.string.findPasswordByPhone))
            .setContent(R.id.phoneBaseLayout));
        
        DisplayMetrics dm = new DisplayMetrics();  
        getWindowManager().getDefaultDisplay().getMetrics(dm);  
        int screenWidth = dm.widthPixels;   
        tabHost.getTabWidget().getChildTabViewAt(0).setMinimumWidth(screenWidth / 2);  
        tabHost.getTabWidget().getChildTabViewAt(1).setMinimumWidth(screenWidth / 2);
    }

    private void initView()
    {
    	getActionBar().hide();
    	
    	emailEditText = (EditText)findViewById(R.id.emailEditText);
    	phoneEditText = (EditText)findViewById(R.id.mobileEditText);
    	codeEditText = (EditText)findViewById(R.id.codeEditText);
		
    	RelativeLayout emailBaseLayout=(RelativeLayout)findViewById(R.id.emailBaseLayout);
    	emailBaseLayout.setOnClickListener(new View.OnClickListener()
    	{
			public void onClick(View v)
			{
				hideSoftKeyboard();
			}
		});
		
    	RelativeLayout phoneBaseLayout=(RelativeLayout)findViewById(R.id.phoneBaseLayout);
    	phoneBaseLayout.setOnClickListener(new View.OnClickListener()
    	{
			public void onClick(View v)
			{
				hideSoftKeyboard();
			}
		});
    }
    
    private void initButton()
    {
    	Button emailConfirmButton = (Button)findViewById(R.id.emailConfirmButton);
    	emailConfirmButton.setOnClickListener(new View.OnClickListener()
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
						AlertDialog alertDialog = new AlertDialog.Builder(ForgotPasswordActivity.this)
													.setTitle("错误")
													.setMessage("邮箱格式不正确")
													.setPositiveButton("确定", new OnClickListener()
													{
														public void onClick(
																DialogInterface dialog,
																int which)
														{
															dialog.dismiss();
															emailEditText.requestFocus();
														}
													})
													.create();
						alertDialog.show();					
					}					
				}
				else
				{
					Utils.showToast(ForgotPasswordActivity.this, "网络未连接，无法发送请求");
				}
			}
		});

    	Button emailCancelButton = (Button)findViewById(R.id.emailCancelButton);
    	emailCancelButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				startActivity(new Intent(ForgotPasswordActivity.this, SignInActivity.class));
				finish();
			}
		});
    	
    	final Button acquireCodeButton = (Button)findViewById(R.id.acquireCodeButton);
    	acquireCodeButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				if (Utils.isNetworkConnected())
				{
					String phoneNumber = phoneEditText.getText().toString();
					if (Utils.isPhone(phoneNumber))
					{
						hideSoftKeyboard();
						sendTextMessage();
					}
					else
					{
						AlertDialog alertDialog = new AlertDialog.Builder(ForgotPasswordActivity.this)
													.setTitle("错误")
													.setMessage("手机号码格式不正确")
													.setPositiveButton("确定", new OnClickListener()
													{
														public void onClick(
																DialogInterface dialog,
																int which)
														{
															dialog.dismiss();
															phoneEditText.requestFocus();
														}
													})
													.create();
						alertDialog.show();					
					}
				}
				else
				{
					Utils.showToast(ForgotPasswordActivity.this, "网络未连接，无法发送请求");
				}			
			}
		});
		acquireCodeButton.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener()
		{
			public void onGlobalLayout()
			{
				Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.button_short);
				double ratio = ((double)bitmap.getWidth()) / bitmap.getHeight();
				ViewGroup.LayoutParams params = acquireCodeButton.getLayoutParams();
				params.width = (int)(acquireCodeButton.getHeight() * ratio);;
				acquireCodeButton.setLayoutParams(params);
			}
		});    	
    	
    	final Button phoneConfirmButton = (Button)findViewById(R.id.phoneConfirmButton);
    	phoneConfirmButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				if (code.equals(""))
				{
					AlertDialog alertDialog = new AlertDialog.Builder(ForgotPasswordActivity.this)
												.setTitle("错误")
												.setMessage(getResources().getString(R.string.errorNoCode))
												.setPositiveButton("确定", null)
												.create();
					alertDialog.show();
				}
				else if (!codeEditText.getText().toString().equals(code))
				{
					AlertDialog alertDialog = new AlertDialog.Builder(ForgotPasswordActivity.this)
												.setTitle("错误")
												.setMessage(getResources().getString(R.string.errorCode))
												.setPositiveButton("确定", null)
												.create();
					alertDialog.show();					
				}
				else
				{
					Bundle bundle = new Bundle();
					bundle.putInt("cid", cid);
					bundle.putString("code", code);
					Intent intent = new Intent(ForgotPasswordActivity.this, ResetPasswordActivity.class);
					intent.putExtras(bundle);
					startActivity(intent);
					finish();					
				}
			}
		});
		phoneConfirmButton.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener()
		{
			public void onGlobalLayout()
			{
				Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.button_long_solid_light);
				double ratio = ((double)bitmap.getHeight()) / bitmap.getWidth();
				ViewGroup.LayoutParams params = phoneConfirmButton.getLayoutParams();
				params.height = (int)(phoneConfirmButton.getWidth() * ratio);;
				phoneConfirmButton.setLayoutParams(params);
			}
		});

		final Button phoneCancelButton = (Button)findViewById(R.id.phoneCancelButton);
    	phoneCancelButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				startActivity(new Intent(ForgotPasswordActivity.this, SignInActivity.class));
				finish();
			}
		});    	
		phoneCancelButton.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener()
		{
			public void onGlobalLayout()
			{
				Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.button_long_solid_light);
				double ratio = ((double)bitmap.getHeight()) / bitmap.getWidth();
				ViewGroup.LayoutParams params = phoneCancelButton.getLayoutParams();
				params.height = (int)(phoneCancelButton.getWidth() * ratio);;
				phoneCancelButton.setLayoutParams(params);
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
							AlertDialog alertDialog = new AlertDialog.Builder(ForgotPasswordActivity.this)
														.setTitle("提示")
														.setMessage(getResources().getString(R.string.emailSentPrompt))
														.setPositiveButton("确定", null)
														.create();
							alertDialog.show();
						}
					});					
				}
				else
				{
					runOnUiThread(new Runnable()
					{
						public void run()
						{
							AlertDialog alertDialog = new AlertDialog.Builder(ForgotPasswordActivity.this)
														.setTitle("错误")
														.setMessage("邮件发送失败！"+response.getErrorMessage())
														.setPositiveButton("确定", null)
														.create();
							alertDialog.show();	
						}
					});
				}
			}
		});
    }
    
    private void sendTextMessage()
    {
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
				}
				else
				{
					runOnUiThread(new Runnable()
					{
						public void run()
						{
							AlertDialog alertDialog = new AlertDialog.Builder(ForgotPasswordActivity.this)
														.setTitle("错误")
														.setMessage("短信发送失败！"+response.getErrorMessage())
														.setPositiveButton("确定", null)
														.create();
							alertDialog.show();	
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
		imm.hideSoftInputFromWindow(phoneEditText.getWindowToken(), 0);	
		imm.hideSoftInputFromWindow(codeEditText.getWindowToken(), 0);
    }
}
