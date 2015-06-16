package com.rushucloud.reim.me;

import android.app.Activity;
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
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.List;

import classes.adapter.DidiExpenseListViewAdapter;
import classes.model.DidiExpense;
import classes.utils.PhoneUtils;
import classes.utils.Utils;
import classes.utils.ViewUtils;
import classes.widget.ReimProgressDialog;
import classes.widget.XListView;
import netUtils.common.HttpConnectionCallback;
import netUtils.request.user.GetMessagesRequest;
import netUtils.response.user.GetMessagesResponse;

public class DidiExpenseActivity extends Activity
{
    // Widgets
    private TextView expenseTextView;
    private XListView expenseListView;
    private DidiExpenseListViewAdapter adapter;

    // Local Data
    private List<DidiExpense> expenseList = new ArrayList<>();

    // View
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_me_didi_expenses);
        initView();
    }

    protected void onResume()
    {
        super.onResume();
        MobclickAgent.onPageStart("DidiExpenseActivity");
        MobclickAgent.onResume(this);
        ReimProgressDialog.setContext(this);
        if (PhoneUtils.isNetworkConnected())
        {
//            ReimProgressDialog.show();
//            sendGetMessagesRequest();
        }
        else
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
                    sendGetMessagesRequest();
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
//                    Bundle bundle = new Bundle();
//                    bundle.putSerializable("message", expenseList.get(position - 1));
//                    Intent intent = new Intent(DidiExpenseActivity.this, MessageActivity.class);
//                    intent.putExtras(bundle);
//                    ViewUtils.goForward(DidiExpenseActivity.this, intent);
                }
            }
        });
    }

    private void sendGetMessagesRequest()
    {
        GetMessagesRequest request = new GetMessagesRequest();
        request.sendRequest(new HttpConnectionCallback()
        {
            public void execute(Object httpResponse)
            {
                final GetMessagesResponse response = new GetMessagesResponse(httpResponse);
                if (response.getStatus())
                {
                    expenseList.clear();
//                    expenseList.addAll(response.getMessageList());

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

    private void goBack()
    {
        ViewUtils.goBack(this);
    }
}