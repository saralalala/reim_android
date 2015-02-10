package com.rushucloud.reim;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import netUtils.HttpConnectionCallback;
import netUtils.SyncDataCallback;
import netUtils.SyncUtils;
import netUtils.Response.Report.DeleteReportResponse;
import netUtils.Response.Report.ExportReportResponse;
import netUtils.Response.Report.SubordinatesReportResponse;
import netUtils.Request.Report.DeleteReportRequest;
import netUtils.Request.Report.ExportReportRequest;
import netUtils.Request.Report.SubordinatesReportRequest;

import classes.ReimApplication;
import classes.Report;
import classes.User;
import classes.adapter.OthersReportListViewAdapter;
import classes.adapter.ReportListViewAdapter;
import classes.adapter.ReportTagGridViewAdapter;
import classes.utils.AppPreference;
import classes.utils.DBManager;
import classes.utils.PhoneUtils;
import classes.utils.Utils;
import classes.utils.ViewUtils;
import classes.widget.ReimProgressDialog;
import classes.widget.SegmentedGroup;
import classes.widget.XListView;
import classes.widget.XListView.IXListViewListener;

import com.rushucloud.reim.report.ApproveReportActivity;
import com.rushucloud.reim.report.EditReportActivity;
import com.rushucloud.reim.report.ShowReportActivity;
import com.umeng.analytics.MobclickAgent;


import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.support.v4.app.Fragment;

public class ReportFragment extends Fragment implements OnClickListener, IXListViewListener
{
	private static final int SORT_NULL = 0;	
	private static final int SORT_ITEMS_COUNT = 1;	
	private static final int SORT_AMOUNT = 2;	
	private static final int SORT_CREATE_DATE = 3;

	private boolean hasInit = false;
	
	private View view;
	private PopupWindow filterPopupWindow;
	private RelativeLayout noResultLayout;
	private TextView myTitleTextView;
	private TextView othersTitleTextView;
	private ImageView tipImageView;
	private XListView reportListView;
	private ReportListViewAdapter mineAdapter;
	private OthersReportListViewAdapter othersAdapter;
	private PopupWindow operationPopupWindow;
	private PopupWindow deletePopupWindow;

	private AppPreference appPreference;
	private DBManager dbManager;
	
	private List<Report> mineList = new ArrayList<Report>();
	private List<Report> showMineList = new ArrayList<Report>();	
	private int mineSortType = SORT_NULL;
	private boolean mineSortReverse = false;
	private int mineTempSortType = SORT_NULL;
	private boolean[] mineCheck;
	private List<Integer> mineFilterStatusList = new ArrayList<Integer>();
	
	private List<Report> othersList = new ArrayList<Report>();
	private List<Report> showOthersList = new ArrayList<Report>();
	private int othersSortType = SORT_NULL;
	private boolean othersSortReverse = false;
	private int othersTempSortType = SORT_NULL;
	private boolean[] othersCheck;
	private List<Integer> othersFilterStatusList = new ArrayList<Integer>();
	
	private int reportIndex;
	
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
		showBadge();
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
		
