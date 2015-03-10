package com.rushucloud.reim;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.app.ActionBarActivity;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;

import com.rushucloud.reim.item.EditItemActivity;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.List;

import classes.ReimApplication;
import classes.User;
import classes.utils.AppPreference;
import classes.utils.DBManager;
import classes.utils.PhoneUtils;
import classes.utils.Utils;
import classes.utils.ViewUtils;
import classes.widget.ReimProgressDialog;
import classes.widget.TabItem;
import netUtils.HttpConnectionCallback;
import netUtils.Request.EventsReadRequest;
import netUtils.Request.EventsRequest;
import netUtils.Request.FeedbackRequest;
import netUtils.Request.Group.GetGroupRequest;
import netUtils.Response.EventsResponse;
import netUtils.Response.FeedbackResponse;
import netUtils.Response.Group.GetGroupResponse;
import netUtils.UDPClient;
import netUtils.UDPConnectionCallback;

public class MainActivity extends ActionBarActivity implements OnClickListener
{
	private long exitTime;

	private ViewPager viewPager;
	private ImageView reportTipImageView;
	private ImageView meTipImageView;
	private PopupWindow feedbackPopupWindow;
	private EditText feedbackEditText;
	private PopupWindow phonePopupWindow;
	private EditText codeEditText;
	private EditText phoneEditText;
	
	private DBManager dbManager;
	private UDPClient udpClient;
	
