package com.rushucloud.reim.item;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.rushucloud.reim.R;
import com.rushucloud.reim.me.BindDidiActivity;
import com.umeng.analytics.MobclickAgent;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import classes.adapter.UberExpenseListViewAdapter;
import classes.model.UberExpense;
import classes.model.User;
import classes.utils.AppPreference;
import classes.utils.Constant;
import classes.utils.DBManager;
import classes.utils.PhoneUtils;
import classes.utils.Utils;
import classes.utils.ViewUtils;
import classes.widget.ReimProgressDialog;
import classes.widget.XListView;
import cn.beecloud.BCLocation;
import cn.beecloud.BeeCloud;
import cn.beecloud.async.BCAddressCallback;
import cn.beecloud.async.BCAddressResult;
import netUtils.common.HttpConnectionCallback;
import netUtils.request.item.UberHistoryRequest;
import netUtils.request.item.UberProductRequest;
import netUtils.request.user.UnbindDidiRequest;
import netUtils.response.item.UberHistoryResponse;
import netUtils.response.item.UberProductResponse;
import netUtils.response.user.UnbindDidiResponse;

public class UberExpenseActivity extends Activity
{
    // Widgets
    private TextView expenseTextView;
    private XListView expenseListView;
    private UberExpenseListViewAdapter adapter;

    // Local Data
    private List<UberExpense> expenseList = new ArrayList<>();
    private ArrayList<Integer> importedList = new ArrayList<>();
    private String token = "";
    private boolean needToGetData = true;

