package com.rushucloud.reim.main;

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

import com.rushucloud.reim.R;
import com.rushucloud.reim.report.ApproveReportActivity;
import com.rushucloud.reim.report.EditReportActivity;
import com.rushucloud.reim.report.ExportActivity;
import com.rushucloud.reim.report.ShowReportActivity;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import classes.adapter.ReportListViewAdapter;
import classes.adapter.ReportTagGridViewAdapter;
import classes.model.Report;
import classes.model.User;
import classes.utils.AppPreference;
import classes.utils.Constant;
import classes.utils.DBManager;
import classes.utils.PhoneUtils;
import classes.utils.ReimApplication;
import classes.utils.Utils;
import classes.utils.ViewUtils;
import classes.widget.ReimProgressDialog;
import classes.widget.XListView;
import classes.widget.XListView.IXListViewListener;
import netUtils.common.HttpConnectionCallback;
import netUtils.common.SyncDataCallback;
import netUtils.common.SyncUtils;
import netUtils.request.common.EventsRequest;
import netUtils.request.report.DeleteReportRequest;
import netUtils.request.report.SubordinatesReportRequest;
import netUtils.response.common.EventsResponse;
import netUtils.response.report.DeleteReportResponse;
import netUtils.response.report.SubordinatesReportResponse;

public class ReportFragment extends Fragment implements OnClickListener
{
    // Widgets
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
    private TextView myBadgeTextView;
    private TextView othersTitleTextView;
    private TextView othersBadgeTextView;

    private XListView reportListView;
    private ReportListViewAdapter adapter;
    private PopupWindow operationPopupWindow;
    private Button exportButton;
    private Button deleteButton;

    // Local Data
    private AppPreference appPreference;
    private DBManager dbManager;
    private boolean hasInit = false;
    private int reportIndex;

    private List<Report> mineList = new ArrayList<>();
    private List<Report> showMineList = new ArrayList<>();
    private int mineSortType = Constant.SORT_UPDATE_DATE;
    private int mineTempSortType = Constant.SORT_UPDATE_DATE;
    private boolean mineSortReverse = false;
    private boolean mineTempSortReverse = false;
    private boolean[] mineCheck;
    private List<Integer> mineFilterStatusList = new ArrayList<>();

    private List<Report> othersList = new ArrayList<>();
    private List<Report> showOthersList = new ArrayList<>();
    private int othersSortType = Constant.SORT_UPDATE_DATE;
    private int othersTempSortType = Constant.SORT_UPDATE_DATE;
    private boolean othersSortReverse = false;
    private boolean othersTempSortReverse = false;
    private boolean[] othersCheck;
    private List<Integer> othersFilterStatusList = new ArrayList<>();

