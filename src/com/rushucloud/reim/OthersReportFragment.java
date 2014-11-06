package com.rushucloud.reim;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.umeng.analytics.MobclickAgent;

import netUtils.HttpConnectionCallback;
import netUtils.Request.Report.SubordinatesReportRequest;
import netUtils.Response.Report.SubordinatesReportResponse;
import classes.AppPreference;
import classes.ReimApplication;
import classes.Report;
import classes.Utils;
import classes.XListView;
import classes.Adapter.OthersReportListViewAdapter;
import classes.Adapter.ReportTagGridViewAdapter;
import classes.XListView.IXListViewListener;
import database.DBManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.View.OnKeyListener;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.support.v4.app.Fragment;

public class OthersReportFragment extends Fragment implements IXListViewListener
{	
	private static final int SORT_NULL = 0;	
	private static final int SORT_ITEMS_COUNT = 1;	
	private static final int SORT_AMOUNT = 2;	
	private static final int SORT_CREATE_DATE = 3;
	
	private View view;
	private View filterView;
	private XListView othersListView;
	private OthersReportListViewAdapter othersAdapter;

	private WindowManager windowManager;
	private LayoutParams params = new LayoutParams();
	private AppPreference appPreference;
	private DBManager dbManager;
	private List<Report> othersList = new ArrayList<Report>();
	private List<Report> showOthersList = new ArrayList<Report>();
	
	private int sortType = SORT_NULL;
	private boolean sortReverse = false;
	
	private int tempSortType = SORT_NULL;
	
	private List<Integer> filterStatusList = new ArrayList<Integer>();

