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

import classes.adapter.DidiExpenseListViewAdapter;
import classes.model.DidiExpense;
import classes.model.User;
import classes.utils.AppPreference;
import classes.utils.DBManager;
import classes.utils.PhoneUtils;
import classes.utils.Utils;
import classes.utils.ViewUtils;
import classes.widget.ReimProgressDialog;
import classes.widget.XListView;
import cn.beecloud.BCLocation;
import cn.beecloud.BeeCloud;
import netUtils.common.HttpConnectionCallback;
import netUtils.request.item.DidiDetailKuaiCheRequest;
import netUtils.request.item.DidiDetailLiftRequest;
import netUtils.request.item.DidiDetailTaxiRequest;
import netUtils.request.item.DidiDetailZhuanCheRequest;
import netUtils.request.item.DidiOrdersRequest;
import netUtils.request.user.UnbindDidiRequest;
import netUtils.response.item.DidiDetailKuaiCheResponse;
import netUtils.response.item.DidiDetailLiftResponse;
import netUtils.response.item.DidiDetailTaxiResponse;
import netUtils.response.item.DidiDetailZhuanCheResponse;
import netUtils.response.item.DidiOrdersResponse;
import netUtils.response.user.UnbindDidiResponse;

public class DidiExpenseActivity extends Activity
{
    // Widgets
    private TextView expenseTextView;
    private XListView expenseListView;
    private DidiExpenseListViewAdapter adapter;

    // Local Data
    private List<DidiExpense> expenseList = new ArrayList<>();
    private String token = "";
    private boolean needToGetData = true;
    private int pageIndex = 0;

