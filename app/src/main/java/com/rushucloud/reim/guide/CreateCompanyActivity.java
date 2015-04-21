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

import classes.User;
import classes.utils.ViewUtils;
import classes.widget.ClearEditText;

public class CreateCompanyActivity extends Activity
{
	private ClearEditText companyEditText;

    private String companyName;
    private ArrayList<String> inputList;
    private ArrayList<String> inputChosenList = new ArrayList<String>();
    private List<User> contactChosenList;

	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_guide_create_company);
		initData();
		initView();
	}

	protected void onResume()
	{
		super.onResume();
		MobclickAgent.onPageStart("CreateCompanyActivity");
		MobclickAgent.onResume(this);
	}

	protected void onPause()
	{
		super.onPause();
		MobclickAgent.onPageEnd("CreateCompanyActivity");
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
        Bundle bundle = getIntent().getExtras();
        companyName = bundle.getString("companyName", "");
        inputList = bundle.getStringArrayList("inputList");
        inputChosenList = bundle.getStringArrayList("inputChosenList");
        contactChosenList = (List<User>) bundle.getSerializable("contactChosenList");
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

				companyName = companyEditText.getText().toString();
                if (companyName.isEmpty())
				{
					ViewUtils.showToast(CreateCompanyActivity.this, R.string.error_company_name_empty);
				}
				else
				{
                    Bundle bundle = new Bundle();
                    bundle.putString("companyName", companyName);
                    bundle.putStringArrayList("inputList", inputList);
                    bundle.putStringArrayList("inputChosenList", inputChosenList);
                    bundle.putSerializable("contactChosenList", (Serializable) contactChosenList);
                    Intent intent = new Intent(CreateCompanyActivity.this, InviteContactActivity.class);
                    intent.putExtras(bundle);
                    ViewUtils.goForwardAndFinish(CreateCompanyActivity.this, intent);
				}
			}
		});
		
		companyEditText = (ClearEditText) findViewById(R.id.companyEditText);
        companyEditText.setText(companyName);
        ViewUtils.requestFocus(this, companyEditText);

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
		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE); 
		imm.hideSoftInputFromWindow(companyEditText.getWindowToken(), 0);
	}

    private void goBack()
    {
        hideSoftKeyboard();
        ViewUtils.goBackWithIntent(this, GuideStartActivity.class);
    }
}