    // View
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reim_uber_expenses);
        initData();
        initView();
        initBeeCloud();
    }

    protected void onResume()
    {
        super.onResume();
        MobclickAgent.onPageStart("DidiExpenseActivity");
        MobclickAgent.onResume(this);
        ReimProgressDialog.setContext(this);
        if (PhoneUtils.isNetworkConnected() && needToGetData)
        {
            ReimProgressDialog.show();
            sendUberHistoryRequest();
        }
        else if (needToGetData)
        {
            ViewUtils.showToast(UberExpenseActivity.this, R.string.error_get_data_network_unavailable);
        }
    }

    protected void onPause()
    {
        super.onPause();
        MobclickAgent.onPageEnd("MessageListActivity");
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
                case Constant.ACTIVITY_IMPORT_UBER:
                {
                    int id = data.getIntExtra("uberID", -1);
                    if (id > 0 && !importedList.contains(id))
                    {
                        importedList.add(id);
                    }
                    adapter.setImportedList(importedList);
                    adapter.notifyDataSetChanged();
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
        backImageView.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                goBack();
            }
        });

        TextView unbindTextView = (TextView) findViewById(R.id.unbindTextView);
        unbindTextView.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                sendUnbindUberRequest();
            }
        });

        expenseTextView = (TextView) findViewById(R.id.expenseTextView);

        adapter = new UberExpenseListViewAdapter(UberExpenseActivity.this, expenseList);
        expenseListView = (XListView) findViewById(R.id.expenseListView);
        expenseListView.setAdapter(adapter);
        expenseListView.setXListViewListener(new XListView.IXListViewListener()
        {
            public void onRefresh()
            {
                if (PhoneUtils.isNetworkConnected())
                {
                    sendUberHistoryRequest();
                }
                else
                {
                    ViewUtils.showToast(UberExpenseActivity.this, R.string.error_get_data_network_unavailable);
                    expenseListView.stopRefresh();
                }
            }

            public void onLoadMore()
            {

            }
        });
        expenseListView.setPullRefreshEnable(true);
        expenseListView.setPullLoadEnable(false);
        expenseListView.setRefreshTime(Utils.secondToStringUpToMinute(Utils.getCurrentTime()));
        expenseListView.setOnItemClickListener(new OnItemClickListener()
        {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                if (position > 0)
                {
                    Intent intent = new Intent(UberExpenseActivity.this, EditItemActivity.class);
                    intent.putExtra("fromDidi", true);
                    intent.putExtra("expense", expenseList.get(position - 1));
                    ViewUtils.goForwardForResult(UberExpenseActivity.this, intent, Constant.ACTIVITY_IMPORT_DIDI);
                }
            }
        });

        int visibility = expenseList.isEmpty() ? View.VISIBLE : View.GONE;
        expenseTextView.setVisibility(visibility);

        if (!needToGetData)
        {
            getUberProducts();
        }
    }

    private void goBack()
    {
        ViewUtils.goBack(this);
    }

    // Data
    @SuppressWarnings("unchecked")
    private void initData()
    {
        Serializable serializable = getIntent().getSerializableExtra("expenseList");
        if (serializable != null)
        {
            expenseList.addAll((List<UberExpense>) serializable);
            needToGetData = false;
        }

        token = AppPreference.getAppPreference().getCurrentUser().getDidiToken();
    }

    // Network
    private void initBeeCloud()
    {
        BeeCloud.setAppIdAndSecret(this, "0f4179d0-b80d-45c6-9ca1-7ad46f4c9402", "9e4a4a76-e771-4d75-aa52-4ff7c15df658");
    }

    private void getUberProducts()
    {
        if (PhoneUtils.isNetworkConnected())
        {
            for (UberExpense expense : expenseList)
            {
                queryLocation(expense);
                sendUberProductRequest(expense);
            }
        }
    }

    private void sendUberHistoryRequest()
    {
        UberHistoryRequest request = new UberHistoryRequest();
        request.sendRequest(new HttpConnectionCallback()
        {
            public void execute(Object httpResponse)
            {
                final UberHistoryResponse response = new UberHistoryResponse(httpResponse);
                if (response.getStatus())
                {
                    needToGetData = false;

                    expenseList.clear();
                    expenseList.addAll(response.getUberExpenseList());

                    getUberProducts();

                    runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            ReimProgressDialog.dismiss();
                            adapter.setExpenseList(expenseList);
                            adapter.notifyDataSetChanged();

                            int visibility = expenseList.isEmpty() ? View.VISIBLE : View.GONE;
                            expenseTextView.setVisibility(visibility);

                            expenseListView.stopRefresh();
                            expenseListView.setRefreshTime(Utils.secondToStringUpToMinute(Utils.getCurrentTime()));
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
                            ViewUtils.showToast(UberExpenseActivity.this, R.string.failed_to_get_uber_expenses, response.getErrorMessage());
                            expenseListView.stopRefresh();
                        }
                    });
                }
            }
        });
    }

    private void sendUberProductRequest(final UberExpense expense)
    {
        UberProductRequest request = new UberProductRequest(token, expense.getProductID());
        request.sendRequest(new HttpConnectionCallback()
        {
            public void execute(Object httpResponse)
            {
                UberProductResponse response = new UberProductResponse(httpResponse);
                if (response.getStatus())
                {
                    expense.setAmount(response.getAmount());

                    runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            adapter.notifyDataSetChanged();
                        }
                    });
                }
            }
        });
    }

    private void sendUnbindUberRequest()
    {
        ReimProgressDialog.show();
        UnbindDidiRequest request = new UnbindDidiRequest();
        request.sendRequest(new HttpConnectionCallback()
        {
            public void execute(Object httpResponse)
            {
                final UnbindDidiResponse response = new UnbindDidiResponse(httpResponse);
                runOnUiThread(new Runnable()
                {
                    public void run()
                    {
                        ReimProgressDialog.dismiss();
                        if (response.getStatus())
                        {
                            User currentUser = AppPreference.getAppPreference().getCurrentUser();
                            currentUser.setDidi("");
                            currentUser.setDidiToken("");
                            DBManager.getDBManager().updateUser(currentUser);

                            ViewUtils.showToast(UberExpenseActivity.this, R.string.succeed_in_unbinding_didi);
                            ViewUtils.goBackWithIntent(UberExpenseActivity.this, BindDidiActivity.class);
                        }
                        else
                        {
                            ViewUtils.showToast(UberExpenseActivity.this, R.string.failed_to_unbind_didi, response.getErrorMessage());
                        }
                    }
                });
            }
        });
    }

    private void queryLocation(final UberExpense expense)
    {
        BCLocation location = BCLocation.locationWithLatitude(expense.getStartLatitude(), expense.getStartLongitude());
        location.getAddressAsync(new BCAddressCallback()
        {
            public void done(BCAddressResult bcAddressResult)
            {
                expense.setStart(bcAddressResult.getFormattedAddress());

                runOnUiThread(new Runnable()
                {
                    public void run()
                    {
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        });
    }
}