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
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.rushucloud.reim.R;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.List;

import classes.Group;
import classes.adapter.CompanyListViewAdapter;
import classes.utils.AppPreference;
import classes.utils.ViewUtils;
import classes.widget.ReimProgressDialog;
import netUtils.HttpConnectionCallback;
import netUtils.request.group.SearchGroupRequest;
import netUtils.request.user.ApplyRequest;
import netUtils.response.group.SearchGroupResponse;
import netUtils.response.user.ApplyResponse;

public class PickCompanyActivity extends Activity
{
    private TextView completeTextView;
    private EditText companyEditText;
    private CompanyListViewAdapter adapter;

    private List<Group> companyList = new ArrayList<Group>();
    private Group company;

	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_guide_pick_company);
		initView();
	}

	protected void onResume()
	{
		super.onResume();
		MobclickAgent.onPageStart("ChooseCompanyActivity");
		MobclickAgent.onResume(this);
        ReimProgressDialog.setContext(this);
	}

	protected void onPause()
	{
		super.onPause();
		MobclickAgent.onPageEnd("ChooseCompanyActivity");
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
		getActionBar().hide();

		ImageView backImageView = (ImageView) findViewById(R.id.backImageView);
		backImageView.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
                goBack();
			}
		});

		completeTextView = (TextView) findViewById(R.id.completeTextView);
        completeTextView.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                if (company != null)
                {
                    hideSoftKeyboard();
                    sendApplyRequest();
                }
            }
        });

        ImageView searchImageView = (ImageView) findViewById(R.id.searchImageView);
        searchImageView.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                hideSoftKeyboard();
                if (companyEditText.getText().toString().isEmpty())
                {
                    ViewUtils.showToast(PickCompanyActivity.this, R.string.input_company_name);
                }
                else
                {
                    sendSearchGroupRequest();
                }
            }
        });

        companyEditText = (EditText) findViewById(R.id.companyEditText);
        companyEditText.setOnFocusChangeListener(ViewUtils.onFocusChangeListener);
        ViewUtils.requestFocus(this, companyEditText);

        adapter = new CompanyListViewAdapter(this, companyList, company);
        ListView companyListView = (ListView) findViewById(R.id.companyListView);
        companyListView.setAdapter(adapter);
        companyListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                if (!companyList.isEmpty())
                {
                    Group chosenCompany = companyList.get(position);
                    company = chosenCompany.equals(company)? null : chosenCompany;
                    adapter.setCompany(company);
                    adapter.notifyDataSetChanged();
                    if (company == null)
                    {
                        completeTextView.setTextColor(ViewUtils.getColor(R.color.font_title_selected));
                    }
                    else
                    {
                        completeTextView.setTextColor(getResources().getColorStateList(R.color.title_text_color));
                    }
                }
            }
        });
	}

    private void sendSearchGroupRequest()
    {
        ReimProgressDialog.show();
        SearchGroupRequest request = new SearchGroupRequest(companyEditText.getText().toString());
        request.sendRequest(new HttpConnectionCallback()
        {
            public void execute(Object httpResponse)
            {
                final SearchGroupResponse response = new SearchGroupResponse(httpResponse);
                if (response.getStatus())
                {
                    company = null;
                    companyList.clear();
                    companyList.addAll(response.getGroupList());

                    runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            ReimProgressDialog.dismiss();
                            adapter.setCompany(company);
                            adapter.setCompanyList(companyList);
                            adapter.notifyDataSetChanged();
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
                            ViewUtils.showToast(PickCompanyActivity.this, R.string.failed_to_search_companies, response.getErrorMessage());
                        }
                    });
                }
            }
        });
    }

    private void sendApplyRequest()
    {
        ReimProgressDialog.show();
        ApplyRequest request = new ApplyRequest(company.getServerID());
        request.sendRequest(new HttpConnectionCallback()
        {
            public void execute(Object httpResponse)
            {
                final ApplyResponse response = new ApplyResponse(httpResponse);
                if (response.getStatus())
                {
                    runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            ReimProgressDialog.dismiss();
                            Intent intent = new Intent(PickCompanyActivity.this, JoinCompleteActivity.class);
                            intent.putExtra("companyName", company.getName());
                            ViewUtils.goForwardAndFinish(PickCompanyActivity.this, intent);
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
                            ViewUtils.showToast(PickCompanyActivity.this, R.string.failed_to_apply, response.getErrorMessage());
                        }
                    });
                }
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
        Intent intent = new Intent(this, ModifyNicknameActivity.class);
        intent.putExtra("nickname", AppPreference.getAppPreference().getCurrentUser().getNickname());
        ViewUtils.goBackWithIntent(this, intent);
    }
}