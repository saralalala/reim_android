package com.rushucloud.reim;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import netUtils.HttpConnectionCallback;
import netUtils.SyncDataCallback;
import netUtils.SyncUtils;
import netUtils.Request.Report.DeleteReportRequest;
import netUtils.Request.Report.ExportReportRequest;
import netUtils.Request.Report.SubordinatesReportRequest;
import netUtils.Response.Report.DeleteReportResponse;
import netUtils.Response.Report.ExportReportResponse;
import netUtils.Response.Report.SubordinatesReportResponse;

import classes.AppPreference;
import classes.ReimApplication;
import classes.Report;
import classes.User;
import classes.Utils;
import classes.Adapter.OthersReportListViewAdapter;
import classes.Adapter.ReportListViewAdapter;
import classes.Adapter.ReportTagGridViewAdapter;
import classes.Widget.SegmentedGroup;
import classes.Widget.XListView;
import classes.Widget.XListView.IXListViewListener;

import com.umeng.analytics.MobclickAgent;

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
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.WindowManager.LayoutParams;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.support.v4.app.Fragment;

public class ReportFragment extends Fragment implements OnKeyListener, OnClickListener, IXListViewListener
{
	private static final int SORT_NULL = 0;	
	private static final int SORT_ITEMS_COUNT = 1;	
	private static final int SORT_AMOUNT = 2;	
	private static final int SORT_CREATE_DATE = 3;

	private boolean hasInit = false;
	
	private View view;
	private View filterView;
	private ImageView filterImageView;
	private TextView myTitleTextView;
	private TextView othersTitleTextView;
	private XListView reportListView;
	private ReportListViewAdapter mineAdapter;
	private OthersReportListViewAdapter othersAdapter;

	private WindowManager windowManager;
	private LayoutParams params = new LayoutParams();
	private AppPreference appPreference;
	private DBManager dbManager;
	
	private List<Report> mineList = new ArrayList<Report>();
	private List<Report> showMineList = new ArrayList<Report>();	
	private int mineSortType = SORT_NULL;
	private boolean mineSortReverse = false;
	private int mineTempSortType = SORT_NULL;	
	private List<Integer> mineFilterStatusList = new ArrayList<Integer>();
	
