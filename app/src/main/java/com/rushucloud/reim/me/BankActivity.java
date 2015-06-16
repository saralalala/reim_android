package com.rushucloud.reim.me;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.rushucloud.reim.R;
import com.umeng.analytics.MobclickAgent;

import org.json.JSONArray;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import classes.model.BankAccount;
import classes.model.Province;
import classes.model.User;
import classes.utils.AppPreference;
import classes.utils.DBManager;
import classes.utils.PhoneUtils;
import classes.utils.ViewUtils;
import classes.widget.ReimProgressDialog;
import classes.widget.wheelview.OnWheelChangedListener;
import classes.widget.wheelview.WheelView;
import classes.widget.wheelview.adapter.ArrayWheelAdapter;
import netUtils.common.HttpConnectionCallback;
import netUtils.request.bank.CreateBankAccountRequest;
import netUtils.request.bank.ModifyBankAccountRequest;
import netUtils.response.bank.CreateBankAccountResponse;
import netUtils.response.bank.ModifyBankAccountResponse;

public class BankActivity extends Activity
{
    // Widgets
    private TextView nameTextView;
    private TextView numberTextView;
    private TextView bankNameTextView;
    private TextView locationTextView;
    private PopupWindow bankPopupWindow;
    private PopupWindow locationPopupWindow;
    private WheelView provinceWheelView;
    private WheelView cityWheelView;

    // Local Data
    private DBManager dbManager;
    private User currentUser;
    private BankAccount bankAccount;

    private String originalBankName;
    private List<String> bankList;

    private String originalLocation;
    protected Province currentProvince;
    protected String currentCity;
    private List<Province> provinceList = new ArrayList<>();

