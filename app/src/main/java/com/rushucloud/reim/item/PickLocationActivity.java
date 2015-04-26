package com.rushucloud.reim.item;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.rushucloud.reim.R;
import com.umeng.analytics.MobclickAgent;

import classes.adapter.LocationListViewAdapter;
import classes.utils.ViewUtils;
import classes.widget.ClearEditText;

public class PickLocationActivity extends Activity
{
	private LocationListViewAdapter locationAdapter;
	private ClearEditText locationEditText;

	private String location;
	private String currentCity;
	private boolean[] locationCheck;
	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_reim_location);
		initData();
		initView();
	}

	protected void onResume()
	{
		super.onResume();
		MobclickAgent.onPageStart("PickLocationActivity");		
		MobclickAgent.onResume(this);
	}

	protected void onPause()
	{
		super.onPause();
		MobclickAgent.onPageEnd("PickLocationActivity");
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
		location = getIntent().getStringExtra("location");
		currentCity = getIntent().getStringExtra("currentCity");
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
		
		TextView confirmTextView = (TextView) findViewById(R.id.confirmTextView);
		confirmTextView.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				hideSoftKeyboard();
				
				Intent intent = new Intent();
				intent.putExtra("location", locationEditText.getText().toString());
                ViewUtils.goBackWithResult(PickLocationActivity.this, intent);
			}
		});
		
		locationEditText = (ClearEditText) findViewById(R.id.locationEditText);
    	locationEditText.setText(location);

		locationAdapter = new LocationListViewAdapter(this, location, currentCity);
		locationCheck = locationAdapter.getCheck();
		
		ListView locationListView = (ListView) findViewById(R.id.locationListView);
		locationListView.setAdapter(locationAdapter);
		locationListView.setOnItemClickListener(new OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
                hideSoftKeyboard();

				if (position == 0 && !currentCity.isEmpty())
				{
					locationEditText.setText(currentCity);
				}
				else if (position > 1)
				{
					for (int i = 0; i < locationCheck.length; i++)
					{
						locationCheck[i] = false;
					}
					
					locationEditText.setText(locationAdapter.getCityList().get(position - 2));
					locationCheck[position - 2] = true;
					locationAdapter.setCheck(locationCheck);
					locationAdapter.notifyDataSetChanged();
				}
			}
		});
	}
	
    private void hideSoftKeyboard()
    {
		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE); 
		imm.hideSoftInputFromWindow(locationEditText.getWindowToken(), 0);
    }

    private void goBack()
    {
        hideSoftKeyboard();
        ViewUtils.goBack(this);
    }
}