	private OnKeyListener listener = new OnKeyListener()
	{
		public boolean onKey(View v, int keyCode, KeyEvent event)
		{
			if (keyCode == KeyEvent.KEYCODE_BACK)
			{
				windowManager.removeView(filterView);
			}
			return false;
		}
	};
	
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
		ReimApplication.showProgressDialog();
        initData();
        initView();
		ReimApplication.dismissProgressDialog();
		if (Utils.isNetworkConnected(getActivity()))
		{
			getSubordinatesReports();
		}
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
			MobclickAgent.onEvent(getActivity(), "UMENG_SHEET_OTHERS_REPORT_CLICK");
			windowManager.addView(filterView, params);
		}
			
		return super.onOptionsItemSelected(item);
	}
    
    private void initData()
    {
		if (dbManager == null)
		{
			dbManager = DBManager.getDBManager();			
		}
		
		if (appPreference == null)
		{
			appPreference = AppPreference.getAppPreference();
		}
		othersList.clear();
		othersList.addAll(dbManager.getOthersReports(appPreference.getCurrentUserID()));
		filterReportList();
    }

	private void initView()
	{
		othersAdapter = new OthersReportListViewAdapter(getActivity(), showOthersList);
		
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
					Report report = showOthersList.get(position-1);
					Bundle bundle = new Bundle();
					bundle.putSerializable("report", report);
					Intent intent;
					if (report.getStatus() == Report.STATUS_SUBMITTED)
					{
						intent = new Intent(getActivity(), ApproveReportActivity.class);
					}
					else
					{
						bundle.putBoolean("myReport", false);
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
			
			filterView.setFocusable(true);
			filterView.setFocusableInTouchMode(true);
			filterView.setOnKeyListener(listener);

			final RadioButton sortNullRadio = (RadioButton)filterView.findViewById(R.id.sortNullRadio);
			final RadioButton sortItemsCountRadio = (RadioButton)filterView.findViewById(R.id.sortItemsCountRadio);
			final RadioButton sortAmountRadio = (RadioButton)filterView.findViewById(R.id.sortAmountRadio);	
			final RadioButton sortCreateDateRadio = (RadioButton)filterView.findViewById(R.id.sortCreateDateRadio);
			RadioGroup sortRadioGroup = (RadioGroup)filterView.findViewById(R.id.sortRadioGroup);
			sortRadioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener()
			{
				public void onCheckedChanged(RadioGroup group, int checkedId)
				{
					if (checkedId == sortNullRadio.getId())
					{
						MobclickAgent.onEvent(getActivity(), "UMENG_SHEET_OTHERS_MODIFY_DATE");
						tempSortType = SORT_NULL;
					}
					else if (checkedId == sortItemsCountRadio.getId())
					{
						MobclickAgent.onEvent(getActivity(), "UMENG_SHEET_OTHERS_ITEMS_COUNT");
						tempSortType = SORT_ITEMS_COUNT;
					}
					else if (checkedId == sortAmountRadio.getId())
					{
						MobclickAgent.onEvent(getActivity(), "UMENG_SHEET_OTHERS_AMOUNT");
						tempSortType = SORT_AMOUNT;
					}
					else if (checkedId == sortCreateDateRadio.getId())
					{
						MobclickAgent.onEvent(getActivity(), "UMENG_SHEET_OTHERS_CREATE_DATE");
						tempSortType = SORT_CREATE_DATE;
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
					MobclickAgent.onEvent(getActivity(), "UMENG_SHEET_OTHERS_TAG");
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
					ReimApplication.showProgressDialog();

					othersList.clear();
					othersList.addAll(dbManager.getOthersReports(appPreference.getCurrentUserID()));
					filterReportList();
					othersAdapter.set(showOthersList);
					othersAdapter.notifyDataSetChanged();	
					
					ReimApplication.dismissProgressDialog();
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
	
	private void filterReportList()
	{
		showOthersList.clear();
		for (Report report : othersList)
		{
			if (filterStatusList.size() > 0 && filterStatusList.size() < 5)
			{
				if (!report.isInSpecificStatus(filterStatusList))
				{
					continue;
				}
			}
			showOthersList.add(report);
		}

		if (sortType == SORT_NULL)
		{
			Report.sortByModifyDate(showOthersList);
		}
		if (sortType == SORT_AMOUNT)
		{
			Report.sortByAmount(showOthersList);
		}
		if (sortType == SORT_ITEMS_COUNT)
		{
			Report.sortByItemsCount(showOthersList);
		}
		if (sortType == SORT_CREATE_DATE)
		{
			Report.sortByCreateDate(showOthersList);
		}
		
		if (sortReverse)
		{
			Collections.reverse(showOthersList);
		}
	}

	private void getSubordinatesReports()
	{
		SubordinatesReportRequest request = new SubordinatesReportRequest(0, 9999, 1);
		request.sendRequest(new HttpConnectionCallback()
		{
			public void execute(Object httpResponse)
			{
				final SubordinatesReportResponse response = new SubordinatesReportResponse(httpResponse);
				if (response.getStatus())
				{
					int managerID = appPreference.getCurrentUserID();
					List<Report> reportList = response.getReportList();
					dbManager.deleteOthersReports(managerID);
					
					for (Report report : reportList)
					{
						report.setManagerID(managerID);
						dbManager.insertOthersReport(report);
					}
					
					othersList = dbManager.getOthersReports(appPreference.getCurrentUserID());
					filterReportList();
					othersAdapter.set(showOthersList);
					
					getActivity().runOnUiThread(new Runnable()
					{
						public void run()
						{
							othersListView.stopRefresh();
							othersListView.stopLoadMore();
							othersListView.setRefreshTime(Utils.secondToStringUpToMinute(Utils.getCurrentTime()));
							othersAdapter.notifyDataSetChanged();
						}
					});
				}
				else
				{
					getActivity().runOnUiThread(new Runnable()
					{
						public void run()
						{
							othersListView.stopRefresh();
							othersListView.stopLoadMore();
							Toast.makeText(getActivity(), "获取数据失败" + response.getErrorMessage(), Toast.LENGTH_SHORT).show();
						}
					});					
				}
			}
		});
	}

	public void onRefresh()
	{
		if (Utils.isNetworkConnected(getActivity()))
		{
			getSubordinatesReports();
		}
		else
		{
			getActivity().runOnUiThread(new Runnable()
			{
				public void run()
				{
					othersListView.stopRefresh();
					Toast.makeText(getActivity(), "网络未连接，无法刷新", Toast.LENGTH_SHORT).show();
				}
			});
		}
	}

	public void onLoadMore()
	{
		if (Utils.isNetworkConnected(getActivity()))
		{
			getSubordinatesReports();
		}
		else
		{
			getActivity().runOnUiThread(new Runnable()
			{
				public void run()
				{
					othersListView.stopLoadMore();
					Toast.makeText(getActivity(), "网络未连接，无法刷新", Toast.LENGTH_SHORT).show();
				}
			});
		}		
	}
}
