package com.rushucloud.reim;

import java.util.ArrayList;
import java.util.List;

import com.umeng.analytics.MobclickAgent;

import netUtils.HttpConnectionCallback;
import netUtils.Request.Report.DeleteReportRequest;
import netUtils.Response.Report.DeleteReportResponse;


import classes.AppPreference;
import classes.Item;
import classes.ReimApplication;
import classes.Report;
import classes.Tag;
import classes.Utils;
import classes.Adapter.ReportListViewAdapter;
import database.DBManager;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TabHost;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.support.v4.app.Fragment;

public class ReportFragment extends Fragment
{
	private static final int LIST_FILTER_ALL = 0;
	private static final int LIST_FILTER_DRAFT = 1;
	private static final int LIST_FILTER_SUBMITTED = 2;
	private static final int LIST_FILTER_APPROVED = 3;
	private static final int LIST_FILTER_REJECTED = 4;	
	private static final int LIST_FILTER_FINISHED = 5;	
	private static final int LIST_SORT_AMOUNT = 6;	
	private static final int LIST_SORT_DATE = 7;	
	private static final int LIST_SORT_ITEMS_NUMBER = 8;	
	
	private TabHost tabHost;
	private View view;
	private Button addButton;
	private ListView mineListView;
	private ListView approveListView;
	private ReportListViewAdapter mineAdapter;
	private ReportListViewAdapter approveAdapter;
	
	private DBManager dbManager;
	private List<Report> mineList = new ArrayList<Report>();
	private List<Report> approveList = new ArrayList<Report>();
	private List<Report> showMineList = new ArrayList<Report>();
	private List<Report> showApproveList = new ArrayList<Report>();
	
