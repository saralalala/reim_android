package com.rushucloud.reim.guide;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import classes.model.User;
import classes.utils.AppPreference;
import classes.utils.DBManager;
import classes.utils.PhoneUtils;
import classes.utils.ViewUtils;
import classes.widget.ClearEditText;
import classes.widget.ReimProgressDialog;
import netUtils.HttpConnectionCallback;
import netUtils.request.user.ModifyUserRequest;
import netUtils.response.user.ModifyUserResponse;

public class SetNicknameActivity extends Activity
{
    private ClearEditText nicknameEditText;

    private User currentUser;
    private boolean join;
    private String nickname;
    private String companyName;
    private ArrayList<String> inputList = new ArrayList<>();
    private ArrayList<String> inputChosenList = new ArrayList<>();
    private List<User> contactChosenList = new ArrayList<>();

    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide_set_nickname);
        initData();
        initView();
    }

    protected void onResume()
    {
        super.onResume();
        MobclickAgent.onPageStart("SetNicknameActivity");
        MobclickAgent.onResume(this);
        ReimProgressDialog.setContext(this);
    }

    protected void onPause()
    {
        super.onPause();
        MobclickAgent.onPageEnd("SetNicknameActivity");
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

    @SuppressWarnings("unchecked")
    private void initData()
    {
        currentUser = AppPreference.getAppPreference().getCurrentUser();
        join = getIntent().getBooleanExtra("join", false);
        if (join)
        {
            nickname = getIntent().getStringExtra("nickname");
        }
        else
        {
            Bundle bundle = getIntent().getExtras();
            nickname = bundle.getString("nickname", currentUser.getNickname());
            companyName = bundle.getString("companyName", "");
            inputList = bundle.getStringArrayList("inputList");
            inputChosenList = bundle.getStringArrayList("inputChosenList");
            contactChosenList = (List<User>) bundle.getSerializable("contactChosenList");
        }
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

        TextView nextTextView = (TextView) findViewById(R.id.nextTextView);
        nextTextView.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                hideSoftKeyboard();

                nickname = nicknameEditText.getText().toString();
                if (!PhoneUtils.isNetworkConnected())
                {
                    ViewUtils.showToast(SetNicknameActivity.this, R.string.error_modify_network_unavailable);
                }
                else if (nickname.isEmpty())
                {
                    ViewUtils.showToast(SetNicknameActivity.this, R.string.error_new_nickname_empty);
                }
                else
                {
                    currentUser.setNickname(nickname);
                    sendModifyUserInfoRequest();
                }
            }
        });

        nicknameEditText = (ClearEditText) findViewById(R.id.nicknameEditText);
        nicknameEditText.setText(nickname);
        ViewUtils.requestFocus(this, nicknameEditText);

        LinearLayout baseLayout = (LinearLayout) findViewById(R.id.baseLayout);
        baseLayout.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                hideSoftKeyboard();
            }
        });
    }

    private void sendModifyUserInfoRequest()
    {
        ReimProgressDialog.show();
        ModifyUserRequest request = new ModifyUserRequest(currentUser);
        request.sendRequest(new HttpConnectionCallback()
        {
            public void execute(Object httpResponse)
            {
                final ModifyUserResponse response = new ModifyUserResponse(httpResponse);
                if (response.getStatus())
                {
                    DBManager.getDBManager().updateUser(currentUser);

                    runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            ReimProgressDialog.dismiss();
                            ViewUtils.showToast(SetNicknameActivity.this, R.string.succeed_in_modifying_user_info);
                            if (join)
                            {
                                Intent intent = new Intent(SetNicknameActivity.this, PickCompanyActivity.class);
                                intent.putExtra("fromGuide", true);
                                ViewUtils.goForwardAndFinish(SetNicknameActivity.this, intent);
                            }
                            else
                            {
                                Bundle bundle = new Bundle();
                                bundle.putString("companyName", companyName);
                                bundle.putStringArrayList("inputList", inputList);
                                bundle.putStringArrayList("inputChosenList", inputChosenList);
                                bundle.putSerializable("contactChosenList", (Serializable) contactChosenList);
                                Intent intent = new Intent(SetNicknameActivity.this, CompanyNameActivity.class);
                                intent.putExtras(bundle);
                                ViewUtils.goForwardAndFinish(SetNicknameActivity.this, intent);
                            }
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
                            ViewUtils.showToast(SetNicknameActivity.this, R.string.failed_to_modify_user_info, response.getErrorMessage());
                        }
                    });
                }
            }
        });
    }

    private void hideSoftKeyboard()
    {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(nicknameEditText.getWindowToken(), 0);
    }

    private void goBack()
    {
        hideSoftKeyboard();
        ViewUtils.goBackWithIntent(this, GuideStartActivity.class);
    }
}