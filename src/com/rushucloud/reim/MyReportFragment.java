package com.rushucloud.reim;

import java.util.ArrayList;
import java.util.List;

import com.umeng.analytics.MobclickAgent;

import netUtils.HttpConnectionCallback;
import netUtils.SyncDataCallback;
import netUtils.SyncUtils;
import netUtils.Request.Report.DeleteReportRequest;
import netUtils.Response.Report.DeleteReportResponse;


import classes.AppPreference;
import classes.ReimApplication;
import classes.Report;
import classes.Utils;
import classes.XListView;
import classes.Adapter.ReportListViewAdapter;
import classes.Adapter.ReportTagGridViewAdapter;
import classes.XListView.IXListViewListener;
import database.DBManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.support.v4.app.Fragment;

public class MyReportFragment extends Fragment implements IXListViewListener
{	
	private static final int SORT_NULL = 0;	
	private static final int SORT_ITEMS_COUNT = 1;	
	private static final int SORT_AMOUNT = 2;	
	private static final int SORT_CREATE_DATE = 3;	
	private static final int SORT_MODIFY_DATE = 4;	
	
	private View view;
	private View filterView;
	private Button addButton;
	private XListView mineListView;
	private ReportListViewAdapter mineAdapter;

	private WindowManager windowManager;
	private LayoutParams params = new LayoutParams();
	private DBManager dbManager;
	private List<Report> mineList = new ArrayList<Report>();
	private List<Report> showMineList = new ArrayList<Report>();
	
	private int sortType = SORT_NULL;
	private boolean sortReverse = false;
	
	private int tempSortType = SORT_NULL;
	
	private List<Integer> filterStatusList = new ArrayList<Integer>();
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		if (view == null)
		{
			view = inflater.inflate(R.layout.report_mine, container, false);
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
		MobclickAgent.onPageStart("MyReportFragment");	
		ReimApplication.pDialog.show();
        viewInitialise();
        dataInitialise();
		refreshMineReportListView();
		ReimApplication.pDialog.dismiss();
		syncReports();
	}

	public void onPause()
	{
		super.onPause();
		MobclickAgent.onPageEnd("MyReportFragment");
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
			mineListView = (XListView)getActivity().findViewById(R.id.mineListView);
			mineListView.setAdapter(mineAdapter);
			mineListView.setXListViewListener(this);
			mineListView.setPullLoadEnable(true);
			mineListView.setPullRefreshEnable(true);
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
					refreshMineReportListView();
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
	
	private List<Report> readMineReportList()
	{
		AppPreference appPreference = AppPreference.getAppPreference();
		DBManager dbManager = DBManager.getDBManager();
		return dbManager.getUserReports(appPreference.getCurrentUserID());
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

	private List<Report> filterReportList(List<Report> reportList)
	{
		List<Report> newReportList = new ArrayList<Report>(reportList);
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

	private void syncReports()
	{
		if (Utils.canSyncToServer(getActivity()))
		{
			SyncUtils.syncFromServer(new SyncDataCallback()
			{
				public void execute()
				{
					getActivity().runOnUiThread(new Runnable()
					{
						public void run()
						{
							refreshMineReportListView();
						}
					});

					SyncUtils.syncAllToServer(null);
				}
			});
		}
	}

	public void onRefresh()
	{
		new Handler().postDelayed(new Runnable()
		{
			public void run()
			{
				mineListView.stopRefresh();
				mineListView.setRefreshTime(Utils.secondToStringUpToMinute(Utils.getCurrentTime()));
			}
		}, 2000);
	}

	public void onLoadMore()
	{
		new Handler().postDelayed(new Runnable()
		{
			public void run()
			{
				mineListView.stopLoadMore();
			}
		}, 2000);		
	}
}