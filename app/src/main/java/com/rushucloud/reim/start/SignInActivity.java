package com.rushucloud.reim.start;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.method.PasswordTransformationMethod;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.rushucloud.reim.MainActivity;
import com.rushucloud.reim.R;
import com.umeng.analytics.MobclickAgent;

import classes.User;
import classes.utils.AppPreference;
import classes.utils.DBManager;
import classes.utils.PhoneUtils;
import classes.utils.Utils;
import classes.utils.ViewUtils;
import classes.widget.ClearEditText;
import classes.widget.ReimProgressDialog;
import netUtils.HttpConnectionCallback;
import netUtils.request.user.SignInRequest;
import netUtils.response.user.SignInResponse;

public class SignInActivity extends Activity
{
	private ClearEditText usernameEditText;
	private ClearEditText passwordEditText;
	private PopupWindow forgotPopupWindow;

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
		ReimProgressDialog.setContext(this);
	}

	protected void onPause()
	{
		super.onPause();
		MobclickAgent.onPageEnd("SignInActivity");
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

	private void initView()
	{
		getActionBar().hide();

		ImageView backImageView = (ImageView) findViewById(R.id.backImageView);
		backImageView.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
                goBack();
			}
		});
		
		String username = getIntent().getStringExtra("username");
		String password = getIntent().getStringExtra("password");
		
		usernameEditText = (ClearEditText) findViewById(R.id.usernameEditText);
		
		passwordEditText = (ClearEditText) findViewById(R.id.passwordEditText);
        passwordEditText.setTransformationMethod(new PasswordTransformationMethod());
        passwordEditText.setOnKeyListener(new View.OnKeyListener()
        {
            public boolean onKey(View v, int keyCode, KeyEvent event)
            {
                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_ENTER)
                {
                    signIn();
                }
                return false;
            }
        });

		if (username != null)
		{
			usernameEditText.setText(username);
			passwordEditText.setText(password);
		}

		Button signInButton = (Button) findViewById(R.id.signInButton);
		signInButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
                signIn();
			}
		});


        TextView forgorPasswordTextView = (TextView) findViewById(R.id.forgotTextView);
		forgorPasswordTextView.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                MobclickAgent.onEvent(SignInActivity.this, "UMENG_REGIST_FORGETPASSWORD");
                showForgotWindow();
            }
        });

        LinearLayout signUpLayout = (LinearLayout) findViewById(R.id.signUpLayout);
        signUpLayout.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                startActivity(new Intent(SignInActivity.this, WelcomeActivity.class));
                finish();
            }
        });

        RelativeLayout baseLayout = (RelativeLayout) findViewById(R.id.baseLayout);
        baseLayout.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                hideSoftKeyboard();
            }
        });

		// init forgot window
		View forgorView = View.inflate(this, R.layout.window_start_find_password, null);
		
		Button phoneButton = (Button) forgorView.findViewById(R.id.phoneButton);
		phoneButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				MobclickAgent.onEvent(SignInActivity.this, "UMENG_REGIST_FORGETPASSWORD_TEL");
				forgotPopupWindow.dismiss();
				
				startActivity(new Intent(SignInActivity.this, PhoneFindActivity.class));
				finish();
			}
		});
		
		Button emailButton = (Button) forgorView.findViewById(R.id.emailButton);
		emailButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				MobclickAgent.onEvent(SignInActivity.this, "UMENG_REGIST_FORGETPASSWORD_MAIL");
				forgotPopupWindow.dismiss();
				
				startActivity(new Intent(SignInActivity.this, EmailFindActivity.class));
				finish();
			}
		});
		
		Button cancelButton = (Button) forgorView.findViewById(R.id.cancelButton);
		cancelButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				forgotPopupWindow.dismiss();
			}
		});
		
		forgotPopupWindow = ViewUtils.buildBottomPopupWindow(this, forgorView);
	}

    private void showForgotWindow()
    {
		forgotPopupWindow.showAtLocation(findViewById(R.id.baseLayout), Gravity.BOTTOM, 0, 0);
		forgotPopupWindow.update();

		ViewUtils.dimBackground(this);
    }

    private void signIn()
    {
        MobclickAgent.onEvent(SignInActivity.this, "UMENG_LOGIN");
        hideSoftKeyboard();

        final String username = usernameEditText.getText().toString();
        final String password = passwordEditText.getText().toString();

        if (!PhoneUtils.isNetworkConnected())
        {
            ViewUtils.showToast(SignInActivity.this, R.string.error_request_network_unavailable);
        }
        else if (username.isEmpty())
        {
            ViewUtils.showToast(SignInActivity.this, R.string.error_username_empty);
            usernameEditText.requestFocus();
        }
        else if (password.isEmpty())
        {
            ViewUtils.showToast(SignInActivity.this, R.string.error_password_empty);
            passwordEditText.requestFocus();
        }
        else if (!Utils.isEmailOrPhone(username))
        {
            ViewUtils.showToast(SignInActivity.this, R.string.error_email_or_phone_wrong_format);
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
                            currentUser.setAvatarLocalPath(localUser.getAvatarLocalPath());
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
                    AppPreference appPreference = AppPreference.getAppPreference();
                    appPreference.setUsername("");
                    appPreference.setPassword("");
                    appPreference.saveAppPreference();

                    runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            ReimProgressDialog.dismiss();
                            ViewUtils.showToast(SignInActivity.this, R.string.failed_to_sign_in, response.getErrorMessage());
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

    private void goBack()
    {
        startActivity(new Intent(SignInActivity.this, WelcomeActivity.class));
        finish();
    }
}