	private List<Report> othersList = new ArrayList<Report>();
	private List<Report> showOthersList = new ArrayList<Report>();
	private int othersSortType = SORT_NULL;
	private boolean othersSortReverse = false;
	private int othersTempSortType = SORT_NULL;	
	private List<Integer> othersFilterStatusList = new ArrayList<Integer>();
	
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
	    return view;  
	}
	   
	public void onResume()
	{
		super.onResume();
		MobclickAgent.onPageStart("ReportFragment");
		if (!hasInit)
		{
	        initView();
	        initData();
	        hasInit = true;
			setListView(ReimApplication.getReportTabIndex());
			syncReports();			
		}
	}

	public void onPause()
	{
		super.onPause();
		MobclickAgent.onPageEnd("ReportFragment");
	}
	
	public void setUserVisibleHint(boolean isVisibleToUser)
	{
		super.setUserVisibleHint(isVisibleToUser);
		if (isVisibleToUser && hasInit)
		{
			setListView(ReimApplication.getReportTabIndex());
			syncReports();
		}
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
    	if (!getUserVisibleHint())
		{
			return false;
		}
    	
    	AdapterContextMenuInfo menuInfo=(AdapterContextMenuInfo)item.getMenuInfo();
    	int index = (int)reportListView.getAdapter().getItemId(menuInfo.position);
    	final Report report = showMineList.get(index);
    	switch (item.getItemId()) 
    	{
			case 0:
			{
				if (report.isEditable())
				{
					AlertDialog mDialog = new AlertDialog.Builder(getActivity())
														.setTitle("警告")
														.setMessage(R.string.delete_report_warning)
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
																	Utils.showToast(getActivity(), "网络未连接，无法删除");
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
					Utils.showToast(getActivity(), "报告已提交，不可删除");
				}
				break;
			}
			case 1:
			{
				if (!Utils.isNetworkConnected())
				{
					Utils.showToast(getActivity(), "网络未连接，无法导出");
				}
				else if (report.getStatus() != Report.STATUS_FINISHED && report.getStatus() != Report.STATUS_APPROVED)
				{
					Utils.showToast(getActivity(), "报销未完成，不可导出");					
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
		
		mineList.addAll(readMineReportList());
		showMineList.addAll(filterReportList(mineList, mineSortType, mineSortReverse, mineFilterStatusList));
    }

	private void initView()
	{
		if (mineAdapter == null)
		{
			mineAdapter = new ReportListViewAdapter(getActivity(), showMineList);			
		}
		
		if (othersAdapter == null)
		{
			othersAdapter = new OthersReportListViewAdapter(getActivity(), showOthersList);			
		}
		
		if (myTitleTextView == null)
		{
			myTitleTextView = (TextView)getActivity().findViewById(R.id.myTitleTextView);
			myTitleTextView.setOnClickListener(this);
		}
		
		if (othersTitleTextView == null)
		{
			othersTitleTextView = (TextView)getActivity().findViewById(R.id.othersTitleTextView);
			othersTitleTextView.setOnClickListener(this);
		}
		
		if (reportListView == null)
		{
			reportListView = (XListView)getActivity().findViewById(R.id.reportListView);
			reportListView.setAdapter(mineAdapter);
			reportListView.setXListViewListener(this);
			reportListView.setPullLoadEnable(true);
			reportListView.setPullRefreshEnable(true);
			reportListView.setOnItemClickListener(new OnItemClickListener()
			{
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id)
				{
					if (ReimApplication.getReportTabIndex() == 0)
					{
						Report report = showMineList.get(position-1);
						Bundle bundle = new Bundle();
						bundle.putSerializable("report", report);
						Intent intent;
						if (report.isEditable())
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
					else
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
				}
			});
			registerForContextMenu(reportListView);
		}
		
		if (filterImageView == null)
		{
			filterImageView = (ImageView) view.findViewById(R.id.filterImageView);
			filterImageView.setOnClickListener(new OnClickListener()
			{
				public void onClick(View v)
				{
					MobclickAgent.onEvent(getActivity(), "UMENG_SHEET_CLICK");
					windowManager.addView(filterView, params);
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
			filterView.setOnKeyListener(this);

			final RadioButton sortNullRadio = (RadioButton)filterView.findViewById(R.id.sortNullRadio);
			final RadioButton sortItemsCountRadio = (RadioButton)filterView.findViewById(R.id.sortItemsCountRadio);
			final RadioButton sortAmountRadio = (RadioButton)filterView.findViewById(R.id.sortAmountRadio);	
			final RadioButton sortCreateDateRadio = (RadioButton)filterView.findViewById(R.id.sortCreateDateRadio);
			SegmentedGroup sortRadioGroup = (SegmentedGroup)filterView.findViewById(R.id.sortRadioGroup);
			sortRadioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener()
			{
				public void onCheckedChanged(RadioGroup group, int checkedId)
				{
					if (ReimApplication.getReportTabIndex() == 0)
					{
						if (checkedId == sortNullRadio.getId())
						{
							MobclickAgent.onEvent(getActivity(), "UMENG_SHEET_MY_MODIFY_DATE");
							mineTempSortType = SORT_NULL;
						}
						else if (checkedId == sortItemsCountRadio.getId())
						{
							MobclickAgent.onEvent(getActivity(), "UMENG_SHEET_MY_ITEMS_COUNT");
							mineTempSortType = SORT_ITEMS_COUNT;
						}
						else if (checkedId == sortAmountRadio.getId())
						{
							MobclickAgent.onEvent(getActivity(), "UMENG_SHEET_MY_AMOUNT");
							mineTempSortType = SORT_AMOUNT;
						}
						else if (checkedId == sortCreateDateRadio.getId())
						{
							MobclickAgent.onEvent(getActivity(), "UMENG_SHEET_MY_CREATE_DATE");
							mineTempSortType = SORT_CREATE_DATE;
						}						
					}
					else
					{
						if (checkedId == sortNullRadio.getId())
						{
							MobclickAgent.onEvent(getActivity(), "UMENG_SHEET_OTHERS_MODIFY_DATE");
							othersTempSortType = SORT_NULL;
						}
						else if (checkedId == sortItemsCountRadio.getId())
						{
							MobclickAgent.onEvent(getActivity(), "UMENG_SHEET_OTHERS_ITEMS_COUNT");
							othersTempSortType = SORT_ITEMS_COUNT;
						}
						else if (checkedId == sortAmountRadio.getId())
						{
							MobclickAgent.onEvent(getActivity(), "UMENG_SHEET_OTHERS_AMOUNT");
							othersTempSortType = SORT_AMOUNT;
						}
						else if (checkedId == sortCreateDateRadio.getId())
						{
							MobclickAgent.onEvent(getActivity(), "UMENG_SHEET_OTHERS_CREATE_DATE");
							othersTempSortType = SORT_CREATE_DATE;
						}						
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
			
			ImageView confirmImageView = (ImageView)filterView.findViewById(R.id.confirmImageView);
			confirmImageView.setOnClickListener(new View.OnClickListener()
			{
				public void onClick(View v)
				{
					if (ReimApplication.getReportTabIndex() == 0)
					{
						mineSortReverse = mineSortType == mineTempSortType ? !mineSortReverse : false;
						mineSortType = mineTempSortType;
						mineFilterStatusList.clear();
						mineFilterStatusList.addAll(tagAdapter.getFilterStatusList());						
					}
					else
					{
						othersSortReverse = othersSortType == othersTempSortType ? !othersSortReverse : false;
						othersSortType = othersTempSortType;
						othersFilterStatusList.clear();
						othersFilterStatusList.addAll(tagAdapter.getFilterStatusList());
					}
					
					windowManager.removeView(filterView);
					ReimApplication.showProgressDialog();
					refreshReportListView();
					ReimApplication.dismissProgressDialog();
				}
			});

			ImageView cancelImageView = (ImageView)filterView.findViewById(R.id.cancelImageView);
			cancelImageView.setOnClickListener(new View.OnClickListener()
			{
				public void onClick(View v)
				{
					windowManager.removeView(filterView);
				}
			});
		}
	}
	
	private void setListView(int index)
	{
		ReimApplication.setReportTabIndex(index);
		if (index == 0)
		{
			myTitleTextView.setTextColor(getResources().getColor(R.color.major_light));
			othersTitleTextView.setTextColor(getResources().getColor(R.color.hint_white));
			registerForContextMenu(reportListView);
		}
		else
		{
			if (Utils.isNetworkConnected())
			{
				sendSubordinatesReportsRequest();
			}
			myTitleTextView.setTextColor(getResources().getColor(R.color.hint_white));
			othersTitleTextView.setTextColor(getResources().getColor(R.color.major_light));
			unregisterForContextMenu(reportListView);
		}
		ReimApplication.showProgressDialog();
		refreshReportListView();
		ReimApplication.dismissProgressDialog();
	}
	
	private List<Report> readMineReportList()
	{
		return dbManager.getUserReports(appPreference.getCurrentUserID());
	}
	
	private List<Report> readOthersReportList()
	{
		return dbManager.getOthersReports(appPreference.getCurrentUserID());
	}
	
	private void refreshReportListView()
	{
		if (ReimApplication.getReportTabIndex() == 0)
		{
			mineList.clear();
			mineList.addAll(readMineReportList());
			showMineList.clear();
			showMineList.addAll(filterReportList(mineList, mineSortType, mineSortReverse, mineFilterStatusList));
			mineAdapter.set(showMineList);
			if (reportListView.getAdapter().equals(mineAdapter))
			{
				mineAdapter.notifyDataSetChanged();
			}
			else
			{
				reportListView.setAdapter(mineAdapter);				
			}	
		}
		else
		{
			othersList.clear();
			othersList.addAll(readOthersReportList());
			showOthersList.clear();
			showOthersList.addAll(filterReportList(othersList, othersSortType, othersSortReverse, othersFilterStatusList));
			othersAdapter.set(showOthersList);
			if (reportListView.getAdapter().equals(othersAdapter))
			{
				othersAdapter.notifyDataSetChanged();
			}
			else
			{
				reportListView.setAdapter(othersAdapter);				
			}		
		}
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
											Utils.showToast(getActivity(), "邮箱不能为空");
										}
										else if (!Utils.isEmail(email))
										{
											Utils.showToast(getActivity(), "邮箱格式不正确");
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
	
	private List<Report> filterReportList(List<Report> reportList, int sortType, boolean sortReverse, List<Integer> filterStatusList)
	{
		List<Report> resultList = new ArrayList<Report>();
		for (Report report : reportList)
		{
			if (filterStatusList.size() > 0 && filterStatusList.size() < 5)
			{
				if (!report.isInSpecificStatus(filterStatusList))
				{
					continue;
				}
			}
			resultList.add(report);
		}

		if (sortType == SORT_NULL)
		{
			Report.sortByUpdateDate(resultList);
		}
		if (sortType == SORT_AMOUNT)
		{
			Report.sortByAmount(resultList);
		}
		if (sortType == SORT_ITEMS_COUNT)
		{
			Report.sortByItemsCount(resultList);
		}
		if (sortType == SORT_CREATE_DATE)
		{
			Report.sortByCreateDate(resultList);
		}
		
		if (sortReverse)
		{
			Collections.reverse(resultList);
		}

		return resultList;
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
				            Utils.showToast(getActivity(), R.string.delete_failed);
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
							Utils.showToast(getActivity(), "报告导出成功");
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
							Utils.showToast(getActivity(), "报告导出失败");
						}
					});
				}
			}
		});
	}

	private void sendSubordinatesReportsRequest()
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
						dbManager.insertOthersReport(report);
						dbManager.deleteOthersReportItems(report.getServerID());
					}
					
					getActivity().runOnUiThread(new Runnable()
					{
						public void run()
						{
							reportListView.stopRefresh();
							reportListView.stopLoadMore();
							reportListView.setRefreshTime(Utils.secondToStringUpToMinute(Utils.getCurrentTime()));
							refreshReportListView();
						}
					});
				}
				else
				{
					getActivity().runOnUiThread(new Runnable()
					{
						public void run()
						{
							reportListView.stopRefresh();
							reportListView.stopLoadMore();
							Utils.showToast(getActivity(), "获取数据失败" + response.getErrorMessage());
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
			refreshReportListView();
			ReimApplication.dismissProgressDialog();
            Utils.showToast(getActivity(), R.string.delete_succeed);														
		}
		else
		{
			ReimApplication.dismissProgressDialog();
            Utils.showToast(getActivity(), R.string.delete_failed);
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
							refreshReportListView();
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

	private void refreshReports()
	{
		if (ReimApplication.getReportTabIndex() == 0)
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
								reportListView.stopRefresh();
								reportListView.setRefreshTime(Utils.secondToStringUpToMinute(Utils.getCurrentTime()));
								refreshReportListView();
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
						reportListView.stopRefresh();
						String prompt = SyncUtils.isSyncOnGoing ? "正在同步中" : "未打开同步开关或未打开Wifi，无法刷新";
						Utils.showToast(getActivity(), prompt);
					}
				});
			}			
		}	
		else
		{
			if (Utils.isNetworkConnected())
			{
				sendSubordinatesReportsRequest();
			}
			else
			{
				getActivity().runOnUiThread(new Runnable()
				{
					public void run()
					{
						reportListView.stopRefresh();
						Utils.showToast(getActivity(), "网络未连接，无法刷新");
					}
				});
			}			
		}		
	}
	
	public boolean onKey(View v, int keyCode, KeyEvent event)
	{
		if (keyCode == KeyEvent.KEYCODE_BACK)
		{
			windowManager.removeView(filterView);
		}
		return false;
	}

	public void onClick(View v)
	{
		if (v.equals(myTitleTextView))
		{
			setListView(0);			
		}
		else
		{
			setListView(1);
		}
	}
	
	public void onRefresh()
	{
		refreshReports();
	}

	public void onLoadMore()
	{
		refreshReports();
	}	
}