	private int listType;
	
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
	    return view;  
	}
	   
	public void onResume()
	{
		super.onResume();
		MobclickAgent.onPageStart("ReportFragment");	
        viewInitialise();
        dataInitialise();
		refreshMineReportListView();
		refreshApproveReportListView();
//		setHasOptionsMenu(true);
	}

	public void onPause()
	{
		super.onPause();
		MobclickAgent.onPageEnd("ReportFragment");
	}
	
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{
		inflater.inflate(R.menu.report, menu);
		Spinner spinner = (Spinner)menu.findItem(R.id.action_filter_item).getActionView();
		SpinnerAdapter spinnerAdapter = ArrayAdapter.createFromResource(getActivity(), 
				R.array.reportSpinner, R.layout.spinner_drop_down_item);
		spinner.setAdapter(spinnerAdapter);		
		spinner.setOnItemSelectedListener(new OnItemSelectedListener()
		{
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
			{
				listType = position;
//				if (position != 4)
//				{
//					Toast.makeText(getActivity(), "这是第"+position+"个", Toast.LENGTH_SHORT).show();
//					refreshItemListView();
//				}
//				else
//				{
//					//TODO alertdialog to let user choose
//				}
			}

			public void onNothingSelected(AdapterView<?> parent)
			{
				Toast.makeText(getActivity(), "Nothing selected", Toast.LENGTH_SHORT).show();
			}
		});
		super.onCreateOptionsMenu(menu, inflater);
	}

	public boolean onOptionsItemSelected(MenuItem item)
	{
		int id = item.getItemId();
		if (id == R.id.action_sort_item)
		{
			//TODO alertdialog to let user choose base
		}
			
		return super.onOptionsItemSelected(item);
	}
	
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo)
    {
    	super.onCreateContextMenu(menu, v, menuInfo);
    	menu.setHeaderTitle("选项");
    	menu.add(0,0,0,"删除");
    }

    public boolean onContextItemSelected(MenuItem item)
    {
    	AdapterContextMenuInfo menuInfo=(AdapterContextMenuInfo)item.getMenuInfo();
    	int index = (int)mineListView.getAdapter().getItemId(menuInfo.position);
    	final Report report = mineList.get(index);
    	switch (item.getItemId()) 
    	{
			case 0:
				if (!Utils.isNetworkConnected(getActivity()))
				{
					Toast.makeText(getActivity(), "网络未连接，无法删除", Toast.LENGTH_SHORT).show();
				}
				else if (report.getStatus() == Report.STATUS_DRAFT || report.getStatus() == Report.STATUS_REJECTED)
				{
					AlertDialog mDialog = new AlertDialog.Builder(getActivity())
														.setTitle("警告")
														.setMessage(R.string.deleteReportWarning)
														.setPositiveButton(R.string.confirm, 
																new DialogInterface.OnClickListener()
														{
															public void onClick(DialogInterface dialog, int which)
															{
																if (report.getServerID() == -1)
																{
																	deleteReportFromLocal(report.getLocalID());
																}
																else
																{
																	sendDeleteReportRequest(report);
																}
															}
														})
														.setNegativeButton(R.string.cancel, null)
														.create();
					mDialog.show();
				}
				else
				{
					Toast.makeText(getActivity(), "报告已提交，不可删除", Toast.LENGTH_SHORT).show();
				}
				break;
			default:
				break;
		}    		
		
    	return super.onContextItemSelected(item);
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
		if (tabHost == null)
		{
			tabHost = (TabHost)getActivity().findViewById(android.R.id.tabhost);
			tabHost.setup();
			LayoutInflater layoutInflater = getActivity().getLayoutInflater();
			layoutInflater.inflate(R.layout.report_mine, tabHost.getTabContentView());
			layoutInflater.inflate(R.layout.report_approve, tabHost.getTabContentView());
			
			tabHost.addTab(tabHost.newTabSpec("myReport")
					.setIndicator(getResources().getString(R.string.myReport))
					.setContent(R.id.mineBaseLayout));
			
			tabHost.addTab(tabHost.newTabSpec("approveReport")
					.setIndicator(getResources().getString(R.string.approveReport))
					.setContent(R.id.approveBaseLayout));
			
			DisplayMetrics dm = new DisplayMetrics();
			getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
			int screenWidth = dm.widthPixels;
			tabHost.getTabWidget().getChildTabViewAt(0).setMinimumWidth(screenWidth / 2);
			tabHost.getTabWidget().getChildTabViewAt(1).setMinimumWidth(screenWidth / 2);
		}
		Bundle bundle = getActivity().getIntent().getExtras();
		int index = bundle == null ? 0 : bundle.getInt("reportTabIndex");
		tabHost.setCurrentTab(index);
		
		if (addButton == null)
		{
			addButton = (Button)getActivity().findViewById(R.id.addButton);
			addButton.setOnClickListener(new View.OnClickListener()
			{
				public void onClick(View v)
				{
					Intent intent = new Intent(getActivity(), EditReportActivity.class);
					startActivity(intent);
				}
			});			
		}

		if (mineAdapter == null)
		{
			mineAdapter = new ReportListViewAdapter(getActivity(), mineList);			
		}
		
		if (mineListView == null)
		{
			mineListView = (ListView)getActivity().findViewById(R.id.mineListView);
			mineListView.setAdapter(mineAdapter);
			mineListView.setOnItemClickListener(new OnItemClickListener()
			{
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id)
				{
					Bundle bundle = new Bundle();
					bundle.putSerializable("report", showMineList.get(position));
					Intent intent = new Intent(getActivity(), EditReportActivity.class);
					intent.putExtras(bundle);
					startActivity(intent);
				}
			});
			registerForContextMenu(mineListView);
		}

		if (approveAdapter == null)
		{
			approveAdapter = new ReportListViewAdapter(getActivity(), approveList);			
		}
		
		if (approveListView == null)
		{
			approveListView = (ListView)getActivity().findViewById(R.id.approveReportListView);
			approveListView.setAdapter(approveAdapter);
			approveListView.setOnItemClickListener(new OnItemClickListener()
			{
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id)
				{
					Report report = showApproveList.get(position);
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
	}
	
	private List<Report> readMineReportList()
	{
		AppPreference appPreference = AppPreference.getAppPreference();
		DBManager dbManager = DBManager.getDBManager();
		return dbManager.getUserReports(appPreference.getCurrentUserID());
	}
	
	private List<Report> readApproveReportList()
	{
		AppPreference appPreference = AppPreference.getAppPreference();
		DBManager dbManager = DBManager.getDBManager();
		return dbManager.getApproveReports(appPreference.getCurrentUserID());
	}
	
	private void refreshMineReportListView()
	{
		mineList.clear();
		mineList.addAll(readMineReportList());
		showMineList.clear();
		showMineList.addAll(filterReportList(mineList));
		mineAdapter.set(showMineList);
		mineAdapter.notifyDataSetChanged();
	}
	
	private void refreshApproveReportListView()
	{
		approveList.clear();
		approveList.addAll(readApproveReportList());
		showApproveList.clear();
		showApproveList.addAll(filterReportList(approveList));
		approveAdapter.set(showApproveList);
		approveAdapter.notifyDataSetChanged();	
	}

	private List<Report> filterReportList(List<Report> reportList)
	{
//		List<Report> newReportList = new ArrayList<Report>();
		List<Report> newReportList = new ArrayList<Report>(reportList);
		//TODO add reports to newReportList from reportList
		switch (listType)
		{
			case LIST_FILTER_ALL:
				
				break;
			case LIST_FILTER_DRAFT:
				
				break;
			case LIST_FILTER_SUBMITTED:
				
				break;
			case LIST_FILTER_APPROVED:
				
				break;
			case LIST_FILTER_REJECTED:
				
				break;
			case LIST_FILTER_FINISHED:
				
				break;
			case LIST_SORT_AMOUNT:
				
				break;
			case LIST_SORT_DATE:
				
				break;
			case LIST_SORT_ITEMS_NUMBER:
				
				break;
			default:
				break;
		}
		return newReportList;
	}
	
	private void sendDeleteReportRequest(final Report report)
	{
		ReimApplication.pDialog.show();
		DeleteReportRequest request = new DeleteReportRequest(report.getServerID());
		request.sendRequest(new HttpConnectionCallback()
		{
			public void execute(Object httpResponse)
			{
				DeleteReportResponse response = new DeleteReportResponse(httpResponse);
				if (response.getStatus())
				{
					getActivity().runOnUiThread(new Runnable()
					{
						public void run()
						{
							deleteReportFromLocal(report.getLocalID());
						}
					});
				}
				else
				{
					getActivity().runOnUiThread(new Runnable()
					{
						public void run()
						{
							ReimApplication.pDialog.dismiss();
				            Toast.makeText(getActivity(), R.string.deleteFailed, Toast.LENGTH_SHORT).show();
						}
					});		
				}
			}
		});
	}
	
	private void deleteReportFromLocal(int reportLocalID)
	{
		if (dbManager.deleteReport(reportLocalID))
		{
			refreshMineReportListView();
			ReimApplication.pDialog.dismiss();
            Toast.makeText(getActivity(), R.string.deleteSucceed, Toast.LENGTH_SHORT).show();														
		}
		else
		{
			ReimApplication.pDialog.dismiss();
            Toast.makeText(getActivity(), R.string.deleteFailed, Toast.LENGTH_SHORT).show();
		}		
	}
}