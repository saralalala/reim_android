package com.rushucloud.reim.me;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.rushucloud.reim.R;
import com.umeng.analytics.MobclickAgent;

import classes.model.BankAccount;
import classes.model.User;
import classes.utils.AppPreference;
import classes.utils.DBManager;
import classes.utils.PhoneUtils;
import classes.utils.ViewUtils;
import classes.widget.ClearEditText;
import classes.widget.ReimProgressDialog;
import netUtils.common.HttpConnectionCallback;
import netUtils.request.bank.CreateBankAccountRequest;
import netUtils.request.bank.ModifyBankAccountRequest;
import netUtils.response.bank.CreateBankAccountResponse;
import netUtils.response.bank.ModifyBankAccountResponse;

public class BankNameActivity extends Activity
{
    // Widgets
    private ClearEditText nameEditText;

    // Local Data
    private DBManager dbManager;
    private User currentUser;
    private BankAccount bankAccount;
    private String originalName;

    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_me_bank_name);
        initData();
        initView();
    }

    protected void onResume()
    {
        super.onResume();
        MobclickAgent.onPageStart("BankNameActivity");
        MobclickAgent.onResume(this);
        ReimProgressDialog.setContext(this);
    }

    protected void onPause()
    {
        super.onPause();
        MobclickAgent.onPageEnd("BankNameActivity");
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

        TextView saveTextView = (TextView) findViewById(R.id.saveTextView);
        saveTextView.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                hideSoftKeyboard();

                String newName = nameEditText.getText().toString();
                if (!PhoneUtils.isNetworkConnected())
                {
                    ViewUtils.showToast(BankNameActivity.this, R.string.error_modify_network_unavailable);
                }
                else if (newName.equals(originalName))
                {
                    goBack();
                }
                else if (newName.isEmpty())
                {
                    ViewUtils.showToast(BankNameActivity.this, R.string.error_account_name_empty);
                    ViewUtils.requestFocus(BankNameActivity.this, nameEditText);
                }
                else if (bankAccount == null)
                {
                    bankAccount = new BankAccount();
                    bankAccount.setName(newName);
                    sendCreateBankAccountRequest();
                }
                else if (bankAccount.getServerID() == -1)
                {
                    bankAccount.setName(newName);
                    sendCreateBankAccountRequest();
                }
                else
                {
                    bankAccount.setName(newName);
                    sendModifyBankAccountRequest();
                }
            }
        });

        nameEditText = (ClearEditText) findViewById(R.id.nameEditText);
        String text = originalName.isEmpty() ? currentUser.getNickname() : originalName;
        nameEditText.setText(text);
        ViewUtils.requestFocus(this, nameEditText);

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
        imm.hideSoftInputFromWindow(nameEditText.getWindowToken(), 0);
    }

    private void goBack()
    {
        hideSoftKeyboard();
        ViewUtils.goBack(this);
    }

    // Data
    private void initData()
    {
        dbManager = DBManager.getDBManager();
        currentUser = AppPreference.getAppPreference().getCurrentUser();
        bankAccount = dbManager.getBankAccount(currentUser.getServerID());
        originalName = bankAccount != null && !bankAccount.getName().isEmpty() ? bankAccount.getName() : "";
    }

    // Network
    private void sendCreateBankAccountRequest()
    {
        ReimProgressDialog.show();
        CreateBankAccountRequest request = new CreateBankAccountRequest(bankAccount);
        request.sendRequest(new HttpConnectionCallback()
        {
            public void execute(Object httpResponse)
            {
                final CreateBankAccountResponse response = new CreateBankAccountResponse(httpResponse);
                if (response.getStatus())
                {
                    bankAccount.setServerID(response.getAccountID());
                    int localID = dbManager.insertBankAccount(bankAccount, currentUser.getServerID());
                    bankAccount.setLocalID(localID);

                    runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            ReimProgressDialog.dismiss();
                            ViewUtils.showToast(BankNameActivity.this, R.string.succeed_in_setting_account_name);
                            goBack();
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
                            ViewUtils.showToast(BankNameActivity.this, R.string.failed_to_set_account_name, response.getErrorMessage());
                        }
                    });
                }
            }
        });
    }

    private void sendModifyBankAccountRequest()
    {
        ReimProgressDialog.show();
        ModifyBankAccountRequest request = new ModifyBankAccountRequest(bankAccount);
        request.sendRequest(new HttpConnectionCallback()
        {
            public void execute(Object httpResponse)
            {
                ModifyBankAccountResponse response = new ModifyBankAccountResponse(httpResponse);
                if (response.getStatus())
                {
                    dbManager.updateBankAccount(bankAccount);

                    runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            ReimProgressDialog.dismiss();
                            ViewUtils.showToast(BankNameActivity.this, R.string.succeed_in_setting_account_name);
                            goBack();
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
                            ViewUtils.showToast(BankNameActivity.this, R.string.failed_to_set_account_name);
                        }
                    });
                }
            }
        });
    }
}
