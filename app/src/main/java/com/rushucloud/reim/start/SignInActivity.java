package com.rushucloud.reim.start;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Selection;
import android.text.Spannable;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.rushucloud.reim.R;
import com.rushucloud.reim.guide.GuideStartActivity;
import com.rushucloud.reim.main.MainActivity;
import com.umeng.analytics.MobclickAgent;

import classes.utils.AppPreference;
import classes.utils.DBManager;
import classes.utils.PhoneUtils;
import classes.utils.ReimApplication;
import classes.utils.Utils;
import classes.utils.ViewUtils;
import classes.utils.WeChatUtils;
import classes.widget.ClearEditText;
import classes.widget.ReimProgressDialog;
import netUtils.common.HttpConnectionCallback;
import netUtils.request.user.SignInRequest;
import netUtils.response.user.SignInResponse;

public class SignInActivity extends Activity
{
    // Widgets
    private ClearEditText usernameEditText;
    private ClearEditText passwordEditText;
    private PopupWindow forgotPopupWindow;

    // Local Data
    private boolean showPassword = false;

    // View
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
        ImageView backImageView = (ImageView) findViewById(R.id.backImageView);
        backImageView.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                goBack();
            }
        });

        usernameEditText = (ClearEditText) findViewById(R.id.usernameEditText);
        ViewUtils.requestFocus(this, usernameEditText);

        passwordEditText = (ClearEditText) findViewById(R.id.passwordEditText);
        passwordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
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

        String username = getIntent().getStringExtra("username");
        String password = getIntent().getStringExtra("password");

        if (username != null)
        {
            usernameEditText.setText(username);
        }

        if (password != null)
        {
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

        TextView signUpTextView = (TextView) findViewById(R.id.signUpTextView);
        signUpTextView.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                ViewUtils.goForwardAndFinish(SignInActivity.this, PhoneSignUpActivity.class);
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

        ImageView wechatImageView = (ImageView) findViewById(R.id.wechatImageView);
        wechatImageView.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                WeChatUtils.sendAuthRequest(SignInActivity.this, null);
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
                String username = usernameEditText.getText().toString();
                Intent intent = new Intent(SignInActivity.this, PhoneFindActivity.class);
                if (Utils.isPhone(username))
                {
                    intent.putExtra("username", username);
                }
                ViewUtils.goForwardAndFinish(SignInActivity.this, intent);
            }
        });

        Button emailButton = (Button) forgorView.findViewById(R.id.emailButton);
        emailButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                MobclickAgent.onEvent(SignInActivity.this, "UMENG_REGIST_FORGETPASSWORD_MAIL");
                forgotPopupWindow.dismiss();
                String username = usernameEditText.getText().toString();
                Intent intent = new Intent(SignInActivity.this, EmailFindActivity.class);
                if (Utils.isEmail(username))
                {
                    intent.putExtra("username", username);
                }
                ViewUtils.goForwardAndFinish(SignInActivity.this, intent);
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

    private void hideSoftKeyboard()
    {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(usernameEditText.getWindowToken(), 0);
        imm.hideSoftInputFromWindow(passwordEditText.getWindowToken(), 0);
    }

    private void goBack()
    {
        ViewUtils.goBackWithIntent(SignInActivity.this, WelcomeActivity.class);
    }

    // Network
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
            ViewUtils.requestFocus(SignInActivity.this, usernameEditText);
        }
        else if (password.isEmpty())
        {
            ViewUtils.showToast(SignInActivity.this, R.string.error_password_empty);
            ViewUtils.requestFocus(SignInActivity.this, passwordEditText);
        }
        else if (!Utils.isEmailOrPhone(username))
        {
            ViewUtils.showToast(SignInActivity.this, R.string.error_email_or_phone_wrong_format);
            ViewUtils.requestFocus(SignInActivity.this, usernameEditText);
        }
        else
        {
            AppPreference appPreference = AppPreference.getAppPreference();
            appPreference.setUsername(username);
            appPreference.setPassword(password);
            appPreference.setProxyUserID(-1);
            appPreference.setHasPassword(true);
            appPreference.saveAppPreference();

            sendSignInRequest();
        }
    }

    private void sendSignInRequest()
    {
        ReimProgressDialog.show();
        final SignInRequest request = new SignInRequest();
        request.sendRequest(new HttpConnectionCallback()
        {
            public void execute(Object httpResponse)
            {
                final SignInResponse response = new SignInResponse(httpResponse);
                if (response.getStatus())
                {
                    final AppPreference appPreference = AppPreference.getAppPreference();
                    appPreference.setServerToken(response.getServerToken());
                    appPreference.setCurrentUserID(response.getCurrentUser().getServerID());
                    appPreference.setProxyUserID(-1);
                    appPreference.setLastShownGuideVersion(response.getLastShownGuideVersion());
                    appPreference.setSyncOnlyWithWifi(true);
                    appPreference.setEnablePasswordProtection(true);
                    appPreference.setLastSyncTime(0);
                    appPreference.setLastGetOthersReportTime(0);
                    appPreference.setLastGetMineStatTime(0);
                    appPreference.setLastGetOthersStatTime(0);
                    appPreference.setSandboxMode(false);

                    Utils.updateGroupInfo(response.getGroup(), response.getCurrentUser(), response.getSetOfBookList(),
                                          response.getCategoryList(), response.getTagList(), response.getMemberList(),
                                          DBManager.getDBManager(), appPreference);

                    // refresh UI
                    runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            ReimProgressDialog.dismiss();
                            if (appPreference.getLastShownGuideVersion() < ReimApplication.GUIDE_VERSION)
                            {
                                ViewUtils.goForwardAndFinish(SignInActivity.this, GuideStartActivity.class);
                            }
                            else
                            {
                                ViewUtils.goForwardAndFinish(SignInActivity.this, MainActivity.class);
                            }
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
}