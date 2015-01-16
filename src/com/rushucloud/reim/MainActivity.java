package com.rushucloud.reim;

import java.util.ArrayList;
import java.util.List;

import netUtils.HttpConnectionCallback;
import netUtils.UDPClient;
import netUtils.UDPConnectionCallback;
import netUtils.Response.EventsResponse;
import netUtils.Response.Group.GetGroupResponse;
import netUtils.Request.EventsReadRequest;
import netUtils.Request.EventsRequest;
import netUtils.Request.Group.GetGroupRequest;
import classes.ReimApplication;
import classes.User;
import classes.utils.AppPreference;
import classes.utils.DBManager;
import classes.utils.Utils;
import classes.widget.ReimProgressDialog;
import classes.widget.TabItem;

import com.rushucloud.reim.item.EditItemActivity;
import com.umeng.analytics.MobclickAgent;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.app.ActionBarActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends ActionBarActivity implements OnClickListener
{
	private long exitTime;

	private ViewPager viewPager;
	private TextView shortBadgeTextView;
	private TextView mediumBadgeTextView;
	private TextView longBadgeTextView;
	private ImageView tipImageView;
	
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
		
		if (Utils.isNetworkConnected())
		{
			sendGetEventsRequest();
			
			if (udpClient == null)
			{
				udpClient = new UDPClient();
				udpClient.send(new UDPConnectionCallback()
				{
					public void execute(Object udpResponse)
					{
						if (Utils.isNetworkConnected())
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
				Utils.showToast(MainActivity.this, R.string.prompt_press_back_to_exit);
				exitTime = System.currentTimeMillis();
			}
			else
			{
				finish();
				dbManager.close();
				udpClient.close();
				android.os.Process.killProcess(android.os.Process.myPid());
			}
			return true;
		}
		else
		{
			return super.onKeyDown(keyCode, event);
		}
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
		
		viewPager = (ViewPager)findViewById(R.id.viewPager);
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
						if (Utils.isNetworkConnected())
						{
							sendEventsReadRequest(EventsReadRequest.TYPE_REPORT);		
						}					
					}
					else if (currentIndex == 3)
					{
						setMeBadge(0);
						if (Utils.isNetworkConnected())
						{
							sendEventsReadRequest(EventsReadRequest.TYPE_INVITE);		
						}
					}
				}
			}
		});

		TabItem tabItemReim = (TabItem)findViewById(R.id.tabItemReim);
		TabItem tabItemReport = (TabItem)findViewById(R.id.tabItemReport);
		TabItem tabItemStat = (TabItem)findViewById(R.id.tabItemStat);
		TabItem tabItemMe = (TabItem)findViewById(R.id.tabItemMe);
		
		tabItemReim.setOnClickListener(this);
		tabItemReport.setOnClickListener(this);
		tabItemStat.setOnClickListener(this);
		tabItemMe.setOnClickListener(this);
		
		tabItemList.add(tabItemReim);
		tabItemList.add(tabItemReport);
		tabItemList.add(tabItemStat);
		tabItemList.add(tabItemMe);
		
		tabItemReim.setIconAlpha(1);
		
		shortBadgeTextView = (TextView)findViewById(R.id.shortBadgeTextView);
		mediumBadgeTextView = (TextView)findViewById(R.id.mediumBadgeTextView);
		longBadgeTextView = (TextView)findViewById(R.id.longBadgeTextView);
		tipImageView = (ImageView)findViewById(R.id.tipImageView);
		
		Button addButton = (Button)findViewById(R.id.addButton);
		addButton.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				Intent intent = new Intent(MainActivity.this, EditItemActivity.class);
				intent.putExtra("fromReim", true);
				startActivity(intent);
			}
		});
	}
	
	private void initData()
	{
		dbManager = DBManager.getDBManager();
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
		if (eventCount > 99)
		{
			longBadgeTextView.setVisibility(View.VISIBLE);
			mediumBadgeTextView.setVisibility(View.GONE);
			shortBadgeTextView.setVisibility(View.GONE);
		}
		else if (eventCount > 9)
		{
			mediumBadgeTextView.setText(Integer.toString(eventCount));
			longBadgeTextView.setVisibility(View.GONE);
			mediumBadgeTextView.setVisibility(View.VISIBLE);
			shortBadgeTextView.setVisibility(View.GONE);
		}
		else if (eventCount > 0)
		{
			shortBadgeTextView.setText(Integer.toString(eventCount));
			longBadgeTextView.setVisibility(View.GONE);
			mediumBadgeTextView.setVisibility(View.GONE);
			shortBadgeTextView.setVisibility(View.VISIBLE);
		}
		else
		{
			longBadgeTextView.setVisibility(View.GONE);
			mediumBadgeTextView.setVisibility(View.GONE);
			shortBadgeTextView.setVisibility(View.GONE);
		}
	}
	
	private void setMeBadge(int eventCount)
	{
		if (eventCount > 0)
		{
			tipImageView.setVisibility(View.VISIBLE);
		}
		else
		{
			tipImageView.setVisibility(View.GONE);
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
					if (response.isNeedToRefresh() && Utils.isNetworkConnected())
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
						if (user.getServerID() == currentUser.getServerID())							
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

	public void onClick(View v)
	{
		resetTabItems();
		
		int position = 0;
		switch (v.getId())
		{
			case R.id.tabItemReim:
			{
				position = 0;
				break;
			}
			case R.id.tabItemReport:
			{
				position = 1;
				setReportBadge(0);
				if (Utils.isNetworkConnected())
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
				if (Utils.isNetworkConnected())
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