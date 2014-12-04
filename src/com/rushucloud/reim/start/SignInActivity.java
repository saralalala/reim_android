package com.rushucloud.reim.start;

import netUtils.Request.User.SignInRequest;
import netUtils.HttpConnectionCallback;
import netUtils.Response.User.SignInResponse;
import classes.AppPreference;
import classes.ReimApplication;
import classes.User;
import classes.Utils;

import com.rushucloud.reim.MainActivity;
import com.rushucloud.reim.R;
import com.umeng.analytics.MobclickAgent;

import database.DBManager;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class SignInActivity extends Activity implements View.OnClickListener
{
	private EditText usernameEditText;
	private EditText passwordEditText;
	private TextView forgorPasswordTextView;
	private TextView signUpTextView;
	private ImageView signUpImageView;

	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.start_sign_in);
		initView();
		initButton();
	}

	protected void onResume()
	{
		super.onResume();
		MobclickAgent.onPageStart("SignInActivity");
		MobclickAgent.onResume(this);
		ReimApplication.setProgressDialog(this);
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
		
		String username = null;
		String password = null;
		
		Bundle bundle = getIntent().getExtras();
		if (bundle != null)
		{
			username = bundle.getString("username");
			password = bundle.getString("password");
		}
		
		usernameEditText = (EditText)findViewById(R.id.usernameEditText);
		passwordEditText = (EditText)findViewById(R.id.passwordEditText);

		if (username != null)
		{
			usernameEditText.setText(username);
			passwordEditText.setText(password);
		}
		
		usernameEditText.setText("a@a.com");
		passwordEditText.setText("111111");

		forgorPasswordTextView = (TextView)findViewById(R.id.forgotTextView);
		forgorPasswordTextView.setOnClickListener(this);

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

	private void initButton()
	{
		final Button confirmButton = (Button)findViewById(R.id.confirmButton);
		confirmButton.setOnClickListener(new View.OnClickListener()
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
					AlertDialog alertDialog = new AlertDialog.Builder(SignInActivity.this)
												.setTitle("错误").setMessage("用户名不能为空")
												.setNegativeButton(R.string.confirm, new OnClickListener()
												{
													public void onClick(DialogInterface dialog, int which)
													{
														usernameEditText.requestFocus();
													}
												}).create();
					alertDialog.show();
				}
				else if (password.equals(""))
				{
					AlertDialog alertDialog = new AlertDialog.Builder(SignInActivity.this)
												.setTitle("错误")
												.setMessage("密码不能为空")
												.setNegativeButton(R.string.confirm, new OnClickListener()
												{
													public void onClick(DialogInterface dialog, int which)
													{
														passwordEditText.requestFocus();
													}
												}).create();
					alertDialog.show();
				}
				else if (!Utils.isEmailOrPhone(username))
				{
					AlertDialog alertDialog = new AlertDialog.Builder(SignInActivity.this)
													.setTitle("错误")
													.setMessage("手机或邮箱格式不正确")
													.setNegativeButton("确定", new OnClickListener()
													{
														public void onClick(DialogInterface dialog, int which)
														{
															usernameEditText.requestFocus();
														}
													}).create();
					alertDialog.show();
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
		confirmButton.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener()
		{
			public void onGlobalLayout()
			{
				Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.button_long_solid_light);
				double ratio = ((double)bitmap.getHeight()) / bitmap.getWidth();
				ViewGroup.LayoutParams params = confirmButton.getLayoutParams();
				params.height = (int)(confirmButton.getWidth() * ratio);;
				confirmButton.setLayoutParams(params);
			}
		});
	}

	private void sendSignInRequest()
	{
		ReimApplication.showProgressDialog();
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
						User localUser = dbManager.getUser(response.getCurrentUser().getServerID());
						if (localUser != null && currentUser.getAvatarID() == localUser.getAvatarID())
						{
							currentUser.setAvatarPath(localUser.getAvatarPath());
						}
						
						dbManager.updateGroupUsers(response.getMemberList(), currentGroupID);

						dbManager.syncUser(response.getCurrentUser());

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
							ReimApplication.dismissProgressDialog();
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
							ReimApplication.dismissProgressDialog();
							AlertDialog alertDialog = new AlertDialog.Builder(SignInActivity.this)
														.setTitle("错误")
														.setMessage("登录失败！" + response.getErrorMessage())
														.setNegativeButton("确定", null).create();
							alertDialog.show();
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
			startActivity(new Intent(SignInActivity.this, ForgotPasswordActivity.class));
			finish();
		}
		else if (v.equals(signUpTextView) || v.equals(signUpImageView))
		{
			startActivity(new Intent(SignInActivity.this, SignUpActivity.class));
			finish();
		}
	}
}