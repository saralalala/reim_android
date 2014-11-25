package com.rushucloud.reim;

import classes.ReimApplication;

import com.umeng.analytics.MobclickAgent;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TabHost.TabSpec;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTabHost;

public class ReportFragment extends Fragment
{
	private FragmentTabHost tabHost;
	private View view;
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		if (view == null)
		{
			view = inflater.inflate(R.layout.fragment_report, container, false);
		}
		else
		{
			ViewGroup viewGroup = (ViewGroup) view.getParent();
			if (viewGroup != null)
			{
				viewGroup.removeView(view);
			}
		}
		initTabHost();
	    return view;  
	}	
	
	public void setUserVisibleHint(boolean isVisibleToUser)
	{
		super.setUserVisibleHint(isVisibleToUser);
		if (isVisibleToUser && tabHost.getCurrentTab() == 0)
		{
			setHasOptionsMenu(true);	
		}
	}
	
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{
		System.out.println("ReportFragment onCreateOptionsMenu");
		inflater.inflate(R.menu.report, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}
	
	public boolean onOptionsItemSelected(MenuItem item)
	{
		System.out.println("ReportFragment onOptionsItemSelected");
		int id = item.getItemId();
		if (id == R.id.action_filter_item)
		{
			if (tabHost.getCurrentTab() == 0)
			{
				MyReportFragment fragment = (MyReportFragment) getChildFragmentManager().findFragmentByTag("myReport");
				if (fragment != null)
				{
					fragment.onOptionsItemSelected(item);
				}				
			}
			else
			{
				OthersReportFragment fragment = (OthersReportFragment) getChildFragmentManager().findFragmentByTag("othersReport");
				if (fragment != null)
				{
					fragment.onOptionsItemSelected(item);
				}				
			}
		}
			
		return super.onOptionsItemSelected(item);
	}
	   
	public void onResume()
	{
		super.onResume();
		MobclickAgent.onPageStart("ReportFragment");

		tabHost.setCurrentTab(ReimApplication.getReportTabIndex());
	}

	public void onPause()
	{
		super.onPause();
		MobclickAgent.onPageEnd("ReportFragment");
	}

	private void initTabHost()
	{
		if (tabHost == null)
		{
			tabHost = (FragmentTabHost)view.findViewById(android.R.id.tabhost);
			tabHost.setup(getActivity(), getChildFragmentManager(), R.id.baseLayout);
			
			TabSpec myReportSpec = tabHost.newTabSpec("myReport").setIndicator(getResources().getString(R.string.myReport));
			tabHost.addTab(myReportSpec, MyReportFragment.class, null);
			
			tabHost.addTab(tabHost.newTabSpec("othersReport")
					.setIndicator(getResources().getString(R.string.othersReport)), OthersReportFragment.class, null);
			
			DisplayMetrics dm = new DisplayMetrics();
			getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
			int screenWidth = dm.widthPixels;
			tabHost.getTabWidget().getChildTabViewAt(0).setMinimumWidth(screenWidth / 2);
			tabHost.getTabWidget().getChildTabViewAt(1).setMinimumWidth(screenWidth / 2);
		}
	}
}