    // View
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_me_didi_expenses);
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
            sendDidiOrdersRequest();
        }
        else if (needToGetData)
        {
            ViewUtils.showToast(DidiExpenseActivity.this, R.string.error_get_data_network_unavailable);
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
                sendUnbindDidiRequest();
            }
        });

        expenseTextView = (TextView) findViewById(R.id.expenseTextView);

        adapter = new DidiExpenseListViewAdapter(DidiExpenseActivity.this, expenseList);
        expenseListView = (XListView) findViewById(R.id.expenseListView);
        expenseListView.setAdapter(adapter);
        expenseListView.setXListViewListener(new XListView.IXListViewListener()
        {
            public void onRefresh()
            {
                if (PhoneUtils.isNetworkConnected())
                {
                    sendDidiOrdersRequest();
                }
                else
                {
                    ViewUtils.showToast(DidiExpenseActivity.this, R.string.error_get_data_network_unavailable);
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
                    Intent intent = new Intent(DidiExpenseActivity.this, EditItemActivity.class);
                    intent.putExtra("fromDidi", true);
                    intent.putExtra("expense", expenseList.get(position - 1));
                    ViewUtils.goForward(DidiExpenseActivity.this, intent);
                }
            }
        });

        int visibility = expenseList.isEmpty() ? View.VISIBLE : View.GONE;
        expenseTextView.setVisibility(visibility);

        if (!needToGetData)
        {
            getOrderAmount();
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
            expenseList.addAll((List<DidiExpense>) serializable);
            needToGetData = false;
        }

        token = AppPreference.getAppPreference().getCurrentUser().getDidiToken();
    }

    // Network
    private void initBeeCloud()
    {
        BeeCloud.setAppIdAndSecret(this, "0f4179d0-b80d-45c6-9ca1-7ad46f4c9402", "9e4a4a76-e771-4d75-aa52-4ff7c15df658");
    }

    private void getOrderAmount()
    {
        if (PhoneUtils.isNetworkConnected())
        {
            for (DidiExpense expense : expenseList)
            {
                switch (expense.getType())
                {
                    case DidiExpense.TYPE_TAXI:
                        sendDidiDetailTaxiRequest(expense);
                        break;
                    case DidiExpense.TYPE_ZHUAN_CHE:
                        sendDidiDetailZhuanCheRequest(expense);
                        break;
                    case DidiExpense.TYPE_KUAI_CHE:
                        sendDidiDetailKuaiCheRequest(expense);
                        break;
                    case DidiExpense.TYPE_LIFT:
                        sendDidiDetailLiftRequest(expense);
                        break;
                }
            }
        }
    }

    private void sendDidiOrdersRequest()
    {
        DidiOrdersRequest request = new DidiOrdersRequest();
        request.sendRequest(new HttpConnectionCallback()
        {
            public void execute(Object httpResponse)
            {
                final DidiOrdersResponse response = new DidiOrdersResponse(httpResponse);
                if (response.getStatus())
                {
                    needToGetData = false;

                    expenseList.clear();
                    expenseList.addAll(response.getDidiExpenseList());

                    getOrderAmount();

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
                            ViewUtils.showToast(DidiExpenseActivity.this, R.string.failed_to_get_didi_expenses, response.getErrorMessage());
                            expenseListView.stopRefresh();
                        }
                    });
                }
            }
        });
    }

    private void sendDidiDetailTaxiRequest(final DidiExpense expense)
    {
        DidiDetailTaxiRequest request = new DidiDetailTaxiRequest(expense.getOrderID(), token);
        request.sendRequest(new HttpConnectionCallback()
        {
            public void execute(Object httpResponse)
            {
                DidiDetailTaxiResponse response = new DidiDetailTaxiResponse(httpResponse);
                if (response.getStatus())
                {
                    expense.setAmount(response.getAmount());
                    expense.setCity(response.getCity());

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

    private void sendDidiDetailZhuanCheRequest(final DidiExpense expense)
    {
        DidiDetailZhuanCheRequest request = new DidiDetailZhuanCheRequest(expense.getOrderID(), token);
        request.sendRequest(new HttpConnectionCallback()
        {
            public void execute(Object httpResponse)
            {
                DidiDetailZhuanCheResponse response = new DidiDetailZhuanCheResponse(httpResponse);
                if (response.getStatus())
                {
                    expense.setAmount(response.getAmount());
                    expense.setCity(response.getCity());

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

    private void sendDidiDetailKuaiCheRequest(final DidiExpense expense)
    {
        DidiDetailKuaiCheRequest request = new DidiDetailKuaiCheRequest(expense.getOrderID(), token);
        request.sendRequest(new HttpConnectionCallback()
        {
            public void execute(Object httpResponse)
            {
                DidiDetailKuaiCheResponse response = new DidiDetailKuaiCheResponse(httpResponse);
                if (response.getStatus())
                {
                    expense.setAmount(response.getAmount());
                    expense.setCity(response.getCity());

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

    private void sendDidiDetailLiftRequest(final DidiExpense expense)
    {
        DidiDetailLiftRequest request = new DidiDetailLiftRequest(expense.getOrderID(), token);
        request.sendRequest(new HttpConnectionCallback()
        {
            public void execute(Object httpResponse)
            {
                DidiDetailLiftResponse response = new DidiDetailLiftResponse(httpResponse);
                if (response.getStatus())
                {
                    expense.setAmount(response.getAmount());
                    queryCity(expense, response.getLatitude(), response.getLongitude());

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

    private void sendUnbindDidiRequest()
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

                            ViewUtils.showToast(DidiExpenseActivity.this, R.string.succeed_in_unbinding_didi);
                            ViewUtils.goBackWithIntent(DidiExpenseActivity.this, BindDidiActivity.class);
                        }
                        else
                        {
                            ViewUtils.showToast(DidiExpenseActivity.this, R.string.failed_to_unbind_didi, response.getErrorMessage());
                        }
                    }
                });
            }
        });
    }

    private void queryCity(DidiExpense expense, double latitude, double longitude)
    {
        BCLocation location = BCLocation.locationWithLatitude(latitude, longitude);
        String city = location.getCity();

        int index = city.indexOf(ViewUtils.getString(R.string.city));
        if (index > 0)
        {
            city = city.substring(0, index);
        }
        expense.setCity(city);

        runOnUiThread(new Runnable()
        {
            public void run()
            {
                adapter.notifyDataSetChanged();
            }
        });
    }
}