	private List<Fragment> fragmentList = new ArrayList<Fragment>();
	private List<TabItem> tabItemList = new ArrayList<TabItem>();

	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initData();
		initView();
	}

	protected void onResume()
	{
		super.onResume();
		MobclickAgent.onResume(this);
		ReimProgressDialog.setProgressDialog(this);

		viewPager.setCurrentItem(ReimApplication.getTabIndex());
		resetTabItems();
		tabItemList.get(ReimApplication.getTabIndex()).setIconAlpha(1);
		fragmentList.get(viewPager.getCurrentItem()).setUserVisibleHint(true);
		
		if (PhoneUtils.isNetworkConnected())
		{
			sendGetEventsRequest();
			
			if (udpClient == null)
			{
				udpClient = new UDPClient();
				udpClient.send(new UDPConnectionCallback()
				{
					public void execute(Object udpResponse)
					{
						if (PhoneUtils.isNetworkConnected())
						{
							sendGetEventsRequest();
						}
					}
				});
			}
		}
	}

	protected void onPause()
	{
		super.onPause();
		MobclickAgent.onPause(this);
	}

	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if (keyCode == KeyEvent.KEYCODE_BACK)
		{
			if (System.currentTimeMillis() - exitTime > 2000)
			{
				ViewUtils.showToast(MainActivity.this, R.string.prompt_press_back_to_exit);
				exitTime = System.currentTimeMillis();
			}
			else
			{
				finish();
				dbManager.closeDatabase();
				if (udpClient != null)
				{
					udpClient.close();					
				}
				android.os.Process.killProcess(android.os.Process.myPid());
			}
			return true;
		}
		else
		{
			return super.onKeyDown(keyCode, event);
		}
	}

	private void initData()
	{
		dbManager = DBManager.getDBManager();
	}
	
	private void initView()
	{
		getActionBar().hide();
		
		ReimFragment reimFragment = new ReimFragment();
		ReportFragment reportFragment = new ReportFragment();
		StatisticsFragment statisticsFragment = new StatisticsFragment();
		MeFragment meFragment = new MeFragment();
		
		fragmentList.add(reimFragment);
		fragmentList.add(reportFragment);
		fragmentList.add(statisticsFragment);
		fragmentList.add(meFragment);
		
		viewPager = (ViewPager) findViewById(R.id.viewPager);
		viewPager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager())
		{
			public int getCount()
			{
				return fragmentList.size();
			}
			
			public Fragment getItem(int arg0)
			{
				return fragmentList.get(arg0);
			}

			public void destroyItem(ViewGroup container, int position, Object object)
			{
				
			}
		});
		viewPager.setOnPageChangeListener(new OnPageChangeListener()
		{
			public void onPageSelected(int arg0)
			{
				ReimApplication.setTabIndex(arg0);
			}
			
			public void onPageScrolled(int arg0, float arg1, int arg2)
			{
				if (arg1 > 0)
				{
					TabItem leftItem = tabItemList.get(arg0);
					TabItem rightItem = tabItemList.get(arg0 + 1);

					leftItem.setIconAlpha(1 - arg1);
					rightItem.setIconAlpha(arg1);
				}
			}
			
			public void onPageScrollStateChanged(int arg0)
			{
				if (arg0 == 2)
				{
					int currentIndex = viewPager.getCurrentItem();
					if (currentIndex == 1)
					{
						setReportBadge(0);	
						if (PhoneUtils.isNetworkConnected())
						{
							sendEventsReadRequest(EventsReadRequest.TYPE_REPORT);		
						}					
					}
					else if (currentIndex == 3)
					{
						setMeBadge(0);
						if (PhoneUtils.isNetworkConnected())
						{
							sendEventsReadRequest(EventsReadRequest.TYPE_INVITE);		
						}
					}
				}
			}
		});

		TabItem tabItemReim = (TabItem) findViewById(R.id.tabItemReim);
		TabItem tabItemReport = (TabItem) findViewById(R.id.tabItemReport);
		TabItem tabItemStat = (TabItem) findViewById(R.id.tabItemStat);
		TabItem tabItemMe = (TabItem) findViewById(R.id.tabItemMe);
		
		tabItemReim.setOnClickListener(this);
		tabItemReport.setOnClickListener(this);
		tabItemStat.setOnClickListener(this);
		tabItemMe.setOnClickListener(this);
		
		tabItemList.add(tabItemReim);
		tabItemList.add(tabItemReport);
		tabItemList.add(tabItemStat);
		tabItemList.add(tabItemMe);
		
		tabItemReim.setIconAlpha(1);

		reportTipImageView = (ImageView) findViewById(R.id.reportTipImageView);
		meTipImageView = (ImageView) findViewById(R.id.meTipImageView);
		
		Button addButton = (Button) findViewById(R.id.addButton);
		addButton.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				Intent intent = new Intent(MainActivity.this, EditItemActivity.class);
				intent.putExtra("fromReim", true);
				startActivity(intent);
			}
		});
	
		ImageView feedbackImageView = (ImageView) findViewById(R.id.feedbackImageView);
		feedbackImageView.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				MobclickAgent.onEvent(MainActivity.this, "UMENG_HELP");
				
				if (PhoneUtils.isNetworkConnected())
				{
					showFeedbackWindow();					
				}
				else
				{
					ViewUtils.showToast(MainActivity.this, R.string.error_feedback_network_unavailable);
				}
			}
		});
		
		initFeedbackWindow();
		initPhoneWindow();
	}
	
	private void initFeedbackWindow()
	{
		View feedbackView = View.inflate(this, R.layout.window_feedback, null);

		feedbackEditText = (EditText) feedbackView.findViewById(R.id.feedbackEditText);
		
		Button submitButton = (Button) feedbackView.findViewById(R.id.submitButton);
		submitButton.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				MobclickAgent.onEvent(MainActivity.this, "UMENG_HELP_SUBMIT");
				
				if (PhoneUtils.isNetworkConnected())
				{
					User user = AppPreference.getAppPreference().getCurrentUser();
					String feedback = feedbackEditText.getText().toString();
					if (feedback.isEmpty())
					{
						ViewUtils.showToast(MainActivity.this, R.string.error_feedback_empty);
					}
					else if (user != null && Utils.isPhone(user.getPhone()))
					{
						sendFeedbackRequest(feedbackEditText.getText().toString(), user.getPhone());
					}
					else
					{
						feedbackPopupWindow.dismiss();
						showPhoneWindow();
					}
				}
				else
				{
					ViewUtils.showToast(MainActivity.this, R.string.error_feedback_network_unavailable);
				}
			}
		});
		ViewUtils.resizeShortButton(submitButton, 90, false);
		
		Button cancelButton = (Button) feedbackView.findViewById(R.id.cancelButton);
		cancelButton.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				MobclickAgent.onEvent(MainActivity.this, "UMENG_HELP_CANCEL");				
				feedbackPopupWindow.dismiss();
			}
		});
		ViewUtils.resizeShortButton(cancelButton, 90, false);

		feedbackPopupWindow = ViewUtils.constructCenterPopupWindow(this, feedbackView);
	}
	
	private void initPhoneWindow()
	{		
		View phoneView = View.inflate(this, R.layout.window_feedback_phone, null);

		codeEditText = (EditText) phoneView.findViewById(R.id.codeEditText);
		phoneEditText = (EditText) phoneView.findViewById(R.id.phoneEditText);
		
		Button submitButton = (Button) phoneView.findViewById(R.id.submitButton);
		submitButton.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				if (PhoneUtils.isNetworkConnected())
				{
					String code = codeEditText.getText().toString();
					String phone = phoneEditText.getText().toString();
					if (code.isEmpty())
					{
						ViewUtils.showToast(MainActivity.this, R.string.error_feedback_code_empty);
					}
					else if (phone.isEmpty())
					{
						ViewUtils.showToast(MainActivity.this, R.string.error_phone_empty);
					}
					else
					{
						sendFeedbackRequest(feedbackEditText.getText().toString(), code + "-" + phone);
					}
				}
				else
				{
					ViewUtils.showToast(MainActivity.this, R.string.error_feedback_network_unavailable);
				}
			}
		});
		ViewUtils.resizeShortButton(submitButton, 90, false);
		
		Button skipButton = (Button) phoneView.findViewById(R.id.skipButton);
		skipButton.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				sendFeedbackRequest(feedbackEditText.getText().toString(), "");
			}
		});
		ViewUtils.resizeShortButton(skipButton, 90, false);

		phonePopupWindow = ViewUtils.constructCenterPopupWindow(this, phoneView);
	}

    private void showFeedbackWindow()
    {
    	feedbackEditText.setText("");
    	
		feedbackPopupWindow.showAtLocation(findViewById(R.id.containerLayout), Gravity.CENTER, 0, 0);
		feedbackPopupWindow.update();
    }

    private void showPhoneWindow()
    {
    	codeEditText.setText(R.string.feedback_code);
    	phoneEditText.setText("");
    	
		phonePopupWindow.showAtLocation(findViewById(R.id.containerLayout), Gravity.CENTER, 0, 0);
		phonePopupWindow.update();
    }
    
	private void resetTabItems()
	{
		for (int i = 0; i < tabItemList.size(); i++)
		{
			tabItemList.get(i).setIconAlpha(0);
		}
	}
	
	private void setReportBadge(int eventCount)
	{
		if (eventCount > 0)
		{
			reportTipImageView.setVisibility(View.VISIBLE);
		}
		else
		{
			reportTipImageView.setVisibility(View.GONE);
		}
	}
	
	private void setMeBadge(int eventCount)
	{
		if (eventCount > 0)
		{
			meTipImageView.setVisibility(View.VISIBLE);
		}
		else
		{
			meTipImageView.setVisibility(View.GONE);
		}
	}

	private void sendGetEventsRequest()
	{
		EventsRequest request = new EventsRequest();
		request.sendRequest(new HttpConnectionCallback()
		{
			public void execute(Object httpResponse)
			{
				final EventsResponse response = new EventsResponse(httpResponse);
				if (response.getStatus())
				{
					if (response.isNeedToRefresh() && PhoneUtils.isNetworkConnected())
					{
						sendGetGroupRequest();
					}
					
					runOnUiThread(new Runnable()
					{
						public void run()
						{
							ReimApplication.setReportBadgeCount(response.getApproveEventCount());
							setReportBadge(response.getReportEventCount());
							setMeBadge(response.getInviteEventCount());
						}
					});
				}
			}
		});
	}

	private void sendEventsReadRequest(int type)
	{
		EventsReadRequest request = new EventsReadRequest(type);
		request.sendRequest(null);
	}
	
	private void sendGetGroupRequest()
	{
		GetGroupRequest request = new GetGroupRequest();
		request.sendRequest(new HttpConnectionCallback()
		{
			public void execute(Object httpResponse)
			{
				GetGroupResponse response = new GetGroupResponse(httpResponse);
				if (response.getStatus())
				{
					int currentGroupID = response.getGroup() == null ? -1 : response.getGroup().getServerID();
					
					// update members
					List<User> memberList = response.getMemberList();
					User currentUser = AppPreference.getAppPreference().getCurrentUser();
					
					for (User user : memberList)
					{
						if (currentUser != null && user.getServerID() == currentUser.getServerID())
						{
							if (user.getServerUpdatedDate() > currentUser.getServerUpdatedDate())
							{
								if (user.getAvatarID() == currentUser.getAvatarID())
								{
									user.setAvatarPath(currentUser.getAvatarPath());								
								}								
							}
							else
							{
								user = currentUser;
							}
						}
					}
					
					dbManager.updateGroupUsers(memberList, currentGroupID);

					// update group info
					dbManager.syncGroup(response.getGroup());
				}
			}
		});
	}

    private void sendFeedbackRequest(String feedback, String contactInfo)
    {
    	ReimProgressDialog.show();
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
					    	ReimProgressDialog.dismiss();
							feedbackPopupWindow.dismiss();
							phonePopupWindow.dismiss();
							ViewUtils.showToast(MainActivity.this, R.string.prompt_feedback_sent);
						}
						else
						{
					    	ReimProgressDialog.dismiss();
							ViewUtils.showToast(MainActivity.this, R.string.failed_to_send_feedback, response.getErrorMessage());
						}
					}						
				});
			}
		});
    }
    
	public void onClick(View v)
	{
		resetTabItems();
		
		int position = 0;
		switch (v.getId())
		{
			case R.id.tabItemReim:
			{
				MobclickAgent.onEvent(MainActivity.this, "UMENG_ITEM");				
				position = 0;
				break;
			}
			case R.id.tabItemReport:
			{
				MobclickAgent.onEvent(MainActivity.this, "UMENG_REPORT");
				
				position = 1;
				setReportBadge(0);
				if (PhoneUtils.isNetworkConnected())
				{
					sendEventsReadRequest(EventsReadRequest.TYPE_REPORT);		
				}
				break;							
			}
			case R.id.tabItemStat:
			{
				position = 2;
				break;							
			}
			case R.id.tabItemMe:
			{
				position = 3;
				setMeBadge(0);
				if (PhoneUtils.isNetworkConnected())
				{
					sendEventsReadRequest(EventsReadRequest.TYPE_INVITE);		
				}
				break;							
			}
			default:
				break;
		}

		ReimApplication.setTabIndex(position);
		viewPager.setCurrentItem(position, false);
		tabItemList.get(position).setIconAlpha(1.0f);
	}
}