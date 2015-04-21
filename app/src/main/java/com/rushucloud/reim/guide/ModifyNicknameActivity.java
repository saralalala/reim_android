package com.rushucloud.reim.guide;

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

import classes.User;
import classes.utils.AppPreference;
import classes.utils.DBManager;
import classes.utils.PhoneUtils;
import classes.utils.ViewUtils;
import classes.widget.ClearEditText;
import classes.widget.ReimProgressDialog;
import netUtils.HttpConnectionCallback;
import netUtils.request.user.ModifyUserRequest;
import netUtils.response.user.ModifyUserResponse;

public class ModifyNicknameActivity extends Activity
{
	private ClearEditText nicknameEditText;

    private User currentUser;
    private String nickname;

	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_guide_change_nickname);
		initData();
		initView();
	}

	protected void onResume()
	{
		super.onResume();
		MobclickAgent.onPageStart("ModifyNicknameActivity");
		MobclickAgent.onResume(this);
        ReimProgressDialog.setContext(this);
	}

	protected void onPause()
	{
		super.onPause();
		MobclickAgent.onPageEnd("ModifyNicknameActivity");
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

	private void initData()
	{
        currentUser = AppPreference.getAppPreference().getCurrentUser();
        nickname = getIntent().getStringExtra("nickname");
	}

	private void initView()
	{
		getActionBar().hide();

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
                    ViewUtils.showToast(ModifyNicknameActivity.this, R.string.error_modify_network_unavailable);
                }
                else if (nickname.isEmpty())
                {
                    ViewUtils.showToast(ModifyNicknameActivity.this, R.string.error_new_nickname_empty);
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
                            ViewUtils.showToast(ModifyNicknameActivity.this, R.string.succeed_in_modifying_user_info);
                            ViewUtils.goForwardAndFinish(ModifyNicknameActivity.this, PickCompanyActivity.class);
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
                            ViewUtils.showToast(ModifyNicknameActivity.this, R.string.failed_to_modify_user_info, response.getErrorMessage());
                        }
                    });
                }
            }
        });
    }

    private void hideSoftKeyboard()
	{
		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE); 
		imm.hideSoftInputFromWindow(nicknameEditText.getWindowToken(), 0);
	}

    private void goBack()
    {
        hideSoftKeyboard();
        ViewUtils.goBackWithIntent(this, GuideStartActivity.class);
    }
}