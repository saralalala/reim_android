package com.rushucloud.reim.item;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.rushucloud.reim.R;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.List;

import classes.adapter.VendorListViewAdapter;
import classes.model.Vendor;
import classes.utils.Constant;
import classes.utils.PhoneUtils;
import classes.utils.ViewUtils;
import classes.widget.ClearEditText;
import classes.widget.ReimProgressDialog;
import netUtils.common.HttpConnectionCallback;
import netUtils.request.common.DownloadImageRequest;
import netUtils.request.item.GetVendorsRequest;
import netUtils.response.common.DownloadImageResponse;
import netUtils.response.item.GetVendorsResponse;

public class PickVendorActivity extends Activity
{
    // Widgets
    private VendorListViewAdapter vendorAdapter;
    private ClearEditText vendorEditText;

    // Local Data
    private String location;
    private double latitude;
    private double longitude;
    private List<Vendor> vendorList = new ArrayList<>();

    // View
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        setContentView(R.layout.activity_reim_vendor);
        initData();
        initView();
        getVendors();
    }

    protected void onResume()
    {
        super.onResume();
        MobclickAgent.onPageStart("PickVendorActivity");
        MobclickAgent.onResume(this);
    }

    protected void onPause()
    {
        super.onPause();
        MobclickAgent.onPageEnd("PickVendorActivity");
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

    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (resultCode == Activity.RESULT_OK)
        {
            switch (requestCode)
            {
                case Constant.ACTIVITY_INPUT_VENDOR:
                {
                    Intent intent = new Intent();
                    intent.putExtra("vendor", data.getStringExtra("vendor"));
                    ViewUtils.goBackWithResult(PickVendorActivity.this, intent);
                    break;
                }
                default:
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
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

        ImageView searchImageView = (ImageView) findViewById(R.id.searchImageView);
        searchImageView.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                searchVendors();
            }
        });

        TextView addTextView = (TextView) findViewById(R.id.addTextView);
        addTextView.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                hideSoftKeyboard();
                Intent intent = new Intent(PickVendorActivity.this, InputVendorActivity.class);
                ViewUtils.goForwardForResult(PickVendorActivity.this, intent, Constant.ACTIVITY_INPUT_VENDOR);
            }
        });

        vendorEditText = (ClearEditText) findViewById(R.id.vendorEditText);
        vendorEditText.setOnKeyListener(new View.OnKeyListener()
        {
            public boolean onKey(View v, int keyCode, KeyEvent event)
            {
                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_ENTER)
                {
                    searchVendors();
                }
                return false;
            }
        });

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
                intent.putExtra("latitude", vendor.getLatitude());
                intent.putExtra("longitude", vendor.getLongitude());
                ViewUtils.goBackWithResult(PickVendorActivity.this, intent);
            }
        });
    }

    private void hideSoftKeyboard()
    {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(vendorEditText.getWindowToken(), 0);
    }

    private void goBack()
    {
        hideSoftKeyboard();
        ViewUtils.goBack(this);
    }

    // Data
    private void initData()
    {
        location = getIntent().getStringExtra("location");
        latitude = getIntent().getDoubleExtra("latitude", -1);
        longitude = getIntent().getDoubleExtra("longitude", -1);
    }

    // Network
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
            sendVendorsRequest();
        }
    }

    private void searchVendors()
    {
        hideSoftKeyboard();
        if (!PhoneUtils.isNetworkConnected())
        {
            ViewUtils.showToast(PickVendorActivity.this, R.string.error_get_vendor_network_unavailable);
        }
        else if (vendorEditText.getText().toString().isEmpty())
        {
            ReimProgressDialog.show();
            sendVendorsRequest();
        }
        else if (!location.isEmpty())
        {
            sendVendorsRequest(vendorEditText.getText().toString());
        }
        else
        {
            ViewUtils.showToast(PickVendorActivity.this, R.string.no_city);
        }
    }

    private void sendVendorsRequest()
    {
        GetVendorsRequest request = new GetVendorsRequest(latitude, longitude);
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
                            vendorList.clear();
                            vendorList.addAll(response.getVendorList());

                            if (!vendorList.isEmpty())
                            {
                                vendorAdapter.setVendorList(vendorList);
                                vendorAdapter.setShowDistance(true);
                                vendorAdapter.notifyDataSetChanged();

                                for (int i = 0; i < vendorList.size(); i++)
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

    private void sendVendorsRequest(String keyword)
    {
        ReimProgressDialog.show();
        GetVendorsRequest request = new GetVendorsRequest(location, keyword);
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
                            ReimProgressDialog.dismiss();

                            vendorList.clear();
                            vendorList.addAll(response.getVendorList());

                            if (!vendorList.isEmpty())
                            {
                                vendorAdapter.setVendorList(vendorList);
                                vendorAdapter.setShowDistance(false);
                                vendorAdapter.notifyDataSetChanged();

                                for (int i = 0; i < vendorList.size(); i++)
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
}