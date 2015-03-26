package com.rushucloud.reim.report;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.rushucloud.reim.R;
import com.umeng.analytics.MobclickAgent;

import classes.Report;
import classes.User;
import classes.utils.AppPreference;
import classes.utils.PhoneUtils;
import classes.utils.Utils;
import classes.utils.ViewUtils;
import classes.widget.ReimProgressDialog;
import netUtils.HttpConnectionCallback;
import netUtils.request.report.ExportReportRequest;
import netUtils.response.report.ExportReportResponse;

public class ExportActivity extends Activity
{
	private EditText emailEditText;

	private User currentUser;
    private Report report;

	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_report_export);
		initData();
		initView();
	}

	protected void onResume()
	{
		super.onResume();
		MobclickAgent.onPageStart("ExportActivity");
		MobclickAgent.onResume(this);
		ReimProgressDialog.setContext(this);
	}

	protected void onPause()
	{
		super.onPause();
		MobclickAgent.onPageEnd("ExportActivity");
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
		currentUser = AppPreference.getAppPreference().getCurrentUser();
        report = (Report) getIntent().getExtras().getSerializable("report");
	}

	private void initView()
	{
		getActionBar().hide();

		ImageView backImageView = (ImageView) findViewById(R.id.backImageView);
		backImageView.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				hideSoftKeyboard();
				finish();
			}
		});

		TextView confirmTextView = (TextView) findViewById(R.id.confirmTextView);
        confirmTextView.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				hideSoftKeyboard();
                if (!PhoneUtils.isNetworkConnected())
                {
                    ViewUtils.showToast(ExportActivity.this, R.string.error_export_network_unavailable);
                }
                else
                {
                    String email = emailEditText.getText().toString();
                    if (email.isEmpty())
                    {
                        ViewUtils.showToast(ExportActivity.this, R.string.error_email_empty);
                    }
                    else if (!Utils.isEmail(email))
                    {
                        ViewUtils.showToast(ExportActivity.this, R.string.error_email_wrong_format);
                    }
                    else
                    {
                        sendExportReportRequest(report.getServerID(), email);
                    }
                }
			}
		});
		
		emailEditText = (EditText) findViewById(R.id.emailEditText);
    	emailEditText.setOnFocusChangeListener(ViewUtils.onFocusChangeListener);
    	emailEditText.setText(currentUser.getEmail());

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
		imm.hideSoftInputFromWindow(emailEditText.getWindowToken(), 0);
	}

    private void sendExportReportRequest(int reportID, String email)
    {
        ReimProgressDialog.show();
        ExportReportRequest request = new ExportReportRequest(reportID, email);
        request.sendRequest(new HttpConnectionCallback()
        {
            public void execute(Object httpResponse)
            {
                final ExportReportResponse response = new ExportReportResponse(httpResponse);
                if (response.getStatus())
                {
                    runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            ReimProgressDialog.dismiss();
                            ViewUtils.showToast(ExportActivity.this, R.string.succeed_in_exporting);
                            finish();
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
                            ViewUtils.showToast(ExportActivity.this, R.string.failed_to_export, response.getErrorMessage());
                        }
                    });
                }
            }
        });
    }
}
