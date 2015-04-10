package com.rushucloud.reim.me;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.rushucloud.reim.R;
import com.umeng.analytics.MobclickAgent;

import classes.utils.PhoneUtils;
import classes.utils.ViewUtils;
import classes.widget.ClearEditText;
import netUtils.HttpConnectionCallback;
import netUtils.request.FeedbackRequest;
import netUtils.response.FeedbackResponse;

public class FeedbackActivity extends Activity
{
	private EditText feedbackEditText;
	private ClearEditText contactEditText;
	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_me_feedback);
		initView();
	}

	protected void onResume()
	{
		super.onResume();
		MobclickAgent.onPageStart("FeedbackActivity");		
		MobclickAgent.onResume(this);
	}

	protected void onPause()
	{
		super.onPause();
		MobclickAgent.onPageEnd("FeedbackActivity");
		MobclickAgent.onPause(this);
	}
	
	public boolean onKeyDown(int keyCode, @NonNull KeyEvent event)
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
		backImageView.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				finish();
			}
		});
		
		feedbackEditText = (EditText) findViewById(R.id.feedbackEditText);
		feedbackEditText.setOnFocusChangeListener(ViewUtils.onFocusChangeListener);
		
		contactEditText = (ClearEditText) findViewById(R.id.contactEditText);

		Button submitButton = (Button) findViewById(R.id.submitButton);
		submitButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				hideSoftKeyboard();
				if (PhoneUtils.isNetworkConnected())
				{
					sendFeedBack();
				}
				else
				{
					ViewUtils.showToast(FeedbackActivity.this, R.string.error_feedback_network_unavailable);
				}
			}
		});
		
		LinearLayout layout = (LinearLayout) findViewById(R.id.baseLayout);
		layout.setOnClickListener(new View.OnClickListener()
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
		imm.hideSoftInputFromWindow(feedbackEditText.getWindowToken(), 0);					
		imm.hideSoftInputFromWindow(contactEditText.getWindowToken(), 0);  	
    }
    
    private void sendFeedBack()
    {
		final String feedback = feedbackEditText.getText().toString();
		final String contactInfo = contactEditText.getText().toString();
		if (feedback.isEmpty() && contactInfo.isEmpty())
		{
			ViewUtils.showToast(this, R.string.error_feedback_contact_empty);
		}
		else
		{
	    	sendFeedbackRequest(feedback, contactInfo);
		}    	
    }
    
    private void sendFeedbackRequest(String feedback, String contactInfo)
    {
    	FeedbackRequest request = new FeedbackRequest(feedback, contactInfo, PhoneUtils.getAppVersion());
    	request.sendRequest(new HttpConnectionCallback()
		{
			public void execute(Object httpResponse)
			{
				final FeedbackResponse response = new FeedbackResponse(httpResponse);
				runOnUiThread(new Runnable()
				{
					public void run()
					{
						if (response.getStatus())
						{
                            ViewUtils.showToast(FeedbackActivity.this, R.string.succeed_in_sending_feedback);
                            finish();
						}
						else
						{
							ViewUtils.showToast(FeedbackActivity.this, R.string.failed_to_send_feedback, response.getErrorMessage());
						}
					}						
				});
			}
		});
    }
}