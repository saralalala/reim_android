package com.rushucloud.reim.report;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.rushucloud.reim.R;
import com.rushucloud.reim.item.EditItemActivity;
import com.rushucloud.reim.main.MainActivity;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.List;

import classes.adapter.ReportDetailListViewAdapter;
import classes.model.Comment;
import classes.model.Group;
import classes.model.Item;
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
import netUtils.common.HttpConnectionCallback;
import netUtils.common.NetworkConstant;
import netUtils.request.group.GetGroupRequest;
import netUtils.request.report.ApproveReportRequest;
import netUtils.request.report.CheckPolicyRequest;
import netUtils.request.report.GetReportRequest;
import netUtils.response.group.GetGroupResponse;
import netUtils.response.report.ApproveReportResponse;
import netUtils.response.report.CheckPolicyResponse;
import netUtils.response.report.GetReportResponse;

public class ApproveReportActivity extends Activity
{
    // Widgets
    private ImageView tipImageView;
    private ReportDetailListViewAdapter adapter;

    // Local Data
    private AppPreference appPreference;
    private DBManager dbManager;

    private int reportServerID;
    private Report report;
    private List<Item> itemList = new ArrayList<>();
    private int lastCommentCount;

    private boolean fromPush;

    // View
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_approve);
        initData();
        initView();
    }

    protected void onResume()
    {
        super.onResume();
        MobclickAgent.onPageStart("ApproveReportActivity");
        MobclickAgent.onResume(this);
        ReimProgressDialog.setContext(this);
        refreshData();
    }

    protected void onPause()
    {
        super.onPause();
        MobclickAgent.onPageEnd("ApproveReportActivity");
        MobclickAgent.onPause(this);
    }

    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_BACK)
        {
            goBackToMainActivity();
        }
        return super.onKeyDown(keyCode, event);
    }

    private void initView()
    {
        ImageView backImageView = (ImageView) findViewById(R.id.backImageView);
        backImageView.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                goBackToMainActivity();
            }
        });

        ImageView commentImageView = (ImageView) findViewById(R.id.commentImageView);
        commentImageView.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                MobclickAgent.onEvent(ApproveReportActivity.this, "UMENG_REPORT_OTHER_COMMENT");

                tipImageView.setVisibility(View.GONE);

                Bundle bundle = new Bundle();
                bundle.putSerializable("report", report);
                bundle.putBoolean("myReport", false);
                Intent intent = new Intent(ApproveReportActivity.this, CommentActivity.class);
                intent.putExtras(bundle);
                ViewUtils.goForward(ApproveReportActivity.this, intent);
            }
        });

        tipImageView = (ImageView) findViewById(R.id.tipImageView);

        Button approveButton = (Button) findViewById(R.id.approveButton);
        approveButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                MobclickAgent.onEvent(ApproveReportActivity.this, "UMENG_PASS_REPORT_DETAIL");
                if (!PhoneUtils.isNetworkConnected())
                {
                    ViewUtils.showToast(ApproveReportActivity.this, R.string.error_approve_network_unavailable);
                }
                else
                {
                    sendCheckPolicyRequest(reportServerID);
                }
            }
        });

        Button rejectButton = (Button) findViewById(R.id.rejectButton);
        rejectButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                MobclickAgent.onEvent(ApproveReportActivity.this, "UMENG_REJECT_REPORT_DETAIL");
                if (!PhoneUtils.isNetworkConnected())
                {
                    ViewUtils.showToast(ApproveReportActivity.this, R.string.error_approve_network_unavailable);
                }
                else
                {
                    showRejectDialog();
                }
            }
        });

        adapter = new ReportDetailListViewAdapter(ApproveReportActivity.this, report, itemList);
        ListView detailListView = (ListView) findViewById(R.id.detailListView);
        detailListView.setAdapter(adapter);
        detailListView.setOnItemClickListener(new OnItemClickListener()
        {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                if (position > 0)
                {
                    Item item = itemList.get(position - 1);
                    Intent intent = new Intent(ApproveReportActivity.this, EditItemActivity.class);
                    intent.putExtra("fromApproveReport", true);
                    intent.putExtra("itemServerID", item.getServerID());
                    ViewUtils.goForward(ApproveReportActivity.this, intent);
                }
            }
        });
    }

    private void refreshView()
    {
        if (report.getStatus() != Report.STATUS_SUBMITTED)
        {
            ViewUtils.showToast(ApproveReportActivity.this, R.string.error_report_approved);
            goBackToMainActivity();
        }
        else if (fromPush && !report.canBeApproved())
        {
            ReimApplication.setTabIndex(Constant.TAB_REPORT);
            ReimApplication.setReportTabIndex(Constant.TAB_REPORT_OTHERS);

            Bundle bundle = new Bundle();
            bundle.putSerializable("report", report);
            bundle.putBoolean("myReport", false);
            bundle.putBoolean("fromPush", fromPush);

            Intent intent = new Intent(ApproveReportActivity.this, ShowReportActivity.class);
            intent.putExtras(bundle);
            startActivity(intent);
            finish();
        }
        else
        {
            User user = dbManager.getUser(report.getSender().getServerID());
            if (user != null)
            {
                report.setSender(user);
                adapter.setReport(report);
                adapter.setItemList(itemList);
                adapter.notifyDataSetChanged();

                if (report.getCommentList().size() != lastCommentCount)
                {
                    tipImageView.setVisibility(View.VISIBLE);
                    lastCommentCount = report.getCommentList().size();
                }
            }
            else
            {
                ViewUtils.showToast(ApproveReportActivity.this, R.string.failed_to_get_data);
                goBackToMainActivity();
            }
        }
    }

    private void showRejectDialog()
    {
        View view = View.inflate(this, R.layout.dialog_report_comment, null);

        TextView titleTextView = (TextView) view.findViewById(R.id.titleTextView);
        titleTextView.setText(R.string.reject_reason);

        final EditText commentEditText = (EditText) view.findViewById(R.id.commentEditText);
        commentEditText.setOnFocusChangeListener(ViewUtils.onFocusChangeListener);
        ViewUtils.requestFocus(this, commentEditText);

        Builder builder = new Builder(this);
        builder.setView(view);
        builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int which)
            {
                MobclickAgent.onEvent(ApproveReportActivity.this, "UMENG_REPORT_OTHER_DIALOG_COMMENT_SEND");
                report.setMyDecision(Report.STATUS_REJECTED);
                sendRejectReportRequest(commentEditText.getText().toString());
            }
        });
        builder.setNegativeButton(R.string.cancel, new OnClickListener()
        {
            public void onClick(DialogInterface dialog, int which)
            {
                MobclickAgent.onEvent(ApproveReportActivity.this, "UMENG_REPORT_OTHER_DIALOG_COMMENT_CLOSE");
            }
        });
        builder.create().show();
    }

    private void jumpToFollowingActivity(ArrayList<Integer> managerIDList, boolean isFixedProcess)
    {
        Intent intent = new Intent(ApproveReportActivity.this, FollowingActivity.class);
        intent.putExtra("report", report);
        if (managerIDList != null)
        {
            intent.putIntegerArrayListExtra("managerIDList", managerIDList);
            intent.putExtra("canBeFinished", false);
            intent.putExtra("isFixedProcess", isFixedProcess);
        }
        ViewUtils.goForwardAndFinish(this, intent);
    }

    private void goBackToMainActivity()
    {
        ReimApplication.setTabIndex(Constant.TAB_REPORT);
        ReimApplication.setReportTabIndex(Constant.TAB_REPORT_OTHERS);

        if (fromPush)
        {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            ViewUtils.goBackWithIntent(this, intent);
        }
        else
        {
            ViewUtils.goBack(this);
        }
    }

    // Data
    private void initData()
    {
        appPreference = AppPreference.getAppPreference();
        dbManager = DBManager.getDBManager();

        Bundle bundle = getIntent().getExtras();
        if (bundle != null)
        {
            report = (Report) bundle.getSerializable("report");
            fromPush = bundle.getBoolean("fromPush", false);
            reportServerID = report.getServerID();
            itemList = dbManager.getOthersReportItems(reportServerID);
            lastCommentCount = report.getCommentList() != null ? report.getCommentList().size() : 0;
        }
    }

    private void refreshData()
    {
        itemList = dbManager.getOthersReportItems(reportServerID);
        adapter.setItemList(itemList);
        adapter.notifyDataSetChanged();

        List<Comment> commentList = dbManager.getOthersReportComments(report.getServerID());
        lastCommentCount = commentList.size();

        if (PhoneUtils.isNetworkConnected())
        {
            sendGetReportRequest(reportServerID);
        }
        else if (itemList.isEmpty())
        {
            ViewUtils.showToast(this, R.string.error_get_data_network_unavailable);
        }
    }

    private void updateReport(Report responseReport, List<Item> responseItemList)
    {
        report = new Report(responseReport);
        dbManager.deleteOthersReport(reportServerID, appPreference.getCurrentUserID());
        dbManager.insertOthersReport(report);

        for (Item item : responseItemList)
        {
            dbManager.insertOthersItem(item);
        }
        itemList = dbManager.getOthersReportItems(reportServerID);

        for (Comment comment : report.getCommentList())
        {
            comment.setReportID(report.getServerID());
            dbManager.insertOthersComment(comment);
        }
    }

    // Network
    private void sendGetReportRequest(final int reportServerID)
    {
        ReimProgressDialog.show();
        GetReportRequest request = new GetReportRequest(reportServerID);
        request.sendRequest(new HttpConnectionCallback()
        {
            public void execute(Object httpResponse)
            {
                final GetReportResponse response = new GetReportResponse(httpResponse);
                if (response.getStatus())
                {
                    if (!response.containsUnsyncedUser())
                    {
                        updateReport(response.getReport(), response.getItemList());
                    }
                    else
                    {
                        Report report = response.getReport();
                        report.setManagerList(response.getManagerList());
                        report.setCCList(response.getCCList());
                        sendGetGroupRequest(response.getReport(), response.getItemList());
                    }

                    runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            ReimProgressDialog.dismiss();
                            refreshView();
                        }
                    });
                }
                else
                {
                    runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            ReimProgressDialog.dismiss();
                            ViewUtils.showToast(ApproveReportActivity.this, R.string.failed_to_get_data, response.getErrorMessage());
                            if (response.getCode() == NetworkConstant.ERROR_REPORT_DELETED || response.getCode() == NetworkConstant.ERROR_REPORT_NOT_EXISTS)
                            {
                                dbManager.deleteOthersReport(reportServerID, AppPreference.getAppPreference().getCurrentUserID());
                            }
                            goBackToMainActivity();
                        }
                    });
                }
            }
        });
    }

    private void sendApproveReportRequest()
    {
        ReimProgressDialog.show();
        ApproveReportRequest request = new ApproveReportRequest(report, true);
        request.sendRequest(new HttpConnectionCallback()
        {
            public void execute(Object httpResponse)
            {
                final ApproveReportResponse response = new ApproveReportResponse(httpResponse);
                if (response.getStatus())
                {
                    report.setStatus(response.getReportStatus());
                    dbManager.updateOthersReport(report);

                    runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            ReimProgressDialog.dismiss();
                            ViewUtils.showToast(ApproveReportActivity.this, R.string.prompt_report_approved);
                            goBackToMainActivity();
                        }
                    });
                }
                else
                {
                    runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            ReimProgressDialog.dismiss();
                            ViewUtils.showToast(ApproveReportActivity.this, R.string.error_operation_failed, response.getErrorMessage());
                        }
                    });
                }
            }
        });
    }

    private void sendCheckPolicyRequest(final int reportServerID)
    {
        ReimProgressDialog.show();
        CheckPolicyRequest request = new CheckPolicyRequest(reportServerID);
        request.sendRequest(new HttpConnectionCallback()
        {
            public void execute(Object httpResponse)
            {
                final CheckPolicyResponse response = new CheckPolicyResponse(httpResponse);
                runOnUiThread(new Runnable()
                {
                    public void run()
                    {
                        ReimProgressDialog.dismiss();
                        if (response.getStatus())
                        {
                            Group group = appPreference.getCurrentGroup();
                            if (response.isReportCanBeFinished() && group.reportCanBeClosedDirectly())
                            {
                                report.setMyDecision(Report.STATUS_APPROVED);
                                sendApproveReportRequest();
                            }
                            else if (response.isReportCanBeFinished())
                            {
                                Builder builder = new Builder(ApproveReportActivity.this);
                                builder.setTitle(R.string.tip);
                                builder.setMessage(R.string.prompt_choose_or_finish);
                                builder.setPositiveButton(R.string.continue_to_choose, new DialogInterface.OnClickListener()
                                {
                                    public void onClick(DialogInterface dialog, int which)
                                    {
                                        jumpToFollowingActivity(null, false);
                                    }
                                });
                                builder.setNegativeButton(R.string.finish, new DialogInterface.OnClickListener()
                                {
                                    public void onClick(DialogInterface dialog, int which)
                                    {
                                        report.setMyDecision(Report.STATUS_APPROVED);
                                        sendApproveReportRequest();
                                    }
                                });
                                builder.create().show();
                            }
                            else if (response.isFixedProcess())
                            {
                                report.setMyDecision(Report.STATUS_APPROVED);
                                report.setManagerList(dbManager.getUsers(response.getManagerIDList()));
                                sendApproveReportRequest();
                            }
                            else
                            {
                                jumpToFollowingActivity(response.getManagerIDList(), response.isFixedProcess());
                            }
                        }
                        else
                        {
                            ViewUtils.showToast(ApproveReportActivity.this, R.string.failed_to_get_data,
                                                response.getErrorMessage());
                        }
                    }
                });
            }
        });
    }

    private void sendRejectReportRequest(final String commentContent)
    {
        ReimProgressDialog.show();

        ApproveReportRequest request = new ApproveReportRequest(report, commentContent);
        request.sendRequest(new HttpConnectionCallback()
        {
            public void execute(Object httpResponse)
            {
                final ApproveReportResponse response = new ApproveReportResponse(httpResponse);
                if (response.getStatus())
                {
                    report.setStatus(response.getReportStatus());
                    dbManager.updateOthersReport(report);

                    if (!commentContent.isEmpty())
                    {
                        int currentTime = Utils.getCurrentTime();
                        User user = appPreference.getCurrentUser();

                        Comment comment = new Comment();
                        comment.setContent(commentContent);
                        comment.setCreatedDate(currentTime);
                        comment.setLocalUpdatedDate(currentTime);
                        comment.setServerUpdatedDate(currentTime);
                        comment.setReportID(report.getServerID());
                        comment.setReviewer(user);

                        dbManager.insertOthersComment(comment);
                    }

                    runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            ReimProgressDialog.dismiss();
                            ViewUtils.showToast(ApproveReportActivity.this, R.string.prompt_report_rejected);
                            goBackToMainActivity();
                        }
                    });
                }
                else
                {
                    runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            ReimProgressDialog.dismiss();
                            ViewUtils.showToast(ApproveReportActivity.this, R.string.error_operation_failed, response.getErrorMessage());
                        }
                    });
                }
            }
        });
    }

    private void sendGetGroupRequest(final Report responseReport, final List<Item> responseItemList)
    {
        GetGroupRequest request = new GetGroupRequest();
        request.sendRequest(new HttpConnectionCallback()
        {
            public void execute(Object httpResponse)
            {
                final GetGroupResponse response = new GetGroupResponse(httpResponse);
                if (response.getStatus())
                {
                    Utils.updateGroupMembers(response.getGroup(), response.getMemberList(), dbManager);

                    // update report
                    updateReport(responseReport, responseItemList);
                    report = dbManager.getReportByServerID(responseReport.getServerID());

                    runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            ReimProgressDialog.dismiss();
                            refreshView();
                        }
                    });
                }
                else
                {
                    runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            ReimProgressDialog.dismiss();
                            ViewUtils.showToast(ApproveReportActivity.this, R.string.failed_to_get_data, response.getErrorMessage());
                            goBackToMainActivity();
                        }
                    });
                }
            }
        });
    }
}