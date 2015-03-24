package com.rushucloud.reim.item;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.rushucloud.reim.R;
import com.umeng.analytics.MobclickAgent;

import classes.utils.ViewUtils;

public class InputVendorActivity extends Activity
{
	private EditText vendorEditText;
	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
		setContentView(R.layout.activity_reim_input_vendor);
		initView();
	}

	protected void onResume()
	{
		super.onResume();
		MobclickAgent.onPageStart("InputVendorActivity");
		MobclickAgent.onResume(this);
	}

	protected void onPause()
	{
		super.onPause();
		MobclickAgent.onPageEnd("InputVendorActivity");
		MobclickAgent.onPause(this);
	}
	
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if (keyCode == KeyEvent.KEYCODE_BACK)
		{
			finish();
		}
		return super.onKeyDown(keyCode, event);
	}
	
	private void initView()
	{		
		getActionBar().hide();
		
		ImageView backImageView = (ImageView) findViewById(R.id.backImageView);
		backImageView.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
                hideSoftKeyboard();
				finish();
			}
		});
		
		TextView confirmTextView = (TextView) findViewById(R.id.confirmTextView);
		confirmTextView.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				hideSoftKeyboard();
                if (!vendorEditText.getText().toString().isEmpty())
                {
                    Intent intent = new Intent();
                    intent.putExtra("vendor", vendorEditText.getText().toString());
                    setResult(RESULT_OK, intent);
                }
				finish();
			}
		});
		
		vendorEditText = (EditText) findViewById(R.id.vendorEditText);
		vendorEditText.setOnFocusChangeListener(ViewUtils.onFocusChangeListener);
        vendorEditText.postDelayed(new Runnable()
        {
            public void run()
            {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(vendorEditText, 0);
            }
        }, 200);

        LinearLayout baseLayout = (LinearLayout) findViewById(R.id.baseLayout);
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
		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE); 
		imm.hideSoftInputFromWindow(vendorEditText.getWindowToken(), 0);
    }
}