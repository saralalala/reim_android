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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.rushucloud.reim.R;
import com.rushucloud.reim.guide.GuideStartActivity;
import com.umeng.analytics.MobclickAgent;

import classes.model.User;
import classes.utils.AppPreference;
import classes.utils.DBManager;
import classes.utils.PhoneUtils;
import classes.utils.Utils;
import classes.utils.ViewUtils;
import classes.utils.WeChatUtils;
import classes.widget.ClearEditText;
import classes.widget.ReimProgressDialog;
import netUtils.common.HttpConnectionCallback;
import netUtils.request.user.RegisterRequest;
import netUtils.request.user.VerifyCodeRequest;
import netUtils.response.user.RegisterResponse;
import netUtils.response.user.VerifyCodeResponse;

public class PhoneSignUpActivity extends Activity
{
    // Widgets
    private ClearEditText phoneEditText;
    private ClearEditText passwordEditText;
    private EditText codeEditText;
    private Button acquireCodeButton;

    // Local Data
    private boolean showPassword = false;
    private int waitingTime;
    private Thread thread;

    // View
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_sign_up_by_phone);
        initView();
    }

    protected void onResume()
    {
        super.onResume();
        MobclickAgent.onPageStart("PhoneSignUpActivity");
        MobclickAgent.onResume(this);
        ReimProgressDialog.setContext(this);
    }

    protected void onPause()
    {
        super.onPause();
        MobclickAgent.onPageEnd("PhoneSignUpActivity");
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

        phoneEditText = (ClearEditText) findViewById(R.id.phoneEditText);
        ViewUtils.requestFocus(this, phoneEditText);

        passwordEditText = (ClearEditText) findViewById(R.id.passwordEditText);
        passwordEditText.setTransformationMethod(new PasswordTransformationMethod());

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

        codeEditText = (EditText) findViewById(R.id.codeEditText);
        codeEditText.setOnFocusChangeListener(ViewUtils.onFocusChangeListener);
        codeEditText.setOnKeyListener(new View.OnKeyListener()
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

        acquireCodeButton = (Button) findViewById(R.id.acquireCodeButton);
        acquireCodeButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                MobclickAgent.onEvent(PhoneSignUpActivity.this, "UMENG_REGIST_TEL-CAPTCHA");
                hideSoftKeyboard();

                String phoneNumber = phoneEditText.getText().toString();
                if (!PhoneUtils.isNetworkConnected())
                {
                    ViewUtils.showToast(PhoneSignUpActivity.this, R.string.error_request_network_unavailable);
                }
                else if (phoneNumber.isEmpty())
                {
                    ViewUtils.showToast(PhoneSignUpActivity.this, R.string.error_phone_empty);
                    ViewUtils.requestFocus(PhoneSignUpActivity.this, phoneEditText);
                }
                else if (!Utils.isPhone(phoneNumber))
                {
                    ViewUtils.showToast(PhoneSignUpActivity.this, R.string.error_phone_wrong_format);
                    ViewUtils.requestFocus(PhoneSignUpActivity.this, phoneEditText);
                }
                else
                {
                    getVerifyCode(Utils.removePhonePrefix(phoneNumber));
                }
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

        ImageView emailImageView = (ImageView) findViewById(R.id.emailImageView);
        emailImageView.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                ViewUtils.goForwardAndFinish(PhoneSignUpActivity.this, EmailSignUpActivity.class);
            }
        });

        ImageView wechatImageView = (ImageView) findViewById(R.id.wechatImageView);
        wechatImageView.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                WeChatUtils.sendAuthRequest(PhoneSignUpActivity.this, null);
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
    }

    private void hideSoftKeyboard()
    {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(phoneEditText.getWindowToken(), 0);
        imm.hideSoftInputFromWindow(passwordEditText.getWindowToken(), 0);
        imm.hideSoftInputFromWindow(codeEditText.getWindowToken(), 0);
    }

    private void goBack()
    {
        waitingTime = -1;
        ViewUtils.goBackWithIntent(PhoneSignUpActivity.this, WelcomeActivity.class);
    }

    // Network
    private void getVerifyCode(String phoneNumber)
    {
        final String second = ViewUtils.getString(R.string.second);
        waitingTime = 60;
        acquireCodeButton.setEnabled(false);
        acquireCodeButton.setText(waitingTime + second);
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
                                acquireCodeButton.setText(waitingTime + second);
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
        VerifyCodeRequest request = new VerifyCodeRequest(phoneNumber, false);
        request.sendRequest(new HttpConnectionCallback()
        {
            public void execute(Object httpResponse)
            {
                final VerifyCodeResponse response = new VerifyCodeResponse(httpResponse);
                if (response.getStatus())
                {
                    runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            ReimProgressDialog.dismiss();
                            ViewUtils.showToast(PhoneSignUpActivity.this, R.string.succeed_in_sending_message);
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
                            ViewUtils.showToast(PhoneSignUpActivity.this, R.string.failed_to_get_code, response.getErrorMessage());
                        }
                    });
                }
            }
        });
    }

    private void signUp()
    {
        MobclickAgent.onEvent(PhoneSignUpActivity.this, "UMENG_REGIST_TEL-SUBMIT");
        hideSoftKeyboard();

        String phoneNumber = phoneEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        String inputCode = codeEditText.getText().toString();

        if (!PhoneUtils.isNetworkConnected())
        {
            ViewUtils.showToast(PhoneSignUpActivity.this, R.string.error_request_network_unavailable);
        }
        else if (!Utils.isPhone(phoneNumber))
        {
            ViewUtils.showToast(PhoneSignUpActivity.this, R.string.error_phone_wrong_format);
            ViewUtils.requestFocus(PhoneSignUpActivity.this, phoneEditText);
        }
        else if (phoneNumber.isEmpty())
        {
            ViewUtils.showToast(PhoneSignUpActivity.this, R.string.error_phone_empty);
            ViewUtils.requestFocus(PhoneSignUpActivity.this, phoneEditText);
        }
        else if (password.isEmpty())
        {
            ViewUtils.showToast(PhoneSignUpActivity.this, R.string.error_password_empty);
            ViewUtils.requestFocus(PhoneSignUpActivity.this, passwordEditText);
        }
        else if (inputCode.isEmpty())
        {
            ViewUtils.showToast(PhoneSignUpActivity.this, R.string.error_code_empty);
            ViewUtils.requestFocus(PhoneSignUpActivity.this, codeEditText);
        }
        else
        {
            User user = new User();
            user.setPhone(Utils.removePhonePrefix(phoneNumber));
            user.setPassword(password);

            sendRegisterRequest(user, inputCode);
        }
    }

    private void sendRegisterRequest(final User user, String verifyCode)
    {
        ReimProgressDialog.show();
        RegisterRequest request = new RegisterRequest(user, verifyCode);
        request.sendRequest(new HttpConnectionCallback()
        {
            public void execute(Object httpResponse)
            {
                final RegisterResponse response = new RegisterResponse(httpResponse);
                if (response.getStatus())
                {
                    AppPreference appPreference = AppPreference.getAppPreference();
                    appPreference.setUsername(user.getPhone());
                    appPreference.setPassword(user.getPassword());
                    appPreference.setHasPassword(true);
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

                    Utils.updateGroupInfo(response.getGroup(), response.getCurrentUser(), response.getSetOfBookList(),
                                          response.getCategoryList(), response.getTagList(), response.getMemberList(),
                                          DBManager.getDBManager(), appPreference);

                    // refresh UI
                    runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            ReimProgressDialog.dismiss();
                            waitingTime = -1;
                            ViewUtils.showToast(PhoneSignUpActivity.this, R.string.succeed_in_sign_up);
                            ViewUtils.goForwardAndFinish(PhoneSignUpActivity.this, GuideStartActivity.class);
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
                            ViewUtils.showToast(PhoneSignUpActivity.this, R.string.failed_to_sign_up, response.getErrorMessage());
                        }
                    });
                }
            }
        });
    }
}