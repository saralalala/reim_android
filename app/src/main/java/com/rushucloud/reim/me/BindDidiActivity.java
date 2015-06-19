package com.rushucloud.reim.me;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.rushucloud.reim.R;
import com.rushucloud.reim.item.DidiExpenseActivity;
import com.umeng.analytics.MobclickAgent;

import java.io.Serializable;

import classes.model.User;
import classes.utils.AppPreference;
import classes.utils.DBManager;
import classes.utils.PhoneUtils;
import classes.utils.Utils;
import classes.utils.ViewUtils;
import classes.widget.ClearEditText;
import classes.widget.ReimProgressDialog;
import netUtils.common.HttpConnectionCallback;
import netUtils.request.item.DidiSignInRequest;
import netUtils.request.item.DidiVerifyCodeRequest;
import netUtils.request.user.BindDidiRequest;
import netUtils.response.item.DidiSignInResponse;
import netUtils.response.item.DidiVerifyCodeResponse;
import netUtils.response.user.BindDidiResponse;

public class BindDidiActivity extends Activity
{
    // Widgets
    private ClearEditText phoneEditText;
    private EditText codeEditText;
    private Button acquireCodeButton;

    // Local Data
    private User currentUser;
    private int waitingTime;
    private Thread thread;

    // View
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_me_bind_didi);
        initData();
        initView();
    }

    protected void onResume()
    {
        super.onResume();
        MobclickAgent.onPageStart("BindDidiActivity");
        MobclickAgent.onResume(this);
        ReimProgressDialog.setContext(this);
    }

    protected void onPause()
    {
        super.onPause();
        MobclickAgent.onPageEnd("BindDidiActivity");
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
        backImageView.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                goBack();
            }
        });

        phoneEditText = (ClearEditText) findViewById(R.id.phoneEditText);
        ViewUtils.requestFocus(this, phoneEditText);

        codeEditText = (EditText) findViewById(R.id.codeEditText);

        phoneEditText.setText("13811891565");
        codeEditText.setText("1234");

        acquireCodeButton = (Button) findViewById(R.id.acquireCodeButton);
        acquireCodeButton.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                hideSoftKeyboard();

                String phoneNumber = phoneEditText.getText().toString();
                if (!PhoneUtils.isNetworkConnected())
                {
                    ViewUtils.showToast(BindDidiActivity.this, R.string.error_request_network_unavailable);
                }
                else if (phoneNumber.isEmpty())
                {
                    ViewUtils.showToast(BindDidiActivity.this, R.string.error_phone_empty);
                    ViewUtils.requestFocus(BindDidiActivity.this, phoneEditText);
                }
                else if (!Utils.isPhone(phoneNumber))
                {
                    ViewUtils.showToast(BindDidiActivity.this, R.string.error_phone_wrong_format);
                    ViewUtils.requestFocus(BindDidiActivity.this, phoneEditText);
                }
                else
                {
                    getVerifyCode(Utils.removePhonePrefix(phoneNumber));
                }
            }
        });

        Button confirmButton = (Button) findViewById(R.id.confirmButton);
        confirmButton.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                hideSoftKeyboard();

                String newPhone = phoneEditText.getText().toString();
                String inputCode = codeEditText.getText().toString();

                if (!PhoneUtils.isNetworkConnected())
                {
                    ViewUtils.showToast(BindDidiActivity.this, R.string.error_modify_network_unavailable);
                }
                else if (newPhone.isEmpty())
                {
                    ViewUtils.showToast(BindDidiActivity.this, R.string.error_new_phone_empty);
                }
                else if (!Utils.isPhone(newPhone))
                {
                    ViewUtils.showToast(BindDidiActivity.this, R.string.error_phone_wrong_format);
                }
                else if (inputCode.isEmpty())
                {
                    ViewUtils.showToast(BindDidiActivity.this, R.string.error_code_empty);
                    ViewUtils.requestFocus(BindDidiActivity.this, codeEditText);
                }
                else
                {
//                    sendDidiSignInRequest(newPhone, inputCode);
                    sendBindDidiRequest("18614097696", "PDPEGnQE8tj1PcsCA0yOhtZAWdU/scRLs2nWdZ7pg/RUjk2uwyAMBu/iNU8KiUMwl3lCYDWRIFT8LKood6/Vrrr6ZjHW+IIxjggOQMEHZqtxQ9oIabWrVlBLYnAC/iH7J5C57yU2cBe0LKNxQUM04aKglVGDePOtIFT2nf/7kflXCpySJLU1GifaDBmph92fJ6fvK9F3319PuZvudwAAAP//");
//                    sendBindDidiRequest("15801628438", "GpLpaibknMm%2FeCunsE1gK8Onz7lCArFdcIOf2EykRA5UzT2qwzAQxPG7TL3Frr1%2BknWZh5GXxCAh0EdlfPeIdKn%2BU%2FxgboxxnQgA4TsWL%2Bp095tbdBcm1JIMQQjHa9YRsvV3ORvCjZZnRFdhZmUltDJqnIwfQqx2dPvvV7ZfFC2l%2BSibZ%2FlbvK4ezycAAP%2F%2F");
                }
            }
        });

        LinearLayout baseLayout = (LinearLayout) findViewById(R.id.baseLayout);
        baseLayout.setOnClickListener(new OnClickListener()
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
        imm.hideSoftInputFromWindow(codeEditText.getWindowToken(), 0);
    }

    private void goBack()
    {
        hideSoftKeyboard();
        ViewUtils.goBack(this);
    }

    // Data
    private void initData()
    {
        currentUser = AppPreference.getAppPreference().getCurrentUser();
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
                        Thread.sleep(1000);
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
        DidiVerifyCodeRequest request = new DidiVerifyCodeRequest(phoneNumber);
        request.sendRequest(new HttpConnectionCallback()
        {
            public void execute(Object httpResponse)
            {
                final DidiVerifyCodeResponse response = new DidiVerifyCodeResponse(httpResponse);
                if (response.getStatus())
                {
                    runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            ReimProgressDialog.dismiss();
                            ViewUtils.showToast(BindDidiActivity.this, R.string.succeed_in_sending_message);
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
                            ViewUtils.showToast(BindDidiActivity.this, R.string.failed_to_get_code);
                        }
                    });
                }
            }
        });
    }

    private void sendDidiSignInRequest(final String phone, String verifyCode)
    {
        ReimProgressDialog.show();
        DidiSignInRequest request = new DidiSignInRequest(phone, verifyCode);
        request.sendRequest(new HttpConnectionCallback()
        {
            public void execute(Object httpResponse)
            {
                final DidiSignInResponse response = new DidiSignInResponse(httpResponse);
                if (response.getStatus())
                {
                    sendBindDidiRequest(phone, response.getToken());
                }
                else
                {
                    runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            ReimProgressDialog.dismiss();
                            ViewUtils.showToast(BindDidiActivity.this, R.string.failed_to_get_didi_token);
                        }
                    });
                }
            }
        });
    }

    private void sendBindDidiRequest(final String phone, final String token)
    {
        BindDidiRequest request = new BindDidiRequest(phone, token);
        request.sendRequest(new HttpConnectionCallback()
        {
            public void execute(Object httpResponse)
            {
                final BindDidiResponse response = new BindDidiResponse(httpResponse);
                if (response.getStatus())
                {
                    currentUser.setDidi(phone);
                    currentUser.setDidiToken(token);
                    DBManager.getDBManager().updateUser(currentUser);

                    runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            ReimProgressDialog.dismiss();
                            ViewUtils.showToast(BindDidiActivity.this, R.string.succeed_in_binding);
                            Intent intent = new Intent(BindDidiActivity.this, DidiExpenseActivity.class);
                            intent.putExtra("expenseList", (Serializable) response.getDidiExpenseList());
                            ViewUtils.goForward(BindDidiActivity.this, intent);
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
                            ViewUtils.showToast(BindDidiActivity.this, R.string.failed_to_bind_didi, response.getErrorMessage());
                        }
                    });
                }
            }
        });
    }
}