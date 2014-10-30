package com.rushucloud.reim;

import java.util.ArrayList;
import java.util.List;

import com.umeng.analytics.MobclickAgent;

import netUtils.HttpConnectionCallback;
import netUtils.SyncDataCallback;
import netUtils.SyncUtils;
import netUtils.Request.Report.SubordinatesReportRequest;
import netUtils.Response.Report.SubordinatesReportResponse;
import classes.AppPreference;
import classes.ReimApplication;
import classes.Report;
import classes.Utils;
import classes.XListView;
import classes.Adapter.ReportListViewAdapter;
import classes.Adapter.ReportTagGridViewAdapter;
import classes.XListView.IXListViewListener;
import database.DBManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.support.v4.app.Fragment;

public class OthersReportFragment extends Fragment implements IXListViewListener
{	
	private static final int SORT_NULL = 0;	
	private static final int SORT_ITEMS_COUNT = 1;	
	private static final int SORT_AMOUNT = 2;	
	private static final int SORT_CREATE_DATE = 3;	
	private static final int SORT_MODIFY_DATE = 4;	
	
	private View view;
	private View filterView;
	private XListView othersListView;
	private ReportListViewAdapter othersAdapter;

	private WindowManager windowManager;
	private LayoutParams params = new LayoutParams();
	private DBManager dbManager;
	private List<Report> othersList = new ArrayList<Report>();
	private List<Report> showOthersList = new ArrayList<Report>();
	
	private int sortType = SORT_NULL;
	private boolean sortReverse = false;
	
	private int tempSortType = SORT_NULL;
	
