package com.rushucloud.reim;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.umeng.analytics.MobclickAgent;

import netUtils.HttpConnectionCallback;
import netUtils.SyncDataCallback;
import netUtils.SyncUtils;
import netUtils.Request.Report.DeleteReportRequest;
import netUtils.Request.Report.ExportReportRequest;
import netUtils.Response.Report.DeleteReportResponse;
import netUtils.Response.Report.ExportReportResponse;


import classes.AppPreference;
import classes.ReimApplication;
import classes.Report;
import classes.User;
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
import android.util.DisplayMetrics;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnKeyListener;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
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
	
	private View view;
	private View filterView;
	private Button addButton;
	private XListView mineListView;
	private ReportListViewAdapter mineAdapter;

	private WindowManager windowManager;
	private LayoutParams params = new LayoutParams();
	private AppPreference appPreference;
	private DBManager dbManager;
	private List<Report> mineList = new ArrayList<Report>();
	private List<Report> showMineList = new ArrayList<Report>();
	
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
		ReimApplication.showProgressDialog();
        initView();
        initData();
		refreshMineReportListView();
		ReimApplication.dismissProgressDialog();
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
			MobclickAgent.onEvent(getActivity(), "UMENG_SHEET_MY_REPORT_CLICK");
			windowManager.addView(filterView, params);
		}
			
		return super.onOptionsItemSelected(item);
	}
	
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo)
    {
    	super.onCreateContextMenu(menu, v, menuInfo);
    	menu.setHeaderTitle("选项");
    	menu.add(0,0,0,"删除");
    	menu.add(0,1,0,"导出");
    }

    public boolean onContextItemSelected(MenuItem item)
    {
    	AdapterContextMenuInfo menuInfo=(AdapterContextMenuInfo)item.getMenuInfo();
    	int index = (int)mineListView.getAdapter().getItemId(menuInfo.position);
    	final Report report = showMineList.get(index);
    	switch (item.getItemId()) 
    	{
			case 0:
			{
				if (report.getStatus() == Report.STATUS_DRAFT || report.getStatus() == Report.STATUS_REJECTED)
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
																else if (!Utils.isNetworkConnected())
																{
																	Toast.makeText(getActivity(), "网络未连接，无法删除", Toast.LENGTH_SHORT).show();
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
			}
			case 1:
			{
				if (!Utils.isNetworkConnected())
				{
					Toast.makeText(getActivity(), "网络未连接，无法导出", Toast.LENGTH_SHORT).show();
				}
				else if (report.getStatus() != Report.STATUS_FINISHED && report.getStatus() != Report.STATUS_APPROVED)
				{
					Toast.makeText(getActivity(), "报销未完成，不可导出", Toast.LENGTH_SHORT).show();					
				}
				else
				{
					showExportDialog(report.getServerID());
				}
				break;
			}
			default:
				break;
		}    		
		
    	return super.onContextItemSelected(item);
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
		
		ReimApplication.setTabIndex(1);
		ReimApplication.setReportTabIndex(0);
    }

	private void initView()
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
					Report report = showMineList.get(position-1);
					Bundle bundle = new Bundle();
					bundle.putSerializable("report", report);
					Intent intent;
					if (report.getStatus() == Report.STATUS_DRAFT || report.getStatus() == Report.STATUS_REJECTED)
					{
						intent = new Intent(getActivity(), EditReportActivity.class);
					}
					else
					{
						bundle.putBoolean("myReport", true);
						intent = new Intent(getActivity(), ShowReportActivity.class);						
					}
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
						MobclickAgent.onEvent(getActivity(), "UMENG_SHEET_MY_MODIFY_DATE");
						tempSortType = SORT_NULL;
					}
					else if (checkedId == sortItemsCountRadio.getId())
					{
						MobclickAgent.onEvent(getActivity(), "UMENG_SHEET_MY_ITEMS_COUNT");
						tempSortType = SORT_ITEMS_COUNT;
					}
					else if (checkedId == sortAmountRadio.getId())
					{
						MobclickAgent.onEvent(getActivity(), "UMENG_SHEET_MY_AMOUNT");
						tempSortType = SORT_AMOUNT;
					}
					else if (checkedId == sortCreateDateRadio.getId())
					{
						MobclickAgent.onEvent(getActivity(), "UMENG_SHEET_MY_CREATE_DATE");
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
					MobclickAgent.onEvent(getActivity(), "UMENG_SHEET_MY_TAG");
					tagAdapter.setSelection(position);
					tagAdapter.notifyDataSetChanged();
				}
			});
			
			Button confirmButton = (Button)filterView.findViewById(R.id.confirmButton);
			confirmButton.setOnClickListener(new View.OnClickListener()
			{
				public void onClick(View v)
				{
					sortReverse = sortType == tempSortType ? !sortReverse : false;
					sortType = tempSortType;
					filterStatusList.clear();
					filterStatusList.addAll(tagAdapter.getFilterStatusList());
					
					windowManager.removeView(filterView);
					ReimApplication.showProgressDialog();
					refreshMineReportListView();
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
	
	private List<Report> readMineReportList()
	{
		return dbManager.getUserReports(appPreference.getCurrentUserID());
	}
	
	private void refreshMineReportListView()
	{
		mineList.clear();
		mineList.addAll(readMineReportList());
		filterReportList();
		mineAdapter.set(showMineList);
		mineAdapter.notifyDataSetChanged();
	}
	
	private void showExportDialog(final int reportID)
    {
		View view = View.inflate(getActivity(), R.layout.report_export_dialog, null);
		final EditText emailEditText = (EditText)view.findViewById(R.id.emailEditText);
		User user = appPreference.getCurrentUser();
		if (!user.getEmail().equals(""))
		{
			emailEditText.setText(user.getEmail());
		}
		emailEditText.requestFocus();
		
    	AlertDialog mDialog = new AlertDialog.Builder(getActivity())
								.setTitle("导出报告")
								.setView(view)
								.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener()
								{
									public void onClick(DialogInterface dialog, int which)
									{
										String email = emailEditText.getText().toString();
										if (email.equals(""))
										{
											Toast.makeText(getActivity(), "邮箱不能为空", Toast.LENGTH_SHORT).show();
										}
										else if (!Utils.isEmail(email))
										{
											Toast.makeText(getActivity(), "邮箱格式不正确", Toast.LENGTH_SHORT).show();
										}
										else
										{
											sendExportReportRequest(reportID, email);
										}
									}
								})
								.setNegativeButton(R.string.cancel, null)
								.create();
		mDialog.show();
    }
	
	private void filterReportList()
	{
		showMineList.clear();
		for (Report report : mineList)
		{
			if (filterStatusList.size() > 0 && filterStatusList.size() < 5)
			{
				if (!report.isInSpecificStatus(filterStatusList))
				{
					continue;
				}
			}
			showMineList.add(report);
		}

		if (sortType == SORT_NULL)
		{
			Report.sortByUpdateDate(showMineList);
		}
		if (sortType == SORT_AMOUNT)
		{
			Report.sortByAmount(showMineList);
		}
		if (sortType == SORT_ITEMS_COUNT)
		{
			Report.sortByItemsCount(showMineList);
		}
		if (sortType == SORT_CREATE_DATE)
		{
			Report.sortByCreateDate(showMineList);
		}
		
		if (sortReverse)
		{
			Collections.reverse(showMineList);
		}
	}
	
	private void sendDeleteReportRequest(final Report report)
	{
		ReimApplication.showProgressDialog();
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
							ReimApplication.dismissProgressDialog();
				            Toast.makeText(getActivity(), R.string.deleteFailed, Toast.LENGTH_SHORT).show();
						}
					});		
				}
			}
		});
	}

	private void sendExportReportRequest(int reportID, String email)
	{
    	ReimApplication.showProgressDialog();
		ExportReportRequest request = new ExportReportRequest(reportID, email);
		request.sendRequest(new HttpConnectionCallback()
		{
			public void execute(Object httpResponse)
			{
				ExportReportResponse response = new ExportReportResponse(httpResponse);
				if (response.getStatus())
				{
					getActivity().runOnUiThread(new Runnable()
					{
						public void run()
						{
							ReimApplication.dismissProgressDialog();
							Toast.makeText(getActivity(), "报告导出成功", Toast.LENGTH_SHORT).show();
						}
					});
				}
				else
				{
					getActivity().runOnUiThread(new Runnable()
					{
						public void run()
						{
							ReimApplication.dismissProgressDialog();
							Toast.makeText(getActivity(), "报告导出失败", Toast.LENGTH_SHORT).show();
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
			ReimApplication.dismissProgressDialog();
            Toast.makeText(getActivity(), R.string.deleteSucceed, Toast.LENGTH_SHORT).show();														
		}
		else
		{
			ReimApplication.dismissProgressDialog();
            Toast.makeText(getActivity(), R.string.deleteFailed, Toast.LENGTH_SHORT).show();
		}		
	}

	private void syncReports()
	{
		if (SyncUtils.canSyncToServer())
		{
			SyncUtils.isSyncOnGoing = true;
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

					SyncUtils.syncAllToServer(new SyncDataCallback()
					{
						public void execute()
						{
							SyncUtils.isSyncOnGoing = false;
						}
					});
				}
			});
		}
	}

	public void onRefresh()
	{
		if (SyncUtils.canSyncToServer())
		{
			SyncUtils.isSyncOnGoing = true;
			SyncUtils.syncFromServer(new SyncDataCallback()
			{
				public void execute()
				{
					getActivity().runOnUiThread(new Runnable()
					{
						public void run()
						{
							mineListView.stopRefresh();
							mineListView.setRefreshTime(Utils.secondToStringUpToMinute(Utils.getCurrentTime()));
							refreshMineReportListView();
						}
					});

					SyncUtils.syncAllToServer(new SyncDataCallback()
					{
						public void execute()
						{
							SyncUtils.isSyncOnGoing = false;
						}
					});
				}
			});
		}
		else
		{
			getActivity().runOnUiThread(new Runnable()
			{
				public void run()
				{
					mineListView.stopRefresh();
					String prompt = SyncUtils.isSyncOnGoing ? "正在同步中" : "未打开同步开关或未打开Wifi，无法刷新";
					Toast.makeText(getActivity(), prompt, Toast.LENGTH_SHORT).show();
				}
			});
		}		
	}

	public void onLoadMore()
	{
		if (SyncUtils.canSyncToServer())
		{
			SyncUtils.isSyncOnGoing = true;
			SyncUtils.syncFromServer(new SyncDataCallback()
			{
				public void execute()
				{
					getActivity().runOnUiThread(new Runnable()
					{
						public void run()
						{
							mineListView.stopLoadMore();
							mineListView.setRefreshTime(Utils.secondToStringUpToMinute(Utils.getCurrentTime()));
							refreshMineReportListView();
						}
					});


					SyncUtils.syncAllToServer(new SyncDataCallback()
					{
						public void execute()
						{
							SyncUtils.isSyncOnGoing = false;
						}
					});
				}
			});
		}
		else
		{
			getActivity().runOnUiThread(new Runnable()
			{
				public void run()
				{
					mineListView.stopLoadMore();
					String prompt = SyncUtils.isSyncOnGoing ? "正在同步中" : "未打开同步开关或未打开Wifi，无法刷新";
					Toast.makeText(getActivity(), prompt, Toast.LENGTH_SHORT).show();
				}
			});
		}	
	}
}
