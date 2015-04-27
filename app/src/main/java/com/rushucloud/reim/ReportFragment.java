package com.rushucloud.reim;

import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.rushucloud.reim.report.ApproveReportActivity;
import com.rushucloud.reim.report.EditReportActivity;
import com.rushucloud.reim.report.ExportActivity;
import com.rushucloud.reim.report.ShowReportActivity;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import classes.base.Report;
import classes.base.User;
import classes.adapter.OthersReportListViewAdapter;
import classes.adapter.ReportListViewAdapter;
import classes.adapter.ReportTagGridViewAdapter;
import classes.utils.AppPreference;
import classes.utils.DBManager;
import classes.utils.PhoneUtils;
import classes.utils.ReimApplication;
import classes.utils.Utils;
import classes.utils.ViewUtils;
import classes.widget.ReimProgressDialog;
import classes.widget.XListView;
import classes.widget.XListView.IXListViewListener;
import netUtils.HttpConnectionCallback;
import netUtils.SyncDataCallback;
import netUtils.SyncUtils;
import netUtils.request.EventsRequest;
import netUtils.request.report.DeleteReportRequest;
import netUtils.request.report.SubordinatesReportRequest;
import netUtils.response.EventsResponse;
import netUtils.response.report.DeleteReportResponse;
import netUtils.response.report.SubordinatesReportResponse;

public class ReportFragment extends Fragment
{
    private static final int GET_DATA_INTERVAL = 600;

	private static final int SORT_UPDATE_DATE = 0;
    private static final int SORT_CREATE_DATE = 1;
	private static final int SORT_AMOUNT = 2;

	private boolean hasInit = false;
	
	private View view;
    private ImageView filterImageView;
	private PopupWindow filterPopupWindow;
    private RadioButton sortUpdateDateRadio;
    private RadioButton sortCreateDateRadio;
    private RadioButton sortAmountRadio;
    private ImageView sortUpdateImageView;
    private ImageView sortCreateImageView;
    private ImageView sortAmountImageView;
    private ReportTagGridViewAdapter tagAdapter;
    private RotateAnimation rotateAnimation;
    private RotateAnimation rotateReverseAnimation;
	private RelativeLayout noResultLayout;
	private TextView myTitleTextView;
    private TextView myShortTextView;
    private TextView myMediumTextView;
    private TextView myLongTextView;
    private TextView othersTitleTextView;
    private TextView othersShortTextView;
    private TextView othersMediumTextView;
    private TextView othersLongTextView;
	private XListView reportListView;
	private ReportListViewAdapter mineAdapter;
	private OthersReportListViewAdapter othersAdapter;
	private PopupWindow operationPopupWindow;
	private PopupWindow deletePopupWindow;

	private AppPreference appPreference;
	private DBManager dbManager;
	
	private List<Report> mineList = new ArrayList<>();
	private List<Report> showMineList = new ArrayList<>();
	private int mineSortType = SORT_UPDATE_DATE;
	private int mineTempSortType = SORT_UPDATE_DATE;
    private boolean mineSortReverse = false;
    private boolean mineTempSortReverse = false;
	private boolean[] mineCheck;
	private List<Integer> mineFilterStatusList = new ArrayList<>();
	
