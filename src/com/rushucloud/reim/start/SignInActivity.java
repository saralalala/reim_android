package com.rushucloud.reim.start;

import netUtils.Request.User.SignInRequest;
import netUtils.HttpConnectionCallback;
import netUtils.Response.User.SignInResponse;
import classes.User;
import classes.Utils.AppPreference;
import classes.Utils.Utils;
import classes.Widget.ReimProgressDialog;

import com.rushucloud.reim.MainActivity;
import com.rushucloud.reim.R;
import com.umeng.analytics.MobclickAgent;

import database.DBManager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class SignInActivity extends Activity implements View.OnClickListener
{
	private EditText usernameEditText;
	private EditText passwordEditText;
	private TextView forgorPasswordTextView;
	private PopupWindow forgotPopupWindow;
	private TextView signUpTextView;
	private ImageView signUpImageView;

	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_start_sign_in);
		initView();
	}

	protected void onResume()
	{
		super.onResume();
		MobclickAgent.onPageStart("SignInActivity");
		MobclickAgent.onResume(this);
		ReimProgressDialog.setProgressDialog(this);
	}

	protected void onPause()
	{
		super.onPause();
		MobclickAgent.onPageEnd("SignInActivity");
		MobclickAgent.onPause(this);
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

	private void initView()
	{
		getActionBar().hide();

		ImageView backImageView = (ImageView) findViewById(R.id.backImageView);
		backImageView.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				startActivity(new Intent(SignInActivity.this, WelcomeActivity.class));
				finish();
			}
		});
		
		String username = null;
		String password = null;
		
		Bundle bundle = getIntent().getExtras();
		if (bundle != null)
		{
			username = bundle.getString("username");
			password = bundle.getString("password");
		}
		
		usernameEditText = (EditText)findViewById(R.id.usernameEditText);
		usernameEditText.setOnFocusChangeListener(Utils.getEditTextFocusChangeListener());
		
		passwordEditText = (EditText)findViewById(R.id.passwordEditText);
		passwordEditText.setOnFocusChangeListener(Utils.getEditTextFocusChangeListener());

		if (username != null)
		{
			usernameEditText.setText(username);
			passwordEditText.setText(password);
		}
		
