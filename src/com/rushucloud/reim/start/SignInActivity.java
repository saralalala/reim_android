package com.rushucloud.reim.start;

import java.util.ArrayList;
import java.util.List;

import netUtils.HttpConstant;
import netUtils.Request.CommonRequest;
import netUtils.Request.BaseRequest.HttpConnectionCallback;
import netUtils.Response.CommonResponse;
import classes.AppPreference;
import classes.Category;
import classes.ReimApplication;
import classes.Tag;
import classes.User;
import classes.Utils;

import com.rushucloud.reim.MainActivity;
import com.rushucloud.reim.R;

import database.DBManager;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class SignInActivity extends Activity
{
	private EditText usernameEditText;
	private EditText passwordEditText;
	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.start_sign_in);
		viewIntialise();
		buttonInitialise();
	}
	
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if (keyCode == KeyEvent.KEYCODE_BACK)
		{
			startActivity(new Intent(SignInActivity.this, WelcomeActivity.class));
			finish();
		}
		return super.onKeyDown(keyCode, event);
	}
	
	private void viewIntialise()
	{
		usernameEditText = (EditText)findViewById(R.id.usernameEditText);
		passwordEditText = (EditText)findViewById(R.id.passwordEditText);
		
		
		usernameEditText.setText(HttpConstant.DEBUG_EMAIL);
		passwordEditText.setText(HttpConstant.DEBUG_PASSWORD);
		
    	RelativeLayout baseLayout=(RelativeLayout)findViewById(R.id.baseLayout);
    	baseLayout.setOnClickListener(new View.OnClickListener()
    	{
			public void onClick(View v)
			{
				hideSoftKeyboard();
			}
		});   
    	
    	TextView forgorPasswordTextView = (TextView)findViewById(R.id.forgotTextView);
    	forgorPasswordTextView.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				startActivity(new Intent(SignInActivity.this, ForgotPasswordActivity.class));
				finish();
			}
		});
	}
	
	private void buttonInitialise()
	{
		Button confirmButton = (Button)findViewById(R.id.confirmButton);
		confirmButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				final String username = usernameEditText.getText().toString();
				final String password = passwordEditText.getText().toString();
				if (username.equals(""))
				{
					AlertDialog alertDialog = new AlertDialog.Builder(SignInActivity.this)
												.setTitle("错误")
												.setMessage("用户名不能为空")
												.setPositiveButton("确定", new OnClickListener()
												{
													public void onClick(
															DialogInterface dialog,
															int which)
													{
														dialog.dismiss();
														passwordEditText.requestFocus();
													}
												})
												.create();
					alertDialog.show();
					
				}
				else if (password.equals(""))
				{
					AlertDialog alertDialog = new AlertDialog.Builder(SignInActivity.this)
												.setTitle("错误")
												.setMessage("密码不能为空")
												.setPositiveButton("确定", new OnClickListener()
												{
													public void onClick(
															DialogInterface dialog,
															int which)
													{
														dialog.dismiss();
														passwordEditText.requestFocus();
													}
												})
												.create();
					alertDialog.show();
					
				}
				else if (!Utils.isEmailOrPhone(username))
				{
					AlertDialog alertDialog = new AlertDialog.Builder(SignInActivity.this)
													.setTitle("错误")
													.setMessage("手机或邮箱格式不正确")
													.setPositiveButton("确定", new OnClickListener()
													{
														public void onClick(
																DialogInterface dialog,
																int which)
														{
															dialog.dismiss();
															usernameEditText.requestFocus();
														}
													})
													.create();
					alertDialog.show();
				}
				else
				{
					AppPreference appPreference = AppPreference.getAppPreference();
					appPreference.setUsername(username);
					appPreference.setPassword(password);
					ReimApplication reimApplication = (ReimApplication)getApplication();
					reimApplication.saveAppPreference();
					
					signIn();
				}
			}
		});
		
		Button cancelbuButton= (Button)findViewById(R.id.cancelButton);
		cancelbuButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				startActivity(new Intent(SignInActivity.this, WelcomeActivity.class));
				finish();
			}
		});
	}
	
	private void signIn()
	{
		CommonRequest request = new CommonRequest();
		request.sendRequest(new HttpConnectionCallback()
		{
			public void execute(Object httpResponse)
			{
				final CommonResponse response = new CommonResponse(httpResponse);
				if (response.getStatus())
				{
					// save server token
					AppPreference appPreference = AppPreference.getAppPreference();
					appPreference.setServerToken(response.getServerToken());
					User currentUser = response.getCurrentUser();
					appPreference.setCurrentUserID(currentUser.getId());
//					appPreference.setCurrentUserID(response.getCurrentUser().getId());
					ReimApplication application = (ReimApplication)getApplication();
					application.saveAppPreference();

					DBManager dbManager = DBManager.getDataBaseManager(getApplicationContext());
					
					// update members
					List<User> userList = new ArrayList<User>(response.getMemberList());
					for (int i = 0; i < userList.size(); i++)
					{
						dbManager.syncUser(userList.get(i));
					}
					
					// update category
					List<Category> categoryList = new ArrayList<Category>(response.getCategoryList());
					for (int i = 0; i < categoryList.size(); i++)
					{
						dbManager.syncCategory(categoryList.get(i));
					}
					
					// update tag
					List<Tag> tagList = new ArrayList<Tag>(response.getTagList());
					for (int i = 0; i < tagList.size(); i++)
					{
						dbManager.syncTag(tagList.get(i));
					}
					
					// update group info
					dbManager.syncGroup(response.getGroup());
					
					// refresh UI
					runOnUiThread(new Runnable()
					{
						public void run()
						{
							AlertDialog alertDialog = new AlertDialog.Builder(SignInActivity.this)
													.setTitle("提示")
													.setMessage("登录成功！")
													.setPositiveButton("确定", new OnClickListener()
													{
														public void onClick(DialogInterface dialog, int which)
														{
															dialog.dismiss();
															startActivity(new Intent(SignInActivity.this, MainActivity.class));
															finish();
														}
													})
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
							AlertDialog alertDialog = new AlertDialog.Builder(SignInActivity.this)
														.setTitle("错误")
														.setMessage("登录失败！"+response.getErrorMessage())
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
		imm.hideSoftInputFromWindow(usernameEditText.getWindowToken(), 0);					
		imm.hideSoftInputFromWindow(passwordEditText.getWindowToken(), 0);
    }
}