	private List<Report> othersList = new ArrayList<>();
	private List<Report> showOthersList = new ArrayList<>();
	private int othersSortType = SORT_UPDATE_DATE;
	private int othersTempSortType = SORT_UPDATE_DATE;
    private boolean othersSortReverse = false;
    private boolean othersTempSortReverse = false;
	private boolean[] othersCheck;
	private List<Integer> othersFilterStatusList = new ArrayList<>();
	
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
		myTitleTextView.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                setListView(0);
            }
        });

        myShortTextView = (TextView) view.findViewById(R.id.myShortTextView);
        myMediumTextView = (TextView) view.findViewById(R.id.myMediumTextView);
        myLongTextView = (TextView) view.findViewById(R.id.myLongTextView);

		othersTitleTextView = (TextView)getActivity().findViewById(R.id.othersTitleTextView);
        othersTitleTextView.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                setListView(1);
            }
        });

        othersShortTextView = (TextView) view.findViewById(R.id.othersShortTextView);
        othersMediumTextView = (TextView) view.findViewById(R.id.othersMediumTextView);
        othersLongTextView = (TextView) view.findViewById(R.id.othersLongTextView);
		
		filterImageView = (ImageView) view.findViewById(R.id.filterImageView);
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
				MobclickAgent.onEvent(getActivity(), "UMENG_REPORT_NEW");

				User currentUser = appPreference.getCurrentUser();
				Report report = new Report();
				report.setSender(currentUser);
				report.setManagerList(currentUser.buildBaseManagerList());
				
				Bundle bundle = new Bundle();
				bundle.putSerializable("report", report);				
				Intent intent = new Intent(getActivity(), EditReportActivity.class);
				intent.putExtras(bundle);
                ViewUtils.goForward(getActivity(), intent);
			}
		});
	}
	
	private void initListView()
	{
		mineAdapter = new ReportListViewAdapter(getActivity(), showMineList);	
		othersAdapter = new OthersReportListViewAdapter(getActivity(), showOthersList);
		
		reportListView = (XListView)getActivity().findViewById(R.id.reportListView);
		reportListView.setAdapter(mineAdapter);
		reportListView.setXListViewListener(new IXListViewListener()
        {
            public void onRefresh()
            {
                if (PhoneUtils.isNetworkConnected())
                {
                    sendGetEventsRequest();
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

            public void onLoadMore()
            {

            }
        });
		reportListView.setPullRefreshEnable(true);
		reportListView.setPullLoadEnable(false);
		reportListView.setOnItemClickListener(new OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id)
			{
				if ((operationPopupWindow == null || !operationPopupWindow.isShowing()) &&
					(deletePopupWindow == null || !deletePopupWindow.isShowing()) && position > 0)
				{		
					if (ReimApplication.getReportTabIndex() == ReimApplication.TAB_REPORT_MINE)
					{
						Report report = showMineList.get(position - 1);

                        List<Integer> mineUnreadList = ReimApplication.getMineUnreadList();
                        if (mineUnreadList.contains(report.getServerID()))
                        {
                            mineUnreadList.remove(Integer.valueOf(report.getServerID()));
                            ReimApplication.setMineUnreadList(mineUnreadList);
                        }

						Bundle bundle = new Bundle();
						bundle.putSerializable("report", report);
						Intent intent = new Intent();
						if (report.isEditable())
						{
							intent.setClass(getActivity(), EditReportActivity.class);
						}
						else
						{
							bundle.putBoolean("myReport", true);
							intent.setClass(getActivity(), ShowReportActivity.class);						
						}
						intent.putExtras(bundle);
                        ViewUtils.goForward(getActivity(), intent);
					}
					else
					{
						Report report = showOthersList.get(position - 1);
                        if (report.getSectionName().isEmpty())
                        {
                            List<Integer> othersUnreadList = ReimApplication.getOthersUnreadList();
                            if (othersUnreadList.contains(report.getServerID()))
                            {
                                othersUnreadList.remove(Integer.valueOf(report.getServerID()));
                                ReimApplication.setOthersUnreadList(othersUnreadList);
                            }

                            Bundle bundle = new Bundle();
                            bundle.putSerializable("report", report);
                            Intent intent = new Intent();
                            if (report.canBeApprovedByMe())
                            {
                                intent.setClass(getActivity(), ApproveReportActivity.class);
                            }
                            else
                            {
                                bundle.putBoolean("myReport", false);
                                intent.setClass(getActivity(), ShowReportActivity.class);
                            }
                            intent.putExtras(bundle);
                            ViewUtils.goForward(getActivity(), intent);
                        }
					}
				}
			}
		});
		reportListView.setOnItemLongClickListener(new OnItemLongClickListener()
		{
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
			{
				reportIndex = position - 1;
				if (ReimApplication.getReportTabIndex() == ReimApplication.TAB_REPORT_MINE)
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

        rotateAnimation = new RotateAnimation(0, 180, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotateAnimation.setDuration(200);
        rotateAnimation.setFillAfter(true);

        rotateReverseAnimation = new RotateAnimation(180, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotateReverseAnimation.setDuration(200);
        rotateReverseAnimation.setFillAfter(true);
		
		View filterView = View.inflate(getActivity(), R.layout.window_report_filter, null);

        sortUpdateDateRadio = (RadioButton) filterView.findViewById(R.id.sortUpdateDateRadio);
        sortUpdateDateRadio.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                selectSortUpdateDateRadio();
                if (ReimApplication.getReportTabIndex() == ReimApplication.TAB_REPORT_MINE)
                {
                    MobclickAgent.onEvent(getActivity(), "UMENG_SHEET_MY_MODIFY_DATE");
                    if (mineTempSortType != SORT_UPDATE_DATE)
                    {
                        mineTempSortReverse = false;
                        mineTempSortType = SORT_UPDATE_DATE;
                    }
                    else
                    {
                        reverseSortUpdateImageView();
                    }
                }
                else
                {
                    MobclickAgent.onEvent(getActivity(), "UMENG_SHEET_OTHERS_MODIFY_DATE");
                    if (othersTempSortType != SORT_UPDATE_DATE)
                    {
                        othersTempSortReverse = false;
                        othersTempSortType = SORT_UPDATE_DATE;
                    }
                    else
                    {
                        reverseSortUpdateImageView();
                    }
                }
            }
        });
        sortCreateDateRadio = (RadioButton) filterView.findViewById(R.id.sortCreateDateRadio);
        sortCreateDateRadio.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                selectSortCreateDateRadio();
                if (ReimApplication.getReportTabIndex() == ReimApplication.TAB_REPORT_MINE)
                {
                    MobclickAgent.onEvent(getActivity(), "UMENG_SHEET_MY_CREATE_DATE");
                    if (mineTempSortType != SORT_CREATE_DATE)
                    {
                        mineTempSortReverse = false;
                        mineTempSortType = SORT_CREATE_DATE;
                    }
                    else
                    {
                        reverseSortCreateImageView();
                    }
                }
                else
                {
                    MobclickAgent.onEvent(getActivity(), "UMENG_SHEET_OTHERS_CREATE_DATE");
                    if (othersTempSortType != SORT_CREATE_DATE)
                    {
                        othersTempSortReverse = false;
                        othersTempSortType = SORT_CREATE_DATE;
                    }
                    else
                    {
                        reverseSortCreateImageView();
                    }
                }
            }
        });
        sortAmountRadio = (RadioButton) filterView.findViewById(R.id.sortAmountRadio);
        sortAmountRadio.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                selectSortAmountRadio();
                if (ReimApplication.getReportTabIndex() == ReimApplication.TAB_REPORT_MINE)
                {
                    MobclickAgent.onEvent(getActivity(), "UMENG_SHEET_MY_AMOUNT");
                    if (mineTempSortType != SORT_AMOUNT)
                    {
                        mineTempSortReverse = false;
                        mineTempSortType = SORT_AMOUNT;
                    }
                    else
                    {
                        reverseSortAmountImageView();
                    }
                }
                else
                {
                    MobclickAgent.onEvent(getActivity(), "UMENG_SHEET_OTHERS_AMOUNT");
                    if (othersTempSortType != SORT_AMOUNT)
                    {
                        othersTempSortReverse = false;
                        othersTempSortType = SORT_AMOUNT;
                    }
                    else
                    {
                        reverseSortAmountImageView();
                    }
                }
            }
        });

        sortUpdateImageView = (ImageView) filterView.findViewById(R.id.sortUpdateImageView);
        sortUpdateImageView.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                reverseSortUpdateImageView();
            }
        });
        sortCreateImageView = (ImageView) filterView.findViewById(R.id.sortCreateImageView);
        sortCreateImageView.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                reverseSortCreateImageView();
            }
        });
        sortAmountImageView = (ImageView) filterView.findViewById(R.id.sortAmountImageView);
        sortAmountImageView.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                reverseSortAmountImageView();
            }
        });

        tagAdapter = new ReportTagGridViewAdapter(getActivity());

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
                if (ReimApplication.getReportTabIndex() == ReimApplication.TAB_REPORT_MINE)
                {
                    mineSortReverse = mineTempSortReverse;
                    mineSortType = mineTempSortType;
                    mineFilterStatusList.clear();
                    mineFilterStatusList.addAll(tagAdapter.getFilterStatusList());
                    mineCheck = tagAdapter.getCheckedTags();
                }
                else
                {
                    othersSortReverse = othersTempSortReverse;
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

		filterPopupWindow = ViewUtils.buildTopPopupWindow(getActivity(), filterView);
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
		
		Button exportButton = (Button) operationView.findViewById(R.id.exportButton);
		exportButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				operationPopupWindow.dismiss();
				
		    	Report report = showMineList.get(reportIndex);
                if (report.getServerID() == -1 || report.getServerID() == 0)
                {
                    ViewUtils.showToast(getActivity(), R.string.error_export_report_not_uploaded);
                }
                else if (report.getStatus() != Report.STATUS_FINISHED && report.getStatus() != Report.STATUS_APPROVED)
                {
                    ViewUtils.showToast(getActivity(), R.string.error_export_not_finished);
                }
                else
                {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("report", report);
                    Intent intent = new Intent(getActivity(), ExportActivity.class);
                    intent.putExtras(bundle);
                    ViewUtils.goForward(getActivity(), intent);
                }
			}
		});
		
		Button cancelButton = (Button) operationView.findViewById(R.id.cancelButton);
		cancelButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				operationPopupWindow.dismiss();
			}
		});
		
		operationPopupWindow = ViewUtils.buildBottomPopupWindow(getActivity(), operationView);
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
		    	if (!report.isPending())
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
		
		Button cancelButton = (Button) deleteView.findViewById(R.id.cancelButton);
		cancelButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				deletePopupWindow.dismiss();
			}
		});
		
		deletePopupWindow = ViewUtils.buildBottomPopupWindow(getActivity(), deleteView);
	}
	
	private void setListView(int index)
	{
		ReimApplication.setReportTabIndex(index);
		if (index == ReimApplication.TAB_REPORT_MINE)
		{
			myTitleTextView.setTextColor(ViewUtils.getColor(R.color.major_light));
			othersTitleTextView.setTextColor(ViewUtils.getColor(R.color.hint_light));
		}
		else
		{
			if (PhoneUtils.isNetworkConnected() && Utils.getCurrentTime() - appPreference.getLastGetOthersReportTime() > GET_DATA_INTERVAL)
			{
				sendSubordinatesReportsRequest();
			}
			myTitleTextView.setTextColor(ViewUtils.getColor(R.color.hint_light));
			othersTitleTextView.setTextColor(ViewUtils.getColor(R.color.major_light));
		}
		refreshReportListView();
	}

	private void showBadge()
	{
		int count = ReimApplication.getMineUnreadList().size();
		if (count == 0)
		{
			myShortTextView.setVisibility(View.GONE);
            myMediumTextView.setVisibility(View.GONE);
            myLongTextView.setVisibility(View.GONE);
		}
		else if (count < 10)
		{
            myShortTextView.setVisibility(View.VISIBLE);
            myShortTextView.setText(Integer.toString(count));
            myMediumTextView.setVisibility(View.GONE);
            myLongTextView.setVisibility(View.GONE);
		}
        else if (count < 100)
        {
            myShortTextView.setVisibility(View.GONE);
            myMediumTextView.setVisibility(View.VISIBLE);
            myMediumTextView.setText(Integer.toString(count));
            myLongTextView.setVisibility(View.GONE);
        }
        else
        {
            myShortTextView.setVisibility(View.GONE);
            myMediumTextView.setVisibility(View.GONE);
            myLongTextView.setVisibility(View.VISIBLE);
        }

        count = ReimApplication.getOthersUnreadList().size();
        if (count == 0)
        {
            othersShortTextView.setVisibility(View.GONE);
            othersMediumTextView.setVisibility(View.GONE);
            othersLongTextView.setVisibility(View.GONE);
        }
        else if (count < 10)
        {
            othersShortTextView.setVisibility(View.VISIBLE);
            othersShortTextView.setText(Integer.toString(count));
            othersMediumTextView.setVisibility(View.GONE);
            othersLongTextView.setVisibility(View.GONE);
        }
        else if (count < 100)
        {
            othersShortTextView.setVisibility(View.GONE);
            othersMediumTextView.setVisibility(View.VISIBLE);
            othersMediumTextView.setText(Integer.toString(count));
            othersLongTextView.setVisibility(View.GONE);
        }
        else
        {
            othersShortTextView.setVisibility(View.GONE);
            othersMediumTextView.setVisibility(View.GONE);
            othersLongTextView.setVisibility(View.VISIBLE);
        }
	}

    private void selectSortUpdateDateRadio()
    {
        sortUpdateDateRadio.setChecked(true);
        sortCreateDateRadio.setChecked(false);
        sortAmountRadio.setChecked(false);

        sortUpdateImageView.setVisibility(View.VISIBLE);
        sortCreateImageView.clearAnimation();
        sortCreateImageView.setVisibility(View.GONE);
        sortAmountImageView.clearAnimation();
        sortAmountImageView.setVisibility(View.GONE);
    }

    private void selectSortCreateDateRadio()
    {
        sortUpdateDateRadio.setChecked(false);
        sortCreateDateRadio.setChecked(true);
        sortAmountRadio.setChecked(false);

        sortUpdateImageView.clearAnimation();
        sortUpdateImageView.setVisibility(View.GONE);
        sortCreateImageView.setVisibility(View.VISIBLE);
        sortAmountImageView.clearAnimation();
        sortAmountImageView.setVisibility(View.GONE);
    }

    private void selectSortAmountRadio()
    {
        sortUpdateDateRadio.setChecked(false);
        sortCreateDateRadio.setChecked(false);
        sortAmountRadio.setChecked(true);

        sortUpdateImageView.clearAnimation();
        sortUpdateImageView.setVisibility(View.GONE);
        sortCreateImageView.clearAnimation();
        sortCreateImageView.setVisibility(View.GONE);
        sortAmountImageView.setVisibility(View.VISIBLE);
    }

    private void reverseSortUpdateImageView()
    {
        if (ReimApplication.getReportTabIndex() == ReimApplication.TAB_REPORT_MINE)
        {
            mineTempSortReverse = !mineTempSortReverse;
            if (!mineTempSortReverse) // status before change
            {
                sortUpdateImageView.startAnimation(rotateReverseAnimation);
            }
            else
            {
                sortUpdateImageView.startAnimation(rotateAnimation);
            }
        }
        else
        {
            othersTempSortReverse = !othersTempSortReverse;
            if (!othersTempSortReverse) // status before change
            {
                sortUpdateImageView.startAnimation(rotateReverseAnimation);
            }
            else
            {
                sortUpdateImageView.startAnimation(rotateAnimation);
            }
        }
    }

    private void reverseSortCreateImageView()
    {
        if (ReimApplication.getReportTabIndex() == ReimApplication.TAB_REPORT_MINE)
        {
            mineTempSortReverse = !mineTempSortReverse;
            if (!mineTempSortReverse) // status before change
            {
                sortCreateImageView.startAnimation(rotateReverseAnimation);
            }
            else
            {
                sortCreateImageView.startAnimation(rotateAnimation);
            }
        }
        else
        {
            othersTempSortReverse = !othersTempSortReverse;
            if (!othersTempSortReverse) // status before change
            {
                sortCreateImageView.startAnimation(rotateReverseAnimation);
            }
            else
            {
                sortCreateImageView.startAnimation(rotateAnimation);
            }
        }
    }

    private void reverseSortAmountImageView()
    {
        if (ReimApplication.getReportTabIndex() == ReimApplication.TAB_REPORT_MINE)
        {
            mineTempSortReverse = !mineTempSortReverse;
            if (!mineTempSortReverse) // status before change
            {
                sortAmountImageView.startAnimation(rotateReverseAnimation);
            }
            else
            {
                sortAmountImageView.startAnimation(rotateAnimation);
            }
        }
        else
        {
            othersTempSortReverse = !othersTempSortReverse;
            if (!othersTempSortReverse) // status before change
            {
                sortAmountImageView.startAnimation(rotateReverseAnimation);
            }
            else
            {
                sortAmountImageView.startAnimation(rotateAnimation);
            }
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
		List<Report> resultList = new ArrayList<>();
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

        switch (sortType)
        {
            case SORT_UPDATE_DATE:
                Report.sortByUpdateDate(resultList);
                break;
            case SORT_CREATE_DATE:
                Report.sortByCreateDate(resultList);
                break;
            case SORT_AMOUNT:
                Report.sortByAmount(resultList);
                break;
            default:
                break;
        }

		if (sortReverse)
		{
			Collections.reverse(resultList);
		}

		return resultList;
	}

    private void buildReportListByStatus()
    {
        Report.sortByUpdateDate(othersList);

        List<Report> pendingList = new ArrayList<>();
        List<Report> processedList = new ArrayList<>();
        for (Report report : othersList)
        {
            if (report.isPending())
            {
                pendingList.add(report);
            }
            else
            {
                processedList.add(report);
            }
        }

        showOthersList.clear();

        Report report = new Report();
        report.setSectionName(getString(R.string.pending));
        showOthersList.add(report);

        if (pendingList.isEmpty())
        {
            Report noPendingReport = new Report();
            noPendingReport.setSectionName(getString(R.string.no_pending_reports));
            showOthersList.add(noPendingReport);
        }
        else
        {
            showOthersList.addAll(pendingList);
        }

        if (!processedList.isEmpty())
        {
            Report processedReport = new Report();
            processedReport.setSectionName(getString(R.string.processed));
            showOthersList.add(processedReport);
            showOthersList.addAll(processedList);
        }
    }

	private void refreshReportListView()
	{
		if (ReimApplication.getReportTabIndex() == ReimApplication.TAB_REPORT_MINE)
		{
			mineList.clear();
			mineList.addAll(readMineReportList());
			showMineList.clear();
			showMineList.addAll(filterReportList(mineList, mineSortType, mineSortReverse, mineFilterStatusList));
			mineAdapter.setReportList(showMineList);
            mineAdapter.setUnreadList(ReimApplication.getMineUnreadList());
			reportListView.setAdapter(mineAdapter);

            int visibility = !mineFilterStatusList.isEmpty() && showMineList.isEmpty()? View.VISIBLE : View.GONE;
            noResultLayout.setVisibility(visibility);

            int filterImage = mineFilterStatusList.isEmpty()? R.drawable.filter_empty : R.drawable.filter_full;
            filterImageView.setImageResource(filterImage);
		}
		else
		{
			othersList.clear();
			othersList.addAll(readOthersReportList());
            if (!othersList.isEmpty() && othersFilterStatusList.isEmpty() && othersSortType == SORT_UPDATE_DATE && !othersSortReverse)
            {
                buildReportListByStatus();
            }
            else
            {
                showOthersList.clear();
                showOthersList.addAll(filterReportList(othersList, othersSortType, othersSortReverse, othersFilterStatusList));
            }

            othersAdapter.set(showOthersList);
            othersAdapter.setUnreadList(ReimApplication.getOthersUnreadList());
			reportListView.setAdapter(othersAdapter);

            int visibility = !othersFilterStatusList.isEmpty() && showOthersList.isEmpty()? View.VISIBLE : View.GONE;
            noResultLayout.setVisibility(visibility);

            int filterImage = othersFilterStatusList.isEmpty()? R.drawable.filter_empty : R.drawable.filter_full;
            filterImageView.setImageResource(filterImage);
		}
        showBadge();
	}

    private void showFilterWindow()
    {
		if (ReimApplication.getReportTabIndex() == ReimApplication.TAB_REPORT_MINE)
		{
            mineTempSortReverse = false;
            mineTempSortType = mineSortType;
            switch (mineSortType)
            {
                case SORT_UPDATE_DATE:
                {
                    selectSortUpdateDateRadio();
                    if (mineSortReverse)
                    {
                        reverseSortUpdateImageView();
                    }
                    break;
                }
                case SORT_CREATE_DATE:
                {
                    selectSortCreateDateRadio();
                    if (mineSortReverse)
                    {
                        reverseSortCreateImageView();
                    }
                    break;
                }
                case SORT_AMOUNT:
                {
                    selectSortAmountRadio();
                    if (mineSortReverse)
                    {
                        reverseSortAmountImageView();
                    }
                    break;
                }
                default:
                    break;
            }

			tagAdapter.setCheck(mineCheck);
			tagAdapter.notifyDataSetChanged();
		}
		else
		{
            othersTempSortReverse = false;
            othersTempSortType = othersSortType;
			switch (othersSortType)
			{
                case SORT_UPDATE_DATE:
                {
                    selectSortUpdateDateRadio();
                    if (othersSortReverse)
                    {
                        reverseSortUpdateImageView();
                    }
                    break;
                }
                case SORT_CREATE_DATE:
                {
                    selectSortCreateDateRadio();
                    if (othersSortReverse)
                    {
                        reverseSortCreateImageView();
                    }
                    break;
                }
                case SORT_AMOUNT:
                {
                    selectSortAmountRadio();
                    if (othersSortReverse)
                    {
                        reverseSortAmountImageView();
                    }
                    break;
                }
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
							int reportID = ReimApplication.getReportTabIndex() == ReimApplication.TAB_REPORT_MINE?
                                    report.getLocalID() : report.getServerID();
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
				            ViewUtils.showToast(getActivity(), R.string.failed_to_delete, response.getErrorMessage());
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
						else
						{
                            report.setType(localReport.getType());
                            report.setManagerList(localReport.getManagerList());
                            report.setCCList(localReport.getCCList());
							dbManager.updateOthersReport(report);
						}
					}
					
					getActivity().runOnUiThread(new Runnable()
					{
						public void run()
						{
                            appPreference.setLastGetOthersReportTime(Utils.getCurrentTime());
                            appPreference.saveAppPreference();
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

    private void sendGetEventsRequest()
    {
        EventsRequest request = new EventsRequest();
        request.sendRequest(new HttpConnectionCallback()
        {
            public void execute(Object httpResponse)
            {
                EventsResponse response = new EventsResponse(httpResponse);
                if (response.getStatus())
                {
                    ReimApplication.setMineUnreadList(response.getMineUnreadList());
                    ReimApplication.setOthersUnreadList(response.getOthersUnreadList());
                    ReimApplication.setUnreadMessagesCount(response.getUnreadMessagesCount());
                    ReimApplication.setHasUnreadMessages(response.hasUnreadMessages());
                    refreshReports();
                }
                else
                {
                    getActivity().runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            reportListView.stopRefresh();
                            ViewUtils.showToast(getActivity(), R.string.failed_to_get_data);
                        }
                    });
                }
            }
        });
    }

    private void deleteLocalReport(int reportID)
	{
		if (ReimApplication.getReportTabIndex() == ReimApplication.TAB_REPORT_MINE)
		{
			if (dbManager.deleteReport(reportID))
			{
				refreshReportListView();
				ReimProgressDialog.dismiss();
	            ViewUtils.showToast(getActivity(), R.string.succeed_in_deleting);
			}
			else
			{
				ReimProgressDialog.dismiss();
	            ViewUtils.showToast(getActivity(), R.string.failed_to_delete);
			}			
		}
		else
		{
			if (dbManager.deleteOthersReport(reportID, appPreference.getCurrentUserID()))
			{
				refreshReportListView();
				ReimProgressDialog.dismiss();
	            ViewUtils.showToast(getActivity(), R.string.succeed_in_deleting);
			}
			else
			{
				ReimProgressDialog.dismiss();
	            ViewUtils.showToast(getActivity(), R.string.failed_to_delete);
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
		if (ReimApplication.getReportTabIndex() == ReimApplication.TAB_REPORT_MINE)
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
//						String prompt = SyncUtils.isSyncOnGoing? "正在同步中" : "未打开同步开关或未打开Wifi，无法刷新";
						int prompt = SyncUtils.isSyncOnGoing? R.string.prompt_sync_ongoing : R.string.error_refresh_network_unavailable;
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
}