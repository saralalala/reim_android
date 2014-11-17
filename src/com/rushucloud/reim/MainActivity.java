package com.rushucloud.reim;

import java.util.List;

import netUtils.HttpConnectionCallback;
import netUtils.Request.EventsReadRequest;
import netUtils.Request.EventsRequest;
import netUtils.Request.Group.GetGroupRequest;
import netUtils.Response.EventsResponse;
import netUtils.Response.Group.GetGroupResponse;
import classes.AppPreference;
import classes.ReimApplication;
import classes.User;
import classes.Utils;

import com.umeng.analytics.MobclickAgent;

import database.DBManager;
import android.support.v4.app.FragmentTabHost;
import android.support.v7.app.ActionBarActivity;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TabHost.TabSpec;

public class MainActivity extends ActionBarActivity
{
	private long exitTime;

	private FragmentTabHost tabHost;

	private Class<?> fragmentList[] = { ReimFragment.class, ReportFragment.class,
			StatisticsFragment.class, MeFragment.class };
	private int imageViewList[] = { R.drawable.tab_item_reim, R.drawable.tab_item_report,
			R.drawable.tab_item_statistics, R.drawable.tab_item_me };
	private int textViewList[] = { R.string.reimbursement, R.string.report, R.string.statistics,
			R.string.me };

	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		tabHostInitialse();
	}

	protected void onResume()
	{
		super.onResume();
		MobclickAgent.onResume(this);
		ReimApplication.setProgressDialog(this);

		tabHost.setCurrentTab(ReimApplication.getTabIndex());
		if (Utils.isNetworkConnected())
		{
			sendGetEventsRequest();			
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
				Toast.makeText(MainActivity.this, "再按一次返回键退出程序", Toast.LENGTH_SHORT).show();
				exitTime = System.currentTimeMillis();
			}
			else
			{
				finish();
				DBManager dbManager = DBManager.getDBManager();
				dbManager.close();
				android.os.Process.killProcess(android.os.Process.myPid());
			}
			return true;
		}
		else
		{
			return super.onKeyDown(keyCode, event);
		}
	}

	private void tabHostInitialse()
	{
		if (tabHost == null)
		{
			LayoutInflater layoutInflater = LayoutInflater.from(this);
			tabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
			tabHost.setup(this, getSupportFragmentManager(), R.id.realTabContent);
			tabHost.getTabWidget().setDividerDrawable(null);

			for (int i = 0; i < 4; i++)
			{
				View view = layoutInflater.inflate(R.layout.tab_item, (ViewGroup) null, false);
				view.setBackgroundColor(Color.WHITE);

				Drawable drawableTop = getResources().getDrawable(imageViewList[i]);
				drawableTop.setBounds(0, 5, drawableTop.getMinimumWidth(),
						drawableTop.getMinimumHeight() + 5);

				TextView textView = (TextView) view.findViewById(R.id.textView);
				textView.setText(getText(textViewList[i]));
				textView.setCompoundDrawablePadding(5);
				textView.setCompoundDrawables(null, drawableTop, null, null);

				TabSpec tabSpec = tabHost.newTabSpec(getText(textViewList[i]).toString()).setIndicator(view);
				tabHost.addTab(tabSpec, fragmentList[i], null);
			}
			
			tabHost.setOnTabChangedListener(new OnTabChangeListener()
			{
				public void onTabChanged(String tabId)
				{
					if (tabId.equals(getText(textViewList[0]).toString()))
					{
						ReimApplication.setTabIndex(0);
					}
					else if (tabId.equals(getText(textViewList[1]).toString()))
					{
						ReimApplication.setTabIndex(1);
						setReportBadge(0);
						if (Utils.isNetworkConnected())
						{
							sendEventsReadRequest(EventsReadRequest.TYPE_REPORT);		
						}
					}
					else if (tabId.equals(getText(textViewList[2]).toString()))
					{
						ReimApplication.setTabIndex(2);
					}
					else if (tabId.equals(getText(textViewList[3]).toString()))
					{
						ReimApplication.setTabIndex(3);
						setMeBadge(0);
						if (Utils.isNetworkConnected())
						{
							sendEventsReadRequest(EventsReadRequest.TYPE_INVITE);		
						}
					}
				}
			});

//			View view = layoutInflater.inflate(R.layout.tab_item_button, (ViewGroup) null, false);
//			
//			Button button = (Button) view.findViewById(R.id.addButton);
//			button.setOnClickListener(new View.OnClickListener()
//			{
//				public void onClick(View v)
//				{
//					Toast.makeText(MainActivity.this, "test", Toast.LENGTH_SHORT).show();
//				}
//			});
//			
//			TabSpec tabSpec = tabHost.newTabSpec("test").setIndicator(view);
//			tabHost.addTab(tabSpec, fragmentList[2], null);
//			tabHost.getTabWidget().getChildAt(4).setBackgroundResource(R.drawable.selector_tab_background);
		}
	}
	
	private void setReportBadge(int eventCount)
	{
		View view = tabHost.getTabWidget().getChildAt(1);
		
		TextView shortBadgeTextView = (TextView) view.findViewById(R.id.shortBadgeTextView);
		TextView longBadgeTextView = (TextView) view.findViewById(R.id.longBadgeTextView);
		if (eventCount > 99)
		{
			longBadgeTextView.setText("99+");
			longBadgeTextView.setVisibility(View.VISIBLE);
			shortBadgeTextView.setVisibility(View.GONE);
		}
		else if (eventCount > 10)
		{
			longBadgeTextView.setText(Integer.toString(eventCount));
			longBadgeTextView.setVisibility(View.VISIBLE);
			shortBadgeTextView.setVisibility(View.GONE);
		}
		else if (eventCount > 0)
		{
			shortBadgeTextView.setText(Integer.toString(eventCount));
			shortBadgeTextView.setVisibility(View.VISIBLE);
			longBadgeTextView.setVisibility(View.GONE);
		}
		else
		{
			shortBadgeTextView.setVisibility(View.GONE);
			longBadgeTextView.setVisibility(View.GONE);
		}
	}
	
	private void setMeBadge(int eventCount)
	{
		View view = tabHost.getTabWidget().getChildAt(3);
		
		ImageView tipImageView = (ImageView) view.findViewById(R.id.tipImageView);
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
					DBManager dbManager = DBManager.getDBManager();
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
								if (user.getImageID() == currentUser.getImageID())
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
}