	private List<Integer> filterStatusList = new ArrayList<Integer>();
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		if (view == null)
		{
			view = inflater.inflate(R.layout.report_others, container, false);
		}
		else
		{
			ViewGroup viewGroup = (ViewGroup)view.getParent();
			viewGroup.removeView(view);
		}
		setHasOptionsMenu(true);
	    return view;  
	}
	   
	public void onResume()
	{
		super.onResume();
		MobclickAgent.onPageStart("OthersReportFragment");	
		ReimApplication.pDialog.show();
        viewInitialise();
        dataInitialise();
		refreshApproveReportListView();
		ReimApplication.pDialog.dismiss();
		getSubordinatesReports();
	}

	public void onPause()
	{
		super.onPause();
		MobclickAgent.onPageEnd("OthersReportFragment");
	}
	
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{
		inflater.inflate(R.menu.report, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	public boolean onOptionsItemSelected(MenuItem item)
	{
		int id = item.getItemId();
		if (id == R.id.action_filter_item)
		{		
			windowManager.addView(filterView, params);
		}
			
		return super.onOptionsItemSelected(item);
	}
    
    private void dataInitialise()
    {
		if (dbManager == null)
		{
			dbManager = DBManager.getDBManager();			
		}
    }

	private void viewInitialise()
	{
		if (othersAdapter == null)
		{
			othersAdapter = new ReportListViewAdapter(getActivity(), othersList);			
		}
		
		if (othersListView == null)
		{
			othersListView = (XListView)getActivity().findViewById(R.id.othersReportListView);
			othersListView.setAdapter(othersAdapter);
			othersListView.setXListViewListener(this);
			othersListView.setPullLoadEnable(true);
			othersListView.setPullRefreshEnable(true);
			othersListView.setOnItemClickListener(new OnItemClickListener()
			{
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id)
				{
					Report report = showOthersList.get(position);
					Bundle bundle = new Bundle();
					bundle.putSerializable("report", report);
					Intent intent;
					if (report.getStatus() == Report.STATUS_SUBMITTED)
					{
						intent = new Intent(getActivity(), ApproveReportActivity.class);
					}
					else
					{
						intent = new Intent(getActivity(), ShowReportActivity.class);
					}
					intent.putExtras(bundle);
					startActivity(intent);
				}
			});
		}
		
		if (filterView == null)
		{
			windowManager = (WindowManager)getActivity().getSystemService(Context.WINDOW_SERVICE);	
			
			DisplayMetrics dm = new DisplayMetrics();
			getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
			
			filterView = getActivity().getLayoutInflater().inflate(R.layout.report_filter, (ViewGroup) null, false);
			filterView.setBackgroundColor(Color.WHITE);
			filterView.setMinimumHeight(dm.heightPixels);

			final RadioButton sortNullRadio = (RadioButton)filterView.findViewById(R.id.sortNullRadio);
			final RadioButton sortItemsCountRadio = (RadioButton)filterView.findViewById(R.id.sortItemsCountRadio);
			final RadioButton sortAmountRadio = (RadioButton)filterView.findViewById(R.id.sortAmountRadio);	
			final RadioButton sortCreateDateRadio = (RadioButton)filterView.findViewById(R.id.sortCreateDateRadio);
			final RadioButton sortModifyDateRadio = (RadioButton)filterView.findViewById(R.id.sortModifyDateRadio);		
			RadioGroup sortRadioGroup = (RadioGroup)filterView.findViewById(R.id.sortRadioGroup);
			sortRadioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener()
			{
				public void onCheckedChanged(RadioGroup group, int checkedId)
				{
					if (checkedId == sortNullRadio.getId())
					{
						tempSortType = SORT_NULL;
					}
					else if (checkedId == sortItemsCountRadio.getId())
					{
						tempSortType = SORT_ITEMS_COUNT;
					}
					else if (checkedId == sortAmountRadio.getId())
					{
						tempSortType = SORT_AMOUNT;
					}
					else if (checkedId == sortCreateDateRadio.getId())
					{
						tempSortType = SORT_CREATE_DATE;
					}
					else if (checkedId == sortModifyDateRadio.getId())
					{
						tempSortType = SORT_MODIFY_DATE;
					}
				}
			});

			final ReportTagGridViewAdapter tagAdapter = new ReportTagGridViewAdapter(getActivity());
			
			GridView tagGridView = (GridView)filterView.findViewById(R.id.tagGridView);
			tagGridView.setAdapter(tagAdapter);
			tagGridView.setOnItemClickListener(new OnItemClickListener()
			{
				public void onItemClick(AdapterView<?> parent, View view, int position, long id)
				{
					tagAdapter.setSelection(position);
					tagAdapter.notifyDataSetChanged();
				}
			});
			
			Button confirmButton = (Button)filterView.findViewById(R.id.confirmButton);
			confirmButton.setOnClickListener(new View.OnClickListener()
			{
				public void onClick(View v)
				{
					sortType = tempSortType;
					filterStatusList.clear();
					filterStatusList.addAll(tagAdapter.getFilterStatusList());					
					sortReverse = !sortReverse;
					
					windowManager.removeView(filterView);
					ReimApplication.pDialog.show();
					refreshApproveReportListView();
					ReimApplication.pDialog.dismiss();
				}
			});
			
			Button cancelButton = (Button)filterView.findViewById(R.id.cancelButton);
			cancelButton.setOnClickListener(new View.OnClickListener()
			{
				public void onClick(View v)
				{
					windowManager.removeView(filterView);
				}
			});
		}
	}
	
	private List<Report> readApproveReportList()
	{
		AppPreference appPreference = AppPreference.getAppPreference();
		DBManager dbManager = DBManager.getDBManager();
		return dbManager.getApproveReports(appPreference.getCurrentUserID());
	}
	
	private void refreshApproveReportListView()
	{
		othersList.clear();
		othersList.addAll(readApproveReportList());
		showOthersList.clear();
		showOthersList.addAll(filterReportList(othersList));
		othersAdapter.set(showOthersList);
		othersAdapter.notifyDataSetChanged();	
	}

	private List<Report> filterReportList(List<Report> reportList)
	{
		List<Report> newReportList = new ArrayList<Report>(reportList);
		return newReportList;
	}

	private void getSubordinatesReports()
	{
		SubordinatesReportRequest request = new SubordinatesReportRequest(0, 9999, 1);
		request.sendRequest(new HttpConnectionCallback()
		{
			public void execute(Object httpResponse)
			{
				SubordinatesReportResponse response = new SubordinatesReportResponse(httpResponse);
				if (response.getStatus())
				{
					getActivity().runOnUiThread(new Runnable()
					{
						public void run()
						{
							
						}
					});
				}
			}
		});
	}

	public void onRefresh()
	{
		new Handler().postDelayed(new Runnable()
		{
			public void run()
			{
				othersListView.stopRefresh();
				othersListView.setRefreshTime(Utils.secondToStringUpToMinute(Utils.getCurrentTime()));
			}
		}, 2000);
	}

	public void onLoadMore()
	{
		new Handler().postDelayed(new Runnable()
		{
			public void run()
			{
				othersListView.stopLoadMore();
			}
		}, 2000);		
	}
}