package com.rushucloud.reim.item;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.rushucloud.reim.R;
import com.umeng.analytics.MobclickAgent;

import java.util.List;

import classes.Vendor;
import classes.adapter.VendorListViewAdapter;
import classes.utils.PhoneUtils;
import classes.utils.ViewUtils;
import classes.widget.ReimProgressDialog;
import netUtils.HttpConnectionCallback;
import netUtils.Request.DownloadImageRequest;
import netUtils.Request.Item.GetVendorsRequest;
import netUtils.Response.DownloadImageResponse;
import netUtils.Response.Item.GetVendorsResponse;

public class PickVendorActivity extends Activity
{
	private VendorListViewAdapter vendorAdapter;
	private EditText vendorEditText;
	
	private String category;
	private double latitude;
	private double longitude;
	private List<Vendor> vendorList = null;
	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
		setContentView(R.layout.activity_reim_vendor);
		initData();
		initView();
	}

	protected void onResume()
	{
		super.onResume();
		MobclickAgent.onPageStart("PickVendorActivity");		
		MobclickAgent.onResume(this);
		getVendors();
	}

	protected void onPause()
	{
		super.onPause();
		MobclickAgent.onPageEnd("PickVendorActivity");
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
	
	private void initData()
	{
		category = getIntent().getStringExtra("category");
		latitude = getIntent().getDoubleExtra("latitude", -1);
		longitude = getIntent().getDoubleExtra("longitude", -1);
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
				
				Intent intent = new Intent();
				intent.putExtra("vendor", vendorEditText.getText().toString());
				setResult(RESULT_OK, intent);
				finish();
			}
		});
		
		vendorEditText = (EditText) findViewById(R.id.vendorEditText);
		vendorEditText.setText(getIntent().getStringExtra("vendor"));
		vendorEditText.setOnFocusChangeListener(ViewUtils.onFocusChangeListener);
		
		vendorAdapter = new VendorListViewAdapter(this);
		ListView vendorListView = (ListView) findViewById(R.id.vendorListView);
		vendorListView.setAdapter(vendorAdapter);
		vendorListView.setOnItemClickListener(new OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
                hideSoftKeyboard();

				Vendor vendor = vendorAdapter.getItem(position);				
				Intent intent = new Intent();
				intent.putExtra("vendor", vendor.getName());
				setResult(RESULT_OK, intent);
				finish();
			}
		});
	}
	
    private void hideSoftKeyboard()
    {
		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE); 
		imm.hideSoftInputFromWindow(vendorEditText.getWindowToken(), 0);
    }

    private void getVendors()
    {
    	if (!PhoneUtils.isNetworkConnected())
		{
			ViewUtils.showToast(PickVendorActivity.this, R.string.error_get_vendor_network_unavailable);
		}
		else if (latitude == -1)
		{
			ViewUtils.showToast(PickVendorActivity.this, R.string.failed_to_get_gps_info);
		}
		else
		{
			sendVendorsRequest(category, latitude, longitude);   	
		}
    }
    
	private void sendDownloadVendorImageRequest(int index)
	{
		final Vendor vendor = vendorList.get(index);
		DownloadImageRequest request = new DownloadImageRequest(vendor.getPhotoURL());
		request.sendRequest(new HttpConnectionCallback()
		{
			public void execute(Object httpResponse)
			{
				DownloadImageResponse response = new DownloadImageResponse(httpResponse);
				if (response.getBitmap() != null)
				{
					vendor.setPhoto(response.getBitmap());
					
					runOnUiThread(new Runnable()
					{
						public void run()
						{
							vendorAdapter.setVendorList(vendorList);
							vendorAdapter.notifyDataSetChanged();
						}
					});
				}
			}
		});
	}
	
    private void sendVendorsRequest(String category, double latitude, double longitude)
    {
		GetVendorsRequest request = new GetVendorsRequest(category, latitude, longitude);
		request.sendRequest(new HttpConnectionCallback()
		{
			public void execute(Object httpResponse)
			{
				final GetVendorsResponse response = new GetVendorsResponse(httpResponse);
				if (response.getStatus())
				{
					runOnUiThread(new Runnable()
					{
						public void run()
						{
							vendorList = response.getVendorList();
							
							if (!vendorList.isEmpty())
							{
								vendorAdapter.setVendorList(vendorList);
								vendorAdapter.notifyDataSetChanged();
								
								for (int i = 0 ; i < vendorList.size(); i++)
								{
									Vendor vendor = vendorList.get(i);
									if (vendor.getPhoto() == null && !vendor.getPhotoURL().isEmpty())
									{
										sendDownloadVendorImageRequest(i);
									}
								}
							}
							else 
							{
								ViewUtils.showToast(PickVendorActivity.this, R.string.failed_to_get_vendor_no_data);								
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
							ViewUtils.showToast(PickVendorActivity.this, R.string.failed_to_get_vendor);
						}
					});					
				}
			}
		});
    }
}