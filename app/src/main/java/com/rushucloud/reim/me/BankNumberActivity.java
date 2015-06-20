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

import org.json.JSONObject;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;

import classes.model.BankAccount;
import classes.model.User;
import classes.utils.AppPreference;
import classes.utils.DBManager;
import classes.utils.PhoneUtils;
import classes.utils.Utils;
import classes.utils.ViewUtils;
import classes.widget.ClearEditText;
import classes.widget.ReimProgressDialog;
import netUtils.common.HttpConnectionCallback;
import netUtils.request.bank.CreateBankAccountRequest;
import netUtils.request.bank.DeleteBankAccountRequest;
import netUtils.request.bank.ModifyBankAccountRequest;
import netUtils.response.bank.CreateBankAccountResponse;
import netUtils.response.bank.DeleteBankAccountResponse;
import netUtils.response.bank.ModifyBankAccountResponse;

public class BankNumberActivity extends Activity
{
    // Widgets
    private ClearEditText bankEditText;

    // Local Data
    private DBManager dbManager;
    private User currentUser;
    private BankAccount bankAccount;
    private String originalAccount;
    private HashMap<String, String> codeMap = new HashMap<>();

    // View
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_me_bank_number);
        initData();
        initView();
    }

    protected void onResume()
    {
        super.onResume();
        MobclickAgent.onPageStart("BankNumberActivity");
        MobclickAgent.onResume(this);
        ReimProgressDialog.setContext(this);
    }

    protected void onPause()
    {
        super.onPause();
        MobclickAgent.onPageEnd("BankNumberActivity");
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

                String newBankAccount = bankEditText.getText().toString();
                if (!PhoneUtils.isNetworkConnected())
                {
                    ViewUtils.showToast(BankNumberActivity.this, R.string.error_modify_network_unavailable);
                }
                else if (newBankAccount.equals(originalAccount))
                {
                    goBack();
                }
                else if (!newBankAccount.isEmpty() && !Utils.isBankAccount(newBankAccount))
                {
                    ViewUtils.showToast(BankNumberActivity.this, R.string.error_bank_account_wrong_format);
                    ViewUtils.requestFocus(BankNumberActivity.this, bankEditText);
                }
                else if (bankAccount == null && !newBankAccount.isEmpty())
                {
                    bankAccount = new BankAccount();
                    setBankAccount(newBankAccount);
                    sendCreateBankAccountRequest();
                }
                else if (bankAccount != null && bankAccount.getServerID() == -1 && !newBankAccount.isEmpty())
                {
                    setBankAccount(newBankAccount);
                    sendCreateBankAccountRequest();
                }
                else if (bankAccount != null && !newBankAccount.isEmpty())
                {
                    setBankAccount(newBankAccount);
                    sendModifyBankAccountRequest();
                }
                else if (bankAccount != null)
                {
                    sendDeleteBankAccountRequest();
                }
            }
        });

        bankEditText = (ClearEditText) findViewById(R.id.bankEditText);
        bankEditText.setText(originalAccount);
        ViewUtils.requestFocus(this, bankEditText);

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
        imm.hideSoftInputFromWindow(bankEditText.getWindowToken(), 0);
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
        originalAccount = bankAccount == null ? "" : bankAccount.getNumber();

        try
        {
            codeMap.clear();

            InputStream inputStream = getResources().openRawResource(R.raw.bank_code);
            byte[] buffer = new byte[inputStream.available()];
            //noinspection ResultOfMethodCallIgnored
            inputStream.read(buffer);

            String json = new String(buffer, "GB2312");
            JSONObject jObject = new JSONObject(json);
            Iterator<?> iterator = jObject.keys();
            String key;
            while (iterator.hasNext())
            {
                key = iterator.next().toString();
                if (key != null && !key.isEmpty())
                {
                    codeMap.put(key, jObject.getString(key));
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void setBankAccount(String newBankAccount)
    {
        bankAccount.setNumber(newBankAccount);
        String bankName = codeMap.get(newBankAccount.substring(0, 6));
        if (bankName != null)
        {
            bankAccount.setBankName(bankName);
        }
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
                            ViewUtils.showToast(BankNumberActivity.this, R.string.succeed_in_setting_bank_account);
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
                            ViewUtils.showToast(BankNumberActivity.this, R.string.failed_to_set_bank_account, response.getErrorMessage());
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
                final ModifyBankAccountResponse response = new ModifyBankAccountResponse(httpResponse);
                if (response.getStatus())
                {
                    dbManager.updateBankAccount(bankAccount);

                    runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            ReimProgressDialog.dismiss();
                            ViewUtils.showToast(BankNumberActivity.this, R.string.succeed_in_setting_bank_account);
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
                            ViewUtils.showToast(BankNumberActivity.this, R.string.failed_to_set_bank_account, response.getErrorMessage());
                        }
                    });
                }
            }
        });
    }

    private void sendDeleteBankAccountRequest()
    {
        ReimProgressDialog.show();
        DeleteBankAccountRequest request = new DeleteBankAccountRequest(bankAccount.getServerID());
        request.sendRequest(new HttpConnectionCallback()
        {
            public void execute(Object httpResponse)
            {
                DeleteBankAccountResponse response = new DeleteBankAccountResponse(httpResponse);
                if (response.getStatus())
                {
                    dbManager.deleteBankAccount(currentUser.getServerID());

                    runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            ReimProgressDialog.dismiss();
                            ViewUtils.showToast(BankNumberActivity.this, R.string.succeed_in_setting_bank_account);
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
                            ViewUtils.showToast(BankNumberActivity.this, R.string.failed_to_set_bank_account);
                        }
                    });
                }
            }
        });
    }
}