    // View
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_me_bank);
        initData();
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

    private void initData()
    {
        dbManager = DBManager.getDBManager();
        currentUser = AppPreference.getAppPreference().getCurrentUser();
        bankAccount = DBManager.getDBManager().getBankAccount(currentUser.getServerID());
        originalBankName = bankAccount == null ? "" : bankAccount.getBankName();
        originalLocation = bankAccount == null ? "" : bankAccount.getLocation();
        bankList = Arrays.asList(getResources().getStringArray(R.array.bankArray));

        try
        {
            provinceList.clear();

            InputStream inputStream = getResources().openRawResource(R.raw.province);
            byte[] buffer = new byte[inputStream.available()];
            //noinspection ResultOfMethodCallIgnored
            inputStream.read(buffer);

            String json = new String(buffer, "GB2312");
            JSONArray jsonArray = new JSONArray(json);
            for (int i = 0; i < jsonArray.length(); i++)
            {
                provinceList.add(new Province(jsonArray.getJSONObject(i)));
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        getLocationInfo();
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
                ViewUtils.goForward(BankActivity.this, BankNameActivity.class);
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
                showBankWindow();
            }
        });

        initBankWindow();

        LinearLayout locationLayout = (LinearLayout) findViewById(R.id.locationLayout);
        locationLayout.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                showLocationWindow();
            }
        });

        initLocationWindow();
    }

    private void initBankWindow()
    {
        final View bankView = View.inflate(this, R.layout.window_me_bank_name, null);

        final WheelView bankWheelView = (WheelView) bankView.findViewById(R.id.bankWheelView);
        bankWheelView.setVisibleItems(11);
        bankWheelView.setViewAdapter(new ArrayWheelAdapter<>(this, bankList.toArray(new String[bankList.size()])));
        if (bankAccount != null && !bankAccount.getBankName().isEmpty())
        {
            int index = bankList.indexOf(bankAccount.getBankName());
            if (index >= 0)
            {
                bankWheelView.setCurrentItem(index);
            }
        }

        Button confirmButton = (Button) bankView.findViewById(R.id.confirmButton);
        confirmButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                String newBankName = bankList.get(bankWheelView.getCurrentItem());
                if (!PhoneUtils.isNetworkConnected())
                {
                    ViewUtils.showToast(BankActivity.this, R.string.error_modify_network_unavailable);
                }
                else if (newBankName.equals(originalBankName))
                {
                    bankPopupWindow.dismiss();
                }
                else if (bankAccount == null)
                {
                    bankAccount = new BankAccount();
                    bankAccount.setBankName(newBankName);
                    sendCreateBankAccountRequest(true);
                }
                else if (bankAccount.getServerID() == -1)
                {
                    bankAccount.setBankName(newBankName);
                    sendCreateBankAccountRequest(true);
                }
                else
                {
                    bankAccount.setBankName(newBankName);
                    sendModifyBankAccountRequest(true);
                }
            }
        });

        bankPopupWindow = ViewUtils.buildBottomPopupWindow(this, bankView);
    }

    private void initLocationWindow()
    {
        final View locationView = View.inflate(this, R.layout.window_me_bank_location, null);

        provinceWheelView = (WheelView) locationView.findViewById(R.id.provinceWheelView);
        provinceWheelView.addChangingListener(new OnWheelChangedListener()
        {
            public void onChanged(WheelView wheel, int oldValue, int newValue)
            {
                updateCurrentProvince(newValue);
            }
        });
        provinceWheelView.setViewAdapter(new ArrayWheelAdapter<>(this, Province.getProvinceArray(provinceList)));
        provinceWheelView.setVisibleItems(11);

        cityWheelView = (WheelView) locationView.findViewById(R.id.cityWheelView);
        cityWheelView.addChangingListener(new OnWheelChangedListener()
        {
            public void onChanged(WheelView wheel, int oldValue, int newValue)
            {
                updateCurrentCity(newValue);
            }
        });
        cityWheelView.setVisibleItems(11);

        updateCurrentProvince(0);

        Button confirmButton = (Button) locationView.findViewById(R.id.confirmButton);
        confirmButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                String location = currentProvince.getName().equals(currentCity) ? currentCity : currentProvince.getName() + currentCity;
                if (!PhoneUtils.isNetworkConnected())
                {
                    ViewUtils.showToast(BankActivity.this, R.string.error_modify_network_unavailable);
                }
                else if (location.equals(originalLocation))
                {
                    locationPopupWindow.dismiss();
                }
                else if (bankAccount == null)
                {
                    bankAccount = new BankAccount();
                    bankAccount.setLocation(location);
                    sendCreateBankAccountRequest(false);
                }
                else if (bankAccount.getServerID() == -1)
                {
                    bankAccount.setLocation(location);
                    sendCreateBankAccountRequest(false);
                }
                else
                {
                    bankAccount.setLocation(location);
                    sendModifyBankAccountRequest(false);
                }
            }
        });

        locationPopupWindow = ViewUtils.buildBottomPopupWindow(this, locationView);
    }

    private void showBankWindow()
    {
        bankPopupWindow.showAtLocation(findViewById(R.id.baseLayout), Gravity.BOTTOM, 0, 0);
        bankPopupWindow.update();

        ViewUtils.dimBackground(this);
    }

    private void showLocationWindow()
    {
        locationPopupWindow.showAtLocation(findViewById(R.id.baseLayout), Gravity.BOTTOM, 0, 0);
        locationPopupWindow.update();

        ViewUtils.dimBackground(this);
    }

    private void refreshInfo()
    {
        currentUser = AppPreference.getAppPreference().getCurrentUser();
        bankAccount = DBManager.getDBManager().getBankAccount(currentUser.getServerID());
        originalBankName = bankAccount == null ? "" : bankAccount.getBankName();
        originalLocation = bankAccount == null ? "" : bankAccount.getLocation();

        getLocationInfo();

        provinceWheelView.setCurrentItem(provinceList.indexOf(currentProvince));
        cityWheelView.setCurrentItem(currentProvince.getCityList().indexOf(currentCity));

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

    private void getLocationInfo()
    {
        String currentLocation = bankAccount == null ? "" : bankAccount.getLocation();
        if (currentLocation.length() == 3)
        {
            Province province = new Province();
            province.setName(currentLocation);
            int index = provinceList.indexOf(province);
            currentProvince = provinceList.get(index);
            currentCity = currentProvince.getCityList().get(0);
        }
        else if (!currentLocation.isEmpty())
        {
            int index = currentLocation.indexOf(getString(R.string.province));
            if (index >= 0)
            {
                Province province = new Province();
                province.setName(currentLocation.substring(0, index + 1));

                String city = currentLocation.substring(index + 1);

                index = provinceList.indexOf(province);
                index = index >= 0 ? index : 0;
                currentProvince = provinceList.get(index);

                index = currentProvince.getCityList().indexOf(city);
                index = index >= 0 ? index : 0;
                currentCity = currentProvince.getCityList().get(index);
            }
            else
            {
                index = currentLocation.indexOf(getString(R.string.region));
                Province province = new Province();
                province.setName(currentLocation.substring(0, index + 3));

                String city = currentLocation.substring(index + 3);

                index = provinceList.indexOf(province);
                index = index >= 0 ? index : 0;
                currentProvince = provinceList.get(index);

                index = currentProvince.getCityList().indexOf(city);
                index = index >= 0 ? index : 0;
                currentCity = currentProvince.getCityList().get(index);
            }
        }
        else
        {
            currentProvince = provinceList.get(0);
            currentCity = currentProvince.getCityList().get(0);
        }
    }

    private void updateCurrentProvince(int index)
    {
        currentProvince = provinceList.get(index);
        cityWheelView.setViewAdapter(new ArrayWheelAdapter<>(this, currentProvince.getCityArray()));
        cityWheelView.setCurrentItem(0);
        currentCity = currentProvince.getCityArray()[0];
    }

    private void updateCurrentCity(int index)
    {
        currentCity = currentProvince.getCityArray()[index];
    }

    private void sendCreateBankAccountRequest(final boolean setBankName)
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
                            if (setBankName)
                            {
                                ViewUtils.showToast(BankActivity.this, R.string.succeed_in_setting_bank_name);
                                bankPopupWindow.dismiss();
                            }
                            else
                            {
                                ViewUtils.showToast(BankActivity.this, R.string.succeed_in_setting_bank_location);
                                locationPopupWindow.dismiss();
                            }
                            refreshInfo();
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
                            int prompt = setBankName ? R.string.failed_to_set_bank_name : R.string.failed_to_set_bank_location;
                            ViewUtils.showToast(BankActivity.this, prompt, response.getErrorMessage());
                        }
                    });
                }
            }
        });
    }

    private void sendModifyBankAccountRequest(final boolean setBankName)
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
                            if (setBankName)
                            {
                                ViewUtils.showToast(BankActivity.this, R.string.succeed_in_setting_bank_name);
                                bankPopupWindow.dismiss();
                            }
                            else
                            {
                                ViewUtils.showToast(BankActivity.this, R.string.succeed_in_setting_bank_location);
                                locationPopupWindow.dismiss();
                            }
                            refreshInfo();
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
                            int prompt = setBankName ? R.string.failed_to_set_bank_name : R.string.failed_to_set_bank_location;
                            ViewUtils.showToast(BankActivity.this, prompt, response.getErrorMessage());
                        }
                    });
                }
            }
        });
    }

    private void goBack()
    {
        ViewUtils.goBack(this);
    }
}