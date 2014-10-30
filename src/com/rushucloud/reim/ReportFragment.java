package com.rushucloud.reim;

import com.umeng.analytics.MobclickAgent;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
			ViewGroup viewGroup = (ViewGroup)view.getParent();
			viewGroup.removeView(view);
		}
		setHasOptionsMenu(true);
		tabHostInitialise();
	    return view;  
	}
	   
	public void onResume()
	{
		super.onResume();
		MobclickAgent.onPageStart("ReportFragment");
	}

	public void onPause()
	{
		super.onPause();
		MobclickAgent.onPageEnd("ReportFragment");
	}

	private void tabHostInitialise()
	{
		if (tabHost == null)
		{
			tabHost = (FragmentTabHost)view.findViewById(android.R.id.tabhost);
			tabHost.setup(getActivity(), getChildFragmentManager(), R.id.baseLayout);
			
			tabHost.addTab(tabHost.newTabSpec("myReport")
					.setIndicator(getResources().getString(R.string.myReport)), MyReportFragment.class, null);
			
			tabHost.addTab(tabHost.newTabSpec("othersReport")
					.setIndicator(getResources().getString(R.string.othersReport)), OthersReportFragment.class, null);
			
			DisplayMetrics dm = new DisplayMetrics();
			getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
			int screenWidth = dm.widthPixels;
			tabHost.getTabWidget().getChildTabViewAt(0).setMinimumWidth(screenWidth / 2);
			tabHost.getTabWidget().getChildTabViewAt(1).setMinimumWidth(screenWidth / 2);
		}
		Bundle bundle = getActivity().getIntent().getExtras();
		int index = bundle == null ? 0 : bundle.getInt("reportTabIndex");
		tabHost.setCurrentTab(index);
	}
}