    // View
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
            setListView(ReimApplication.getReportTabIndex(), false);
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
            setListView(ReimApplication.getReportTabIndex(), true);
            syncReports();
        }
    }

    private void initView()
    {
        initTitleView();
        initListView();
        initFilterWindow();
        initOperationWindow();
    }

    private void initTitleView()
    {
        myTitleTextView = (TextView) getActivity().findViewById(R.id.myTitleTextView);
        myTitleTextView.setOnClickListener(this);

        myBadgeTextView = (TextView) view.findViewById(R.id.myBadgeTextView);

        othersTitleTextView = (TextView) getActivity().findViewById(R.id.othersTitleTextView);
        othersTitleTextView.setOnClickListener(this);

        othersBadgeTextView = (TextView) view.findViewById(R.id.othersBadgeTextView);

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

                if (appPreference.hasProxyEditPermission())
                {
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
                else
                {
                    ViewUtils.showToast(getActivity(), R.string.error_create_report_no_permission);
                }
            }
        });
    }

    private void initListView()
    {
        adapter = new ReportListViewAdapter(getActivity(), showMineList);

        reportListView = (XListView) getActivity().findViewById(R.id.reportListView);
        reportListView.setAdapter(adapter);
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
                if ((operationPopupWindow == null || !operationPopupWindow.isShowing()) && position > 0)
                {
                    if (ReimApplication.getReportTabIndex() == Constant.TAB_REPORT_MINE)
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
                showOperationWindow();
                return false;
            }
        });
    }

    private void initFilterWindow()
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
                if (ReimApplication.getReportTabIndex() == Constant.TAB_REPORT_MINE)
                {
                    MobclickAgent.onEvent(getActivity(), "UMENG_SHEET_MY_MODIFY_DATE");
                    if (mineTempSortType != Constant.SORT_UPDATE_DATE)
                    {
                        mineTempSortReverse = false;
                        mineTempSortType = Constant.SORT_UPDATE_DATE;
                    }
                    else
                    {
                        reverseSortUpdateImageView();
                    }
                }
                else
                {
                    MobclickAgent.onEvent(getActivity(), "UMENG_SHEET_OTHERS_MODIFY_DATE");
                    if (othersTempSortType != Constant.SORT_UPDATE_DATE)
                    {
                        othersTempSortReverse = false;
                        othersTempSortType = Constant.SORT_UPDATE_DATE;
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
                if (ReimApplication.getReportTabIndex() == Constant.TAB_REPORT_MINE)
                {
                    MobclickAgent.onEvent(getActivity(), "UMENG_SHEET_MY_CREATE_DATE");
                    if (mineTempSortType != Constant.SORT_CREATE_DATE)
                    {
                        mineTempSortReverse = false;
                        mineTempSortType = Constant.SORT_CREATE_DATE;
                    }
                    else
                    {
                        reverseSortCreateImageView();
                    }
                }
                else
                {
                    MobclickAgent.onEvent(getActivity(), "UMENG_SHEET_OTHERS_CREATE_DATE");
                    if (othersTempSortType != Constant.SORT_CREATE_DATE)
                    {
                        othersTempSortReverse = false;
                        othersTempSortType = Constant.SORT_CREATE_DATE;
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
                if (ReimApplication.getReportTabIndex() == Constant.TAB_REPORT_MINE)
                {
                    MobclickAgent.onEvent(getActivity(), "UMENG_SHEET_MY_AMOUNT");
                    if (mineTempSortType != Constant.SORT_AMOUNT)
                    {
                        mineTempSortReverse = false;
                        mineTempSortType = Constant.SORT_AMOUNT;
                    }
                    else
                    {
                        reverseSortAmountImageView();
                    }
                }
                else
                {
                    MobclickAgent.onEvent(getActivity(), "UMENG_SHEET_OTHERS_AMOUNT");
                    if (othersTempSortType != Constant.SORT_AMOUNT)
                    {
                        othersTempSortReverse = false;
                        othersTempSortType = Constant.SORT_AMOUNT;
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

        GridView tagGridView = (GridView) filterView.findViewById(R.id.tagGridView);
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

        ImageView confirmImageView = (ImageView) filterView.findViewById(R.id.confirmImageView);
        confirmImageView.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                if (ReimApplication.getReportTabIndex() == Constant.TAB_REPORT_MINE)
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
                refreshReportListView(false);
                ReimProgressDialog.dismiss();
            }
        });

        ImageView cancelImageView = (ImageView) filterView.findViewById(R.id.cancelImageView);
        cancelImageView.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                filterPopupWindow.dismiss();
            }
        });

        filterPopupWindow = ViewUtils.buildTopPopupWindow(getActivity(), filterView);
    }

    private void initOperationWindow()
    {
        View operationView = View.inflate(getActivity(), R.layout.window_report_operation, null);

        exportButton = (Button) operationView.findViewById(R.id.exportButton);
        exportButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                operationPopupWindow.dismiss();

                Report report = ReimApplication.getReportTabIndex() == Constant.TAB_REPORT_MINE ?
                        showMineList.get(reportIndex) : showOthersList.get(reportIndex);
                Bundle bundle = new Bundle();
                bundle.putSerializable("report", report);
                Intent intent = new Intent(getActivity(), ExportActivity.class);
                intent.putExtras(bundle);
                ViewUtils.goForward(getActivity(), intent);
            }
        });

        deleteButton = (Button) operationView.findViewById(R.id.deleteButton);
        deleteButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                operationPopupWindow.dismiss();

                if (ReimApplication.getReportTabIndex() == Constant.TAB_REPORT_MINE)
                {
                    final Report report = showMineList.get(reportIndex);
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
                    final Report report = showOthersList.get(reportIndex);
                    Builder builder = new Builder(getActivity());
                    builder.setTitle(R.string.warning);
                    builder.setMessage(R.string.prompt_delete_report);
                    builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener()
                    {
                        public void onClick(DialogInterface dialog, int which)
                        {
                            if (!PhoneUtils.isNetworkConnected())
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

    private void showFilterWindow()
    {
        if (ReimApplication.getReportTabIndex() == Constant.TAB_REPORT_MINE)
        {
            mineTempSortReverse = false;
            mineTempSortType = mineSortType;
            switch (mineSortType)
            {
                case Constant.SORT_UPDATE_DATE:
                {
                    selectSortUpdateDateRadio();
                    if (mineSortReverse)
                    {
                        reverseSortUpdateImageView();
                    }
                    break;
                }
                case Constant.SORT_CREATE_DATE:
                {
                    selectSortCreateDateRadio();
                    if (mineSortReverse)
                    {
                        reverseSortCreateImageView();
                    }
                    break;
                }
                case Constant.SORT_AMOUNT:
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
                case Constant.SORT_UPDATE_DATE:
                {
                    selectSortUpdateDateRadio();
                    if (othersSortReverse)
                    {
                        reverseSortUpdateImageView();
                    }
                    break;
                }
                case Constant.SORT_CREATE_DATE:
                {
                    selectSortCreateDateRadio();
                    if (othersSortReverse)
                    {
                        reverseSortCreateImageView();
                    }
                    break;
                }
                case Constant.SORT_AMOUNT:
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
        if (ReimApplication.getReportTabIndex() == Constant.TAB_REPORT_MINE)
        {
            Report report = showMineList.get(reportIndex);
            if (report.getStatus() != Report.STATUS_APPROVED && report.getStatus() != Report.STATUS_FINISHED)
            {
                exportButton.setEnabled(false);
                exportButton.setBackgroundResource(R.drawable.window_button_pressed);
                exportButton.setTextColor(ViewUtils.getColor(R.color.button_text_light));
            }
            else
            {
                exportButton.setEnabled(true);
                exportButton.setBackgroundResource(R.drawable.window_button_drawable);
                exportButton.setTextColor(ViewUtils.getColorStateList(R.color.button_text_dark_color));
            }

            if (report.getStatus() == Report.STATUS_SUBMITTED || report.getStatus() == Report.STATUS_APPROVED)
            {
                deleteButton.setEnabled(false);
                deleteButton.setBackgroundResource(R.drawable.window_button_pressed);
                deleteButton.setTextColor(ViewUtils.getColor(R.color.button_text_light));
            }
            else
            {
                deleteButton.setEnabled(true);
                deleteButton.setBackgroundResource(R.drawable.window_button_drawable);
                deleteButton.setTextColor(ViewUtils.getColorStateList(R.color.button_text_dark_color));
            }
        }
        else
        {
            Report report = showOthersList.get(reportIndex);
            if (report.getStatus() != Report.STATUS_APPROVED && report.getStatus() != Report.STATUS_FINISHED)
            {
                exportButton.setEnabled(false);
                exportButton.setBackgroundResource(R.drawable.window_button_pressed);
                exportButton.setTextColor(ViewUtils.getColor(R.color.button_text_light));
            }
            else
            {
                exportButton.setEnabled(true);
                exportButton.setBackgroundResource(R.drawable.window_button_drawable);
                exportButton.setTextColor(ViewUtils.getColorStateList(R.color.button_text_dark_color));
            }

            if (report.getStatus() == Report.STATUS_SUBMITTED && report.getMyDecision() == Report.STATUS_SUBMITTED)
            {
                deleteButton.setEnabled(false);
                deleteButton.setBackgroundResource(R.drawable.window_button_pressed);
                deleteButton.setTextColor(ViewUtils.getColor(R.color.button_text_light));
            }
            else
            {
                deleteButton.setEnabled(true);
                deleteButton.setBackgroundResource(R.drawable.window_button_drawable);
                deleteButton.setTextColor(ViewUtils.getColorStateList(R.color.button_text_dark_color));
            }
        }

        operationPopupWindow.showAtLocation(getActivity().findViewById(R.id.containerLayout), Gravity.BOTTOM, 0, 0);
        operationPopupWindow.update();

        ViewUtils.dimBackground(getActivity());
    }

    public void setListView(int index, boolean readDatabase)
    {
        ReimApplication.setReportTabIndex(index);
        if (index == Constant.TAB_REPORT_MINE)
        {
            myTitleTextView.setTextColor(ViewUtils.getColor(R.color.major_light));
            othersTitleTextView.setTextColor(ViewUtils.getColor(R.color.hint_light));
        }
        else
        {
            myTitleTextView.setTextColor(ViewUtils.getColor(R.color.hint_light));
            othersTitleTextView.setTextColor(ViewUtils.getColor(R.color.major_light));
            if (PhoneUtils.isNetworkConnected() && Utils.getCurrentTime() - appPreference.getLastGetOthersReportTime() > Constant.GET_DATA_INTERVAL)
            {
                sendSubordinatesReportsRequest();
            }
        }
        refreshReportListView(readDatabase);
    }

    public void showBadge()
    {
        if (myBadgeTextView == null || othersBadgeTextView == null)
        {
            return;
        }

        int count = ReimApplication.getMineUnreadList().size();
        if (count == 0)
        {
            myBadgeTextView.setVisibility(View.GONE);
        }
        else if (count < 100)
        {
            myBadgeTextView.setVisibility(View.VISIBLE);
            myBadgeTextView.setText(Integer.toString(count));
        }
        else
        {
            myBadgeTextView.setVisibility(View.VISIBLE);
            myBadgeTextView.setText(R.string.report_over_flow);
        }

        count = ReimApplication.getOthersUnreadList().size();
        if (count == 0)
        {
            othersBadgeTextView.setVisibility(View.GONE);
        }
        else if (count < 100)
        {
            othersBadgeTextView.setVisibility(View.VISIBLE);
            othersBadgeTextView.setText(Integer.toString(count));
        }
        else
        {
            othersBadgeTextView.setVisibility(View.VISIBLE);
            othersBadgeTextView.setText(R.string.report_over_flow);
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
        if (ReimApplication.getReportTabIndex() == Constant.TAB_REPORT_MINE)
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
        if (ReimApplication.getReportTabIndex() == Constant.TAB_REPORT_MINE)
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
        if (ReimApplication.getReportTabIndex() == Constant.TAB_REPORT_MINE)
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

    private void refreshReportListView(boolean readDatabase)
    {
        if (ReimApplication.getReportTabIndex() == Constant.TAB_REPORT_MINE)
        {
            if (readDatabase)
            {
                mineList.clear();
                mineList.addAll(readMineReportList());
            }
            showMineList.clear();
            showMineList.addAll(filterReportList(mineList, mineSortType, mineSortReverse, mineFilterStatusList, true));
            adapter.setReportList(showMineList);
            adapter.setUnreadList(ReimApplication.getMineUnreadList());
            adapter.setTabIndex(Constant.TAB_REPORT_MINE);
            adapter.notifyDataSetChanged();

            int visibility = !mineFilterStatusList.isEmpty() && showMineList.isEmpty() ? View.VISIBLE : View.GONE;
            noResultLayout.setVisibility(visibility);

            int filterImage = mineFilterStatusList.isEmpty() ? R.drawable.filter_empty : R.drawable.filter_full;
            filterImageView.setImageResource(filterImage);
        }
        else
        {
            if (readDatabase)
            {
                othersList.clear();
                othersList.addAll(readOthersReportList());
            }
            if (!othersList.isEmpty() && othersFilterStatusList.isEmpty() && othersSortType == Constant.SORT_UPDATE_DATE && !othersSortReverse)
            {
                buildReportListByStatus();
            }
            else
            {
                showOthersList.clear();
                showOthersList.addAll(filterReportList(othersList, othersSortType, othersSortReverse, othersFilterStatusList,false));
            }

            adapter.setReportList(showOthersList);
            adapter.setUnreadList(ReimApplication.getOthersUnreadList());
            adapter.setTabIndex(Constant.TAB_REPORT_OTHERS);
            adapter.notifyDataSetChanged();

            int visibility = !othersFilterStatusList.isEmpty() && showOthersList.isEmpty() ? View.VISIBLE : View.GONE;
            noResultLayout.setVisibility(visibility);

            int filterImage = othersFilterStatusList.isEmpty() ? R.drawable.filter_empty : R.drawable.filter_full;
            filterImageView.setImageResource(filterImage);
        }
        showBadge();
    }

    public void onClick(View v)
    {
        if (v.equals(myTitleTextView))
        {
            setListView(0, false);
        }
        else
        {
            setListView(1, false);
        }
    }

    // Data
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
        showMineList.addAll(filterReportList(mineList, mineSortType, mineSortReverse, mineFilterStatusList, true));

        othersList.addAll(readOthersReportList());
        showOthersList.addAll(filterReportList(othersList, othersSortType, othersSortReverse, othersFilterStatusList, false));

        mineCheck = new boolean[5];
        othersCheck = new boolean[5];
        for (int i = 0; i < 5; i++)
        {
            mineCheck[i] = false;
            othersCheck[i] = false;
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

    private List<Report> filterReportList(List<Report> reportList, int sortType, boolean sortReverse, List<Integer> filterStatusList, boolean filterMine)
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
            case Constant.SORT_UPDATE_DATE:
                Report.sortByUpdateDate(resultList);
                break;
            case Constant.SORT_CREATE_DATE:
                Report.sortByCreateDate(resultList);
                break;
            case Constant.SORT_AMOUNT:
            {
                if (filterMine)
                {
                    Report.sortMineByAmount(resultList);
                }
                else
                {
                    Report.sortOthersByAmount(resultList);
                }
                break;
            }
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
        if (isAdded())
        {
            report.setSectionName(getString(R.string.pending));
        }
        showOthersList.add(report);

        if (pendingList.isEmpty())
        {
            Report noPendingReport = new Report();
            if (isAdded())
            {
                noPendingReport.setSectionName(getString(R.string.no_pending_reports));
            }
            showOthersList.add(noPendingReport);
        }
        else
        {
            showOthersList.addAll(pendingList);
        }

        if (!processedList.isEmpty())
        {
            Report processedReport = new Report();
            if (isAdded())
            {
                processedReport.setSectionName(getString(R.string.processed));
            }
            showOthersList.add(processedReport);
            showOthersList.addAll(processedList);
        }
    }

    // Network
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
                            int reportID = ReimApplication.getReportTabIndex() == Constant.TAB_REPORT_MINE ?
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
                        else if (report.getStep() != localReport.getStep())
                        {
                            dbManager.updateOthersReport(report);
                        }
                        else
                        {
                            report.setManagerList(localReport.getManagerList());
                            report.setCCList(localReport.getCCList());
                            dbManager.updateOthersReport(report);
                        }
                    }

                    if (isAdded())
                    {
                        getActivity().runOnUiThread(new Runnable()
                        {
                            public void run()
                            {
                                appPreference.setLastGetOthersReportTime(Utils.getCurrentTime());
                                appPreference.saveAppPreference();
                                reportListView.stopRefresh();
                                reportListView.setRefreshTime(Utils.secondToStringUpToMinute(Utils.getCurrentTime()));
                                refreshReportListView(true);
                            }
                        });
                    }
                }
                else if (isAdded())
                {
                    getActivity().runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            reportListView.stopRefresh();
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
                final EventsResponse response = new EventsResponse(httpResponse);
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
                            ViewUtils.showToast(getActivity(), R.string.failed_to_get_data, response.getErrorMessage());
                        }
                    });
                }
            }
        });
    }

    private void deleteLocalReport(int reportID)
    {
        if (ReimApplication.getReportTabIndex() == Constant.TAB_REPORT_MINE)
        {
            if (dbManager.deleteReport(reportID))
            {
                refreshReportListView(true);
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
                refreshReportListView(true);
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

    public void syncReports()
    {
        if (SyncUtils.canSyncToServer())
        {
            SyncUtils.isSyncOnGoing = true;
            SyncUtils.syncFromServer(new SyncDataCallback()
            {
                public void execute()
                {
                    if (getActivity() != null)
                    {
                        getActivity().runOnUiThread(new Runnable()
                        {
                            public void run()
                            {
                                refreshReportListView(true);
                            }
                        });
                    }

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
        if (ReimApplication.getReportTabIndex() == Constant.TAB_REPORT_MINE)
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
                                refreshReportListView(true);
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
}