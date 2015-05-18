package com.rushucloud.reim.me;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.rushucloud.reim.R;
import com.umeng.analytics.MobclickAgent;

import classes.model.BankAccount;
import classes.model.User;
import classes.utils.AppPreference;
import classes.utils.DBManager;
import classes.utils.ViewUtils;
import classes.widget.ReimProgressDialog;

public class BankActivity extends Activity
{
    private TextView nameTextView;
    private TextView numberTextView;
    private TextView bankNameTextView;
    private TextView locationTextView;

	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_me_bank);
		initView();
	}

	protected void onResume()
	{
		super.onResume();
		MobclickAgent.onPageStart("BankActivity");
		MobclickAgent.onResume(this);
		ReimProgressDialog.setContext(this);
        refreshInfo();
	}

	protected void onPause()
	{
		super.onPause();
		MobclickAgent.onPageEnd("BankActivity");
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

        nameTextView = (TextView) findViewById(R.id.nameTextView);
        numberTextView = (TextView) findViewById(R.id.numberTextView);
        bankNameTextView = (TextView) findViewById(R.id.bankNameTextView);
        locationTextView = (TextView) findViewById(R.id.locationTextView);

        LinearLayout nameLayout = (LinearLayout) findViewById(R.id.nameLayout);
        nameLayout.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                ViewUtils.goForward(BankActivity.this, EditCompanyActivity.class);
            }
        });

        LinearLayout numberLayout = (LinearLayout) findViewById(R.id.numberLayout);
        numberLayout.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                ViewUtils.goForward(BankActivity.this, BankNumberActivity.class);
            }
        });

        LinearLayout bankNameLayout = (LinearLayout) findViewById(R.id.bankNameLayout);
        bankNameLayout.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {

            }
        });

        LinearLayout locationLayout = (LinearLayout) findViewById(R.id.locationLayout);
        locationLayout.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {

            }
        });
	}

    private void refreshInfo()
    {
        User currentUser = AppPreference.getAppPreference().getCurrentUser();
        BankAccount bankAccount = DBManager.getDBManager().getBankAccount(currentUser.getServerID());

        nameTextView.setText(R.string.not_set);
        numberTextView.setText(R.string.not_binding);
        bankNameTextView.setText(R.string.not_set);
        locationTextView.setText(R.string.not_set);

        if (bankAccount != null)
        {
            if (!bankAccount.getName().isEmpty())
            {
                nameTextView.setText(bankAccount.getName());
            }
            else if (!currentUser.getNickname().isEmpty())
            {
                nameTextView.setText(currentUser.getNickname());
            }

            if (!bankAccount.getNumber().isEmpty())
            {
                numberTextView.setText(bankAccount.getNumber());
            }

            if (!bankAccount.getBankName().isEmpty())
            {
                bankNameTextView.setText(bankAccount.getBankName());
            }

            if (!bankAccount.getLocation().isEmpty())
            {
                locationTextView.setText(bankAccount.getLocation());
            }
        }
    }

    private void goBack()
    {
        ViewUtils.goBack(this);
    }
}