//		usernameEditText.setText("13911977103");
//		passwordEditText.setText("g0YTBhMzE2OTg1OWZhMDMyYjlmOGVkMTE3NDQ3OD");

		Button signInButton = (Button)findViewById(R.id.signInButton);
		signInButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				MobclickAgent.onEvent(SignInActivity.this, "UMENG_LOGIN");
				hideSoftKeyboard();
				
				final String username = usernameEditText.getText().toString();
				final String password = passwordEditText.getText().toString();
				
				if (!Utils.isNetworkConnected())
				{
					Utils.showToast(SignInActivity.this, "网络未连接，无法登录");
				}
				else if (username.equals(""))
				{
					Utils.showToast(SignInActivity.this, "用户名不能为空");
					usernameEditText.requestFocus();
				}
				else if (password.equals(""))
				{
					Utils.showToast(SignInActivity.this, "密码不能为空");
					passwordEditText.requestFocus();
				}
				else if (!Utils.isEmailOrPhone(username))
				{
					Utils.showToast(SignInActivity.this, "手机或邮箱格式不正确");
					usernameEditText.requestFocus();
				}
				else
				{
					AppPreference appPreference = AppPreference.getAppPreference();
					appPreference.setUsername(username);
					appPreference.setPassword(password);
					appPreference.saveAppPreference();

					sendSignInRequest();
				}
			}
		});
		signInButton = Utils.resizeLongButton(signInButton);
		
		forgorPasswordTextView = (TextView)findViewById(R.id.forgotTextView);
		forgorPasswordTextView.setOnClickListener(this);

		// init forgot window
		View forgorView = View.inflate(this, R.layout.window_start_find_password, null);
		
		Button phoneButton = (Button) forgorView.findViewById(R.id.phoneButton);
		phoneButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				forgotPopupWindow.dismiss();
				
				startActivity(new Intent(SignInActivity.this, PhoneFindActivity.class));
				finish();
			}
		});
		phoneButton = Utils.resizeWindowButton(phoneButton);
		
		Button emailButton = (Button) forgorView.findViewById(R.id.emailButton);
		emailButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				forgotPopupWindow.dismiss();
				
				startActivity(new Intent(SignInActivity.this, EmailFindActivity.class));
				finish();
			}
		});
		emailButton = Utils.resizeWindowButton(emailButton);
		
		Button cancelButton = (Button) forgorView.findViewById(R.id.cancelButton);
		cancelButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				forgotPopupWindow.dismiss();
			}
		});
		cancelButton = Utils.resizeWindowButton(cancelButton);
		
		forgotPopupWindow = Utils.constructBottomPopupWindow(this, forgorView);
		
		signUpTextView = (TextView)findViewById(R.id.signUpTextView);
		signUpTextView.setOnClickListener(this);
		
		signUpImageView = (ImageView)findViewById(R.id.signUpImageView);
		signUpImageView.setOnClickListener(this);
		
		RelativeLayout baseLayout = (RelativeLayout)findViewById(R.id.baseLayout);
		baseLayout.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				hideSoftKeyboard();
			}
		});
	}

    private void showForgotWindow()
    {
		forgotPopupWindow.showAtLocation(findViewById(R.id.baseLayout), Gravity.BOTTOM, 0, 0);
		forgotPopupWindow.update();

		Utils.dimBackground(this);
    }
	
	private void sendSignInRequest()
	{
		ReimProgressDialog.show();
		SignInRequest request = new SignInRequest();
		request.sendRequest(new HttpConnectionCallback()
		{
			public void execute(Object httpResponse)
			{
				final SignInResponse response = new SignInResponse(httpResponse);
				if (response.getStatus())
				{
					int currentUserID = response.getCurrentUser().getServerID();
					int currentGroupID = -1;

					DBManager dbManager = DBManager.getDBManager();
					AppPreference appPreference = AppPreference.getAppPreference();
					appPreference.setServerToken(response.getServerToken());
					appPreference.setCurrentUserID(currentUserID);
					appPreference.setSyncOnlyWithWifi(true);
					appPreference.setEnablePasswordProtection(true);

					if (response.getGroup() != null)
					{
						currentGroupID = response.getGroup().getServerID();

						// update AppPreference
						appPreference.setCurrentGroupID(currentGroupID);
						appPreference.saveAppPreference();

						// update members
						User currentUser = response.getCurrentUser();
						User localUser = dbManager.getUser(currentUser.getServerID());
						if (localUser != null && currentUser.getAvatarID() == localUser.getAvatarID())
						{
							currentUser.setAvatarPath(localUser.getAvatarPath());
						}
						
						dbManager.updateGroupUsers(response.getMemberList(), currentGroupID);

						dbManager.syncUser(currentUser);

						// update categories
						dbManager.updateGroupCategories(response.getCategoryList(), currentGroupID);

						// update tags
						dbManager.updateGroupTags(response.getTagList(), currentGroupID);

						// update group info
						dbManager.syncGroup(response.getGroup());
					}
					else
					{
						// update AppPreference
						appPreference.setCurrentGroupID(currentGroupID);
						appPreference.saveAppPreference();

						// update current user
						dbManager.syncUser(response.getCurrentUser());

						// update categories
						dbManager.updateGroupCategories(response.getCategoryList(), currentGroupID);
					}

					// refresh UI
					runOnUiThread(new Runnable()
					{
						public void run()
						{
							ReimProgressDialog.dismiss();
							startActivity(new Intent(SignInActivity.this, MainActivity.class));
							finish();
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
							Utils.showToast(SignInActivity.this, "登录失败！" + response.getErrorMessage());
						}
					});
				}
			}
		});
	}

	private void hideSoftKeyboard()
	{
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(usernameEditText.getWindowToken(), 0);
		imm.hideSoftInputFromWindow(passwordEditText.getWindowToken(), 0);
	}

	public void onClick(View v)
	{
		if (v.equals(forgorPasswordTextView))
		{
			showForgotWindow();
		}
		else if (v.equals(signUpTextView) || v.equals(signUpImageView))
		{
			startActivity(new Intent(SignInActivity.this, WelcomeActivity.class));
			finish();
		}
	}
}