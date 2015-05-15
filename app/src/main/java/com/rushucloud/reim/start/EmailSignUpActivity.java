package com.rushucloud.reim.start;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Selection;
import android.text.Spannable;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.rushucloud.reim.R;
import com.rushucloud.reim.guide.GuideStartActivity;
import com.umeng.analytics.MobclickAgent;

import classes.model.User;
import classes.utils.AppPreference;
import classes.utils.DBManager;
import classes.utils.PhoneUtils;
import classes.utils.ViewUtils;
import classes.utils.WeChatUtils;
import classes.widget.ClearEditText;
import classes.widget.ReimProgressDialog;
import netUtils.HttpConnectionCallback;
import netUtils.request.user.RegisterRequest;
import netUtils.response.user.RegisterResponse;

public class EmailSignUpActivity extends Activity
{
	private ClearEditText emailEditText;
	private ClearEditText passwordEditText;

    private boolean showPassword = false;
	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_start_sign_up_by_email);
		initView();
	}

	protected void onResume()
	{
		super.onResume();
		MobclickAgent.onPageStart("EmailSignUpActivity");		
		MobclickAgent.onResume(this);
		ReimProgressDialog.setContext(this);
	}

	protected void onPause()
	{
		super.onPause();
		MobclickAgent.onPageEnd("EmailSignUpActivity");
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
		ImageView backImageView = (ImageView) findViewById(R.id.backImageView);
		backImageView.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
                goBack();
			}
		});

		emailEditText = (ClearEditText) findViewById(R.id.emailEditText);
        ViewUtils.requestFocus(this, emailEditText);

		passwordEditText = (ClearEditText) findViewById(R.id.passwordEditText);
        passwordEditText.setTransformationMethod(new PasswordTransformationMethod());
        passwordEditText.setOnKeyListener(new View.OnKeyListener()
        {
            public boolean onKey(View v, int keyCode, KeyEvent event)
            {
                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_ENTER)
                {
                    signUp();
                }
                return false;
            }
        });

        final ImageView passwordImageView = (ImageView) findViewById(R.id.passwordImageView);
        passwordImageView.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                if (showPassword)
                {
                    passwordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    passwordImageView.setImageResource(R.drawable.eye_blank);
                }
                else
                {
                    passwordEditText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    passwordImageView.setImageResource(R.drawable.eye_white);
                }
                showPassword = !showPassword;
                Spannable spanText = passwordEditText.getText();
                Selection.setSelection(spanText, spanText.length());
            }
        });

		Button signUpButton = (Button) findViewById(R.id.signUpButton);
		signUpButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
                signUp();
			}
		});

        ImageView phoneImageView = (ImageView) findViewById(R.id.phoneImageView);
        phoneImageView.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                goBack();
            }
        });

        ImageView wechatImageView = (ImageView) findViewById(R.id.wechatImageView);
        wechatImageView.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                WeChatUtils.sendAuthRequest(EmailSignUpActivity.this);
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

    private void signUp()
    {
        MobclickAgent.onEvent(EmailSignUpActivity.this, "UMENG_REGIST_MAIL-SUBMIT");
        hideSoftKeyboard();

        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();

        if (!PhoneUtils.isNetworkConnected())
        {
            ViewUtils.showToast(EmailSignUpActivity.this, R.string.error_request_network_unavailable);
        }
        else if (email.isEmpty())
        {
            ViewUtils.showToast(EmailSignUpActivity.this, R.string.error_email_empty);
            ViewUtils.requestFocus(this, emailEditText);
        }
        else if (password.isEmpty())
        {
            ViewUtils.showToast(EmailSignUpActivity.this, R.string.error_password_empty);
            ViewUtils.requestFocus(this, passwordEditText);
        }
        else
        {
            User user = new User();
            user.setEmail(email);
            user.setPassword(password);

            sendRegisterRequest(user);
        }
    }

	private void sendRegisterRequest(final User user)
	{
		ReimProgressDialog.show();
		RegisterRequest request = new RegisterRequest(user, "");
		request.sendRequest(new HttpConnectionCallback()
		{
			public void execute(Object httpResponse)
			{
				final RegisterResponse response = new RegisterResponse(httpResponse);
				if (response.getStatus())
				{
                    int currentGroupID = -1;

                    DBManager dbManager = DBManager.getDBManager();
                    AppPreference appPreference = AppPreference.getAppPreference();
                    appPreference.setUsername(user.getEmail());
                    appPreference.setPassword(user.getPassword());
                    appPreference.setHasPassword(true);
                    appPreference.setServerToken(response.getServerToken());
                    appPreference.setCurrentUserID(response.getCurrentUser().getServerID());
                    appPreference.setLastShownGuideVersion(response.getLastShownGuideVersion());
                    appPreference.setSyncOnlyWithWifi(true);
                    appPreference.setEnablePasswordProtection(true);
                    appPreference.setLastSyncTime(0);
                    appPreference.setLastGetOthersReportTime(0);
                    appPreference.setLastGetMineStatTime(0);
                    appPreference.setLastGetOthersStatTime(0);

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
                            ViewUtils.showToast(EmailSignUpActivity.this, R.string.succeed_in_sign_up_with_email);
                            ViewUtils.goForwardAndFinish(EmailSignUpActivity.this, GuideStartActivity.class);
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
							ViewUtils.showToast(EmailSignUpActivity.this, R.string.failed_to_sign_up, response.getErrorMessage());
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
		imm.hideSoftInputFromWindow(passwordEditText.getWindowToken(), 0);    	
    }

    private void goBack()
    {
        ViewUtils.goBackWithIntent(EmailSignUpActivity.this, PhoneSignUpActivity.class);
    }
}