		mineCheck = new boolean[5];
		othersCheck = new boolean[5];
		for (int i = 0; i < 5; i++)
		{
			mineCheck[i] = false;
			othersCheck[i] = false;
		}
    }

	private void initView()
	{
		initTitleView();
		initListView();	
		initFilterView();
		initOperationView();
		initDeleteView();
	}
	
	private void initTitleView()
	{
		myTitleTextView = (TextView)getActivity().findViewById(R.id.myTitleTextView);
		myTitleTextView.setOnClickListener(this);

		othersTitleTextView = (TextView)getActivity().findViewById(R.id.othersTitleTextView);
		othersTitleTextView.setOnClickListener(this);

		tipImageView = (ImageView)view.findViewById(R.id.tipImageView);
		
		ImageView filterImageView = (ImageView) view.findViewById(R.id.filterImageView);
		filterImageView.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				MobclickAgent.onEvent(getActivity(), "UMENG_SHEET_CLICK");
				showFilterWindow();
			}
		});
		
		ImageView addImageView = (ImageView) view.findViewById(R.id.addImageView);
		addImageView.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				Intent intent = new Intent(getActivity(), EditReportActivity.class);
				startActivity(intent);
			}
		});
	}
	
	private void initListView()
	{
		mineAdapter = new ReportListViewAdapter(getActivity(), showMineList);	
		othersAdapter = new OthersReportListViewAdapter(getActivity(), showOthersList);
		
		reportListView = (XListView)getActivity().findViewById(R.id.reportListView);
		reportListView.setAdapter(mineAdapter);
		reportListView.setXListViewListener(this);
		reportListView.setPullRefreshEnable(true);
		reportListView.setPullLoadEnable(false);
		reportListView.setOnItemClickListener(new OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id)
			{
				if ((operationPopupWindow == null || !operationPopupWindow.isShowing()) &&
					(deletePopupWindow == null || !deletePopupWindow.isShowing()))
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
						if (report.getStatus() == Report.STATUS_SUBMITTED && !report.isCC())
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
			}
		});
		reportListView.setOnItemLongClickListener(new OnItemLongClickListener()
		{
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
			{
				reportIndex = position - 1;
				if (ReimApplication.getReportTabIndex() == 0)
				{
					showOperationWindow();
				}
				else
				{
					showDeleteWindow();
				}
				return false;
			}
		});
	}
	
	private void initFilterView()
	{
		noResultLayout = (RelativeLayout) view.findViewById(R.id.noResultLayout);
		
		View filterView = View.inflate(getActivity(), R.layout.window_report_filter, null);		
		filterPopupWindow = ViewUtils.constructTopPopupWindow(getActivity(), filterView);
	}
	
	private void initOperationView()
	{
		View operationView = View.inflate(getActivity(), R.layout.window_report_operation, null);
		
		Button deleteButton = (Button) operationView.findViewById(R.id.deleteButton);
		deleteButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				operationPopupWindow.dismiss();

		    	final Report report = showMineList.get(reportIndex);
		    	if (report.isEditable())
				{
					Builder builder = new Builder(getActivity());
					builder.setTitle(R.string.warning);
					builder.setMessage(R.string.prompt_delete_report);
					builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener()
														{
															public void onClick(DialogInterface dialog, int which)
															{
																if (report.getServerID() == -1)
																{
																	deleteLocalReport(report.getLocalID());
																}
																else if (!PhoneUtils.isNetworkConnected())
																{
																	ViewUtils.showToast(getActivity(), R.string.error_delete_network_unavailable);
																}
																else
																{
																	sendDeleteReportRequest(report);																		
																}
															}
														});
					builder.setNegativeButton(R.string.cancel, null);
					builder.create().show();
				}
				else
				{
					ViewUtils.showToast(getActivity(), R.string.error_delete_report_submitted);
				}
			}
		});
		deleteButton = ViewUtils.resizeWindowButton(deleteButton);
		
		Button exportButton = (Button) operationView.findViewById(R.id.exportButton);
		exportButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				operationPopupWindow.dismiss();
				
		    	final Report report = showMineList.get(reportIndex);
				if (!PhoneUtils.isNetworkConnected())
				{
					ViewUtils.showToast(getActivity(), R.string.error_export_network_unavailable);
				}
				else if (report.getStatus() != Report.STATUS_FINISHED && report.getStatus() != Report.STATUS_APPROVED)
				{
					ViewUtils.showToast(getActivity(), R.string.error_export_not_finished);					
				}
				else
				{
					showExportDialog(report.getServerID());
				}
			}
		});
		exportButton = ViewUtils.resizeWindowButton(exportButton);
		
		Button cancelButton = (Button) operationView.findViewById(R.id.cancelButton);
		cancelButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				operationPopupWindow.dismiss();
			}
		});
		cancelButton = ViewUtils.resizeWindowButton(cancelButton);
		
		operationPopupWindow = ViewUtils.constructBottomPopupWindow(getActivity(), operationView);  
	}
	
	private void initDeleteView()
	{
		View deleteView = View.inflate(getActivity(), R.layout.window_delete, null);
		
		Button deleteButton = (Button) deleteView.findViewById(R.id.deleteButton);
		deleteButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				deletePopupWindow.dismiss();

		    	final Report report = showOthersList.get(reportIndex);
		    	if (report.getStatus() == Report.STATUS_REJECTED)
				{
//					Builder builder = new Builder(getActivity());
//					builder.setTitle(R.string.warning);
//					builder.setMessage(R.string.prompt_delete_report);
//					builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener()
//														{
//															public void onClick(DialogInterface dialog, int which)
//															{
//																if (!PhoneUtils.isNetworkConnected())
//																{
//																	ViewUtils.showToast(getActivity(), R.string.error_delete_network_unavailable);
//																}
//																else
//																{
//																	sendDeleteReportRequest(report);																		
//																}
//															}
//														});
//					builder.setNegativeButton(R.string.cancel, null);
//					builder.create().show();

					if (!PhoneUtils.isNetworkConnected())
					{
						ViewUtils.showToast(getActivity(), R.string.error_delete_network_unavailable);
					}
					else
					{
						sendDeleteReportRequest(report);																		
					}
				}
				else
				{
					ViewUtils.showToast(getActivity(), R.string.error_delete_report_submitted);
				}
			}
		});
		deleteButton = ViewUtils.resizeWindowButton(deleteButton);
		
		Button cancelButton = (Button) deleteView.findViewById(R.id.cancelButton);
		cancelButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				deletePopupWindow.dismiss();
			}
		});
		cancelButton = ViewUtils.resizeWindowButton(cancelButton);
		
		deletePopupWindow = ViewUtils.constructBottomPopupWindow(getActivity(), deleteView);  
	}
	
	private void setListView(int index)
	{
		ReimApplication.setReportTabIndex(index);
		if (index == 0)
		{
			myTitleTextView.setTextColor(ViewUtils.getColor(R.color.major_light));
			othersTitleTextView.setTextColor(ViewUtils.getColor(R.color.hint_light));
		}
		else
		{
			if (PhoneUtils.isNetworkConnected())
			{
				sendSubordinatesReportsRequest();
			}
			myTitleTextView.setTextColor(ViewUtils.getColor(R.color.hint_light));
			othersTitleTextView.setTextColor(ViewUtils.getColor(R.color.major_light));
		}
		ReimProgressDialog.show();
		refreshReportListView();
		ReimProgressDialog.dismiss();
	}

	private void showBadge()
	{
		int count = ReimApplication.getReportBadgeCount();
		if (count > 0)
		{
			tipImageView.setVisibility(View.VISIBLE);
		}
		else
		{
			tipImageView.setVisibility(View.GONE);
		}
	}
	
	private List<Report> readMineReportList()
	{
		return dbManager.getUserReports(appPreference.getCurrentUserID());
	}
	
	private List<Report> readOthersReportList()
	{
		return dbManager.getOthersReports(appPreference.getCurrentUserID());
	}

	private List<Report> filterReportList(List<Report> reportList, int sortType, boolean sortReverse, List<Integer> filterStatusList)
	{
		List<Report> resultList = new ArrayList<Report>();
		for (Report report : reportList)
		{
			if (!filterStatusList.isEmpty() && filterStatusList.size() < 5)
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
	
	private void refreshReportListView()
	{
		if (ReimApplication.getReportTabIndex() == 0)
		{
			mineList.clear();
			mineList.addAll(readMineReportList());
			showMineList.clear();
			showMineList.addAll(filterReportList(mineList, mineSortType, mineSortReverse, mineFilterStatusList));
			mineAdapter.set(showMineList);
			reportListView.setAdapter(mineAdapter);

			if (!mineFilterStatusList.isEmpty() && showMineList.isEmpty())
			{
				noResultLayout.setVisibility(View.VISIBLE);
			}
			else
			{
				noResultLayout.setVisibility(View.GONE);
			}
		}
		else
		{
			othersList.clear();
			othersList.addAll(readOthersReportList());
			showOthersList.clear();
			showOthersList.addAll(filterReportList(othersList, othersSortType, othersSortReverse, othersFilterStatusList));
			othersAdapter.set(showOthersList);
			reportListView.setAdapter(othersAdapter);

			if (!othersFilterStatusList.isEmpty() && showOthersList.isEmpty())
			{
				noResultLayout.setVisibility(View.VISIBLE);
			}
			else
			{
				noResultLayout.setVisibility(View.GONE);
			}
		}
	}

    private void showFilterWindow()
    {
    	View filterView = filterPopupWindow.getContentView();

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
					mineCheck = tagAdapter.getCheckedTags();
				}
				else
				{
					othersSortReverse = othersSortType == othersTempSortType ? !othersSortReverse : false;
					othersSortType = othersTempSortType;
					othersFilterStatusList.clear();
					othersFilterStatusList.addAll(tagAdapter.getFilterStatusList());
					othersCheck = tagAdapter.getCheckedTags();
				}

				filterPopupWindow.dismiss();
				ReimProgressDialog.show();
				refreshReportListView();
				ReimProgressDialog.dismiss();
			}
		});

		ImageView cancelImageView = (ImageView)filterView.findViewById(R.id.cancelImageView);
		cancelImageView.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				filterPopupWindow.dismiss();
			}
		});
		
		if (ReimApplication.getReportTabIndex() == 0)
		{
			switch (mineSortType)
			{
				case SORT_NULL:
					sortRadioGroup.check(sortNullRadio.getId());
					break;
				case SORT_ITEMS_COUNT:
					sortRadioGroup.check(sortItemsCountRadio.getId());
					break;
				case SORT_AMOUNT:
					sortRadioGroup.check(sortAmountRadio.getId());
					break;
				case SORT_CREATE_DATE:
					sortRadioGroup.check(sortCreateDateRadio.getId());
					break;
				default:
					break;
			}
			
			tagAdapter.setCheck(mineCheck);
			tagAdapter.notifyDataSetChanged();
		}
		else
		{
			switch (othersSortType)
			{
				case SORT_NULL:
					sortRadioGroup.check(sortNullRadio.getId());
					break;
				case SORT_ITEMS_COUNT:
					sortRadioGroup.check(sortItemsCountRadio.getId());
					break;
				case SORT_AMOUNT:
					sortRadioGroup.check(sortAmountRadio.getId());
					break;
				case SORT_CREATE_DATE:
					sortRadioGroup.check(sortCreateDateRadio.getId());
					break;
				default:
					break;
			}
			
			tagAdapter.setCheck(othersCheck);
			tagAdapter.notifyDataSetChanged();			
		}
		
		filterPopupWindow.showAtLocation(getActivity().findViewById(R.id.containerLayout), Gravity.CENTER, 0, 0);
		filterPopupWindow.update();
    }
    
    private void showOperationWindow()
    {    	
		operationPopupWindow.showAtLocation(getActivity().findViewById(R.id.containerLayout), Gravity.BOTTOM, 0, 0);
		operationPopupWindow.update();
		
		ViewUtils.dimBackground(getActivity());
    }

    private void showDeleteWindow()
    {    	
		deletePopupWindow.showAtLocation(getActivity().findViewById(R.id.containerLayout), Gravity.BOTTOM, 0, 0);
		deletePopupWindow.update();
		
		ViewUtils.dimBackground(getActivity());
    }
    
	private void showExportDialog(final int reportID)
    {
		View view = View.inflate(getActivity(), R.layout.dialog_report_export, null);
		
		final EditText emailEditText = (EditText)view.findViewById(R.id.emailEditText);
		emailEditText.setOnFocusChangeListener(ViewUtils.onFocusChangeListener);
		
		User user = appPreference.getCurrentUser();
		if (!user.getEmail().equals(""))
		{
			emailEditText.setText(user.getEmail());
		}
		emailEditText.setOnFocusChangeListener(ViewUtils.onFocusChangeListener);
		emailEditText.requestFocus();
		
    	Builder builder = new Builder(getActivity());
    	builder.setTitle(R.string.export_report);
    	builder.setView(view);
    	builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener()
								{
									public void onClick(DialogInterface dialog, int which)
									{
										String email = emailEditText.getText().toString();
										if (email.equals(""))
										{
											ViewUtils.showToast(getActivity(), R.string.error_email_empty);
										}
										else if (!Utils.isEmail(email))
										{
											ViewUtils.showToast(getActivity(), R.string.error_email_wrong_format);
										}
										else
										{
											sendExportReportRequest(reportID, email);
										}
									}
								});
    	builder.setNegativeButton(R.string.cancel, null);
    	builder.create().show();
    }
	
	private void sendDeleteReportRequest(final Report report)
	{
		ReimProgressDialog.show();
		DeleteReportRequest request = new DeleteReportRequest(report.getServerID());
		request.sendRequest(new HttpConnectionCallback()
		{
			public void execute(Object httpResponse)
			{
				final DeleteReportResponse response = new DeleteReportResponse(httpResponse);
				if (response.getStatus())
				{
					getActivity().runOnUiThread(new Runnable()
					{
						public void run()
						{
							int reportID = ReimApplication.getReportTabIndex() == 0 ? report.getLocalID() : report.getServerID();
							deleteLocalReport(reportID);
						}
					});
				}
				else
				{
					getActivity().runOnUiThread(new Runnable()
					{
						public void run()
						{
							ReimProgressDialog.dismiss();
				            ViewUtils.showToast(getActivity(), R.string.prompt_delete_failed, response.getErrorMessage());
						}
					});		
				}
			}
		});
	}

	private void sendExportReportRequest(int reportID, String email)
	{
		ReimProgressDialog.show();
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
							ReimProgressDialog.dismiss();
							ViewUtils.showToast(getActivity(), R.string.succeed_in_exporting);
						}
					});
				}
				else
				{
					getActivity().runOnUiThread(new Runnable()
					{
						public void run()
						{
							ReimProgressDialog.dismiss();
							ViewUtils.showToast(getActivity(), R.string.failed_to_export);
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
					
					List<Report> localReportList = dbManager.getOthersReports(managerID);
					for (Report localReport : localReportList)
					{
						boolean reportExists = false;
						for (Report report : response.getReportList())
						{
							if (localReport.getServerID() == report.getServerID())
							{
								reportExists = true;
								break;
							}
						}
						if (!reportExists)
						{
							dbManager.deleteOthersReport(localReport.getServerID(), managerID);
						}
					}
					
					for (Report report : response.getReportList())
					{
						Report localReport = dbManager.getOthersReport(report.getServerID());
						if (localReport == null)
						{
							dbManager.insertOthersReport(report);
						}
						else if (report.getServerUpdatedDate() > localReport.getLocalUpdatedDate())
						{
							dbManager.updateOthersReport(report);
						}
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
							ViewUtils.showToast(getActivity(), R.string.failed_to_get_data, response.getErrorMessage());
						}
					});					
				}
			}
		});
	}
	
	private void deleteLocalReport(int reportID)
	{
		if (ReimApplication.getReportTabIndex() == 0)
		{
			if (dbManager.deleteReport(reportID))
			{
				refreshReportListView();
				ReimProgressDialog.dismiss();
	            ViewUtils.showToast(getActivity(), R.string.prompt_delete_succeed);														
			}
			else
			{
				ReimProgressDialog.dismiss();
	            ViewUtils.showToast(getActivity(), R.string.prompt_delete_failed);
			}			
		}
		else
		{
			if (dbManager.deleteOthersReport(reportID, appPreference.getCurrentUserID()))
			{
				refreshReportListView();
				ReimProgressDialog.dismiss();
	            ViewUtils.showToast(getActivity(), R.string.prompt_delete_succeed);														
			}
			else
			{
				ReimProgressDialog.dismiss();
	            ViewUtils.showToast(getActivity(), R.string.prompt_delete_failed);
			}			
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
//						String prompt = SyncUtils.isSyncOnGoing ? "正在同步中" : "未打开同步开关或未打开Wifi，无法刷新";
						int prompt = SyncUtils.isSyncOnGoing ? R.string.prompt_sync_ongoing : R.string.error_refresh_network_unavailable;
						ViewUtils.showToast(getActivity(), prompt);
					}
				});
			}			
		}	
		else
		{
			if (PhoneUtils.isNetworkConnected())
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
						ViewUtils.showToast(getActivity(), R.string.error_refresh_network_unavailable);
					}
				});
			}			
		}		
	}
		
	public void onClick(View v)
	{
		if (v.equals(myTitleTextView))
		{
			setListView(0);
		}
		else
		{
			ReimApplication.setReportBadgeCount(0);
			showBadge();
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