package com.rushucloud.reim.report;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.rushucloud.reim.R;
import com.rushucloud.reim.item.ShowItemActivity;
import com.rushucloud.reim.main.MainActivity;
import com.umeng.analytics.MobclickAgent;

import java.util.List;

import classes.adapter.ReportDetailListViewAdapter;
import classes.model.Comment;
import classes.model.Item;
import classes.model.Report;
import classes.model.User;
import classes.utils.AppPreference;
import classes.utils.Constant;
import classes.utils.DBManager;
import classes.utils.PhoneUtils;
import classes.utils.ReimApplication;
import classes.utils.ViewUtils;
import classes.widget.ReimProgressDialog;
import netUtils.common.HttpConnectionCallback;
import netUtils.common.NetworkConstant;
import netUtils.request.group.GetGroupRequest;
import netUtils.request.report.GetReportRequest;
import netUtils.request.report.RevokeReportRequest;
import netUtils.response.group.GetGroupResponse;
import netUtils.response.report.GetReportResponse;
import netUtils.response.report.RevokeReportResponse;

public class ShowReportActivity extends Activity
{
    // Widgets
    private ImageView tipImageView;
    private ReportDetailListViewAdapter adapter;
    private LinearLayout revokeDivider;
    private Button revokeButton;

    // Local Data
    private DBManager dbManager;

    private Report report;
    private List<Item> itemList = null;
    private boolean fromPush;
    private boolean myReport;
    private int lastCommentCount;

    // View
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_show);
        MobclickAgent.onEvent(ShowReportActivity.this, "UMENG_VIEW_REPORT");
        initData();
        initView();
    }

    protected void onResume()
    {
        super.onResume();
        MobclickAgent.onPageStart("ShowReportActivity");
        MobclickAgent.onResume(this);
        ReimProgressDialog.setContext(this);
        refreshView();
    }

    protected void onPause()
    {
        super.onPause();
        MobclickAgent.onPageEnd("ShowReportActivity");
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
                if (myReport)
                {
                    MobclickAgent.onEvent(ShowReportActivity.this, "UMENG_REPORT_MINE_COMMENT");
                }
                else
                {
                    MobclickAgent.onEvent(ShowReportActivity.this, "UMENG_REPORT_OTHER_COMMENT");
                }

                tipImageView.setVisibility(View.GONE);

                Bundle bundle = new Bundle();
                bundle.putSerializable("report", report);
                bundle.putBoolean("myReport", myReport);
                Intent intent = new Intent(ShowReportActivity.this, CommentActivity.class);
                intent.putExtras(bundle);
                ViewUtils.goForward(ShowReportActivity.this, intent);
            }
        });

        tipImageView = (ImageView) findViewById(R.id.tipImageView);

        adapter = new ReportDetailListViewAdapter(ShowReportActivity.this, report, itemList);
        ListView detailListView = (ListView) findViewById(R.id.detailListView);
        detailListView.setAdapter(adapter);
        detailListView.setOnItemClickListener(new OnItemClickListener()
        {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id)
            {
                if (position > 0)
                {
                    Intent intent = new Intent(ShowReportActivity.this, ShowItemActivity.class);
                    if (myReport)
                    {
                        intent.putExtra("itemLocalID", itemList.get(position - 1).getLocalID());
                    }
                    else
                    {
                        intent.putExtra("othersItemServerID", itemList.get(position - 1).getServerID());
                    }
                    ViewUtils.goForward(ShowReportActivity.this, intent);
                }
            }
        });

        revokeDivider = (LinearLayout) findViewById(R.id.revokeDivider);

        revokeButton = (Button) findViewById(R.id.revokeButton);
        revokeButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View view)
            {
                showRevokeDialog();
            }
        });
    }

    private void refreshView()
    {
        if (myReport)
        {
            List<Comment> commentList = dbManager.getReportComments(report.getLocalID());
            lastCommentCount = commentList.size();
        }
        else
        {
            List<Comment> commentList = dbManager.getOthersReportComments(report.getServerID());
            lastCommentCount = commentList.size();
        }

        refreshRevokeView();

        if (PhoneUtils.isNetworkConnected())
        {
            sendGetGroupRequest();
        }
        else
        {
            ViewUtils.showToast(this, R.string.error_get_data_network_unavailable);
        }
    }

    private void refreshRevokeView()
    {
        if (myReport && report.getStatus() != Report.STATUS_FINISHED)
        {
            revokeDivider.setVisibility(View.VISIBLE);
            revokeButton.setVisibility(View.VISIBLE);
        }
        else
        {
            revokeDivider.setVisibility(View.GONE);
            revokeButton.setVisibility(View.GONE);
        }
    }

    private void showRevokeDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.warning);
        builder.setMessage(R.string.prompt_revoke_report);
        builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int which)
            {
                sendRevokeReportRequest(report.getServerID());
            }
        });
        builder.setNegativeButton(R.string.cancel, null);
        builder.create().show();
    }

    private void goBackToMainActivity()
    {
        int reportTabIndex = myReport ? Constant.TAB_REPORT_MINE : Constant.TAB_REPORT_OTHERS;
        ReimApplication.setReportTabIndex(reportTabIndex);
        if (fromPush)
        {
            ReimApplication.setTabIndex(Constant.TAB_REPORT);
            Intent intent = new Intent(ShowReportActivity.this, MainActivity.class);
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
        dbManager = DBManager.getDBManager();

        Bundle bundle = getIntent().getExtras();
        if (bundle != null)
        {
            report = (Report) bundle.getSerializable("report");
            fromPush = bundle.getBoolean("fromPush", false);
            myReport = bundle.getBoolean("myReport", false);
            if (myReport)
            {
                if (fromPush)
                {
                    report = dbManager.getReportByServerID(report.getServerID());
                }
                itemList = dbManager.getReportItems(report.getLocalID());
            }
            else
            {
                itemList = dbManager.getOthersReportItems(report.getServerID());
            }

            lastCommentCount = report.getCommentList() != null ? report.getCommentList().size() : 0;
        }
    }

    // Network
    private void sendGetGroupRequest()
    {
        ReimProgressDialog.show();
        GetGroupRequest request = new GetGroupRequest();
        request.sendRequest(new HttpConnectionCallback()
        {
            public void execute(Object httpResponse)
            {
                final GetGroupResponse response = new GetGroupResponse(httpResponse);
                if (response.getStatus())
                {
                    int currentGroupID = response.getGroup() == null ? -1 : response.getGroup().getServerID();

                    // update members
                    List<User> memberList = response.getMemberList();
                    User currentUser = AppPreference.getAppPreference().getCurrentUser();

                    for (int i = 0; i < memberList.size(); i++)
                    {
                        User user = memberList.get(i);
                        if (currentUser != null && user.equals(currentUser))
                        {
                            if (user.getServerUpdatedDate() > currentUser.getServerUpdatedDate())
                            {
                                if (user.getAvatarID() == currentUser.getAvatarID())
                                {
                                    user.setAvatarLocalPath(currentUser.getAvatarLocalPath());
                                }
                            }
                            else
                            {
                                memberList.set(i, currentUser);
                            }
                            break;
                        }
                    }

                    dbManager.updateGroupUsers(memberList, currentGroupID);

                    // update group info
                    dbManager.syncGroup(response.getGroup());

                    sendGetReportRequest(report.getServerID());
                }
                else
                {
                    runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            ReimProgressDialog.dismiss();
                            ViewUtils.showToast(ShowReportActivity.this, R.string.failed_to_get_data, response.getErrorMessage());
                            goBackToMainActivity();
                        }
                    });
                }
            }
        });
    }

    private void sendGetReportRequest(final int reportServerID)
    {
        GetReportRequest request = new GetReportRequest(reportServerID);
        request.sendRequest(new HttpConnectionCallback()
        {
            public void execute(Object httpResponse)
            {
                final GetReportResponse response = new GetReportResponse(httpResponse);
                if (response.getStatus())
                {
                    int ownerID = AppPreference.getAppPreference().getCurrentUserID();
                    int localID = report.getLocalID();
                    report = new Report(response.getReport());
                    report.setLocalID(localID);

                    if (myReport)
                    {
                        dbManager.updateReportByServerID(report);

                        dbManager.deleteReportComments(report.getLocalID());
                        for (Comment comment : report.getCommentList())
                        {
                            comment.setReportID(report.getLocalID());
                            dbManager.insertComment(comment);
                        }
                    }
                    else
                    {
                        dbManager.deleteOthersReport(reportServerID, ownerID);
                        dbManager.insertOthersReport(report);

                        for (Item item : response.getItemList())
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

                    runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            ReimProgressDialog.dismiss();
                            if (report.canBeApproved())
                            {
                                Bundle bundle = new Bundle();
                                bundle.putSerializable("report", report);
                                Intent intent = new Intent(ShowReportActivity.this, ApproveReportActivity.class);
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
                                    refreshRevokeView();

                                    if (report.getCommentList().size() != lastCommentCount)
                                    {
                                        tipImageView.setVisibility(View.VISIBLE);
                                        lastCommentCount = report.getCommentList().size();
                                    }
                                }
                                else
                                {
                                    ViewUtils.showToast(ShowReportActivity.this, R.string.failed_to_get_data, response.getErrorMessage());
                                    goBackToMainActivity();
                                }
                            }
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
                            ViewUtils.showToast(ShowReportActivity.this, R.string.failed_to_get_data, response.getErrorMessage());
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

    private void sendRevokeReportRequest(final int reportServerID)
    {
        ReimProgressDialog.show();
        RevokeReportRequest request = new RevokeReportRequest(reportServerID);
        request.sendRequest(new HttpConnectionCallback()
        {
            public void execute(Object httpResponse)
            {
                final RevokeReportResponse response = new RevokeReportResponse(httpResponse);
                if (response.getStatus())
                {
                    int ownerID = AppPreference.getAppPreference().getCurrentUserID();
                    int localID = report.getLocalID();
                    report = new Report(response.getReport());
                    report.setLocalID(localID);

                    if (myReport)
                    {
                        dbManager.updateReportByServerID(report);

                        dbManager.deleteReportComments(report.getLocalID());
                        for (Comment comment : report.getCommentList())
                        {
                            comment.setReportID(report.getLocalID());
                            dbManager.insertComment(comment);
                        }
                    }
                    else
                    {
                        dbManager.deleteOthersReport(reportServerID, ownerID);
                        dbManager.insertOthersReport(report);

                        for (Item item : response.getItemList())
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

                    runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            ReimProgressDialog.dismiss();
                            ViewUtils.showToast(ShowReportActivity.this, R.string.succeed_in_revoking_report);
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
                            ViewUtils.showToast(ShowReportActivity.this, R.string.failed_to_revoke_report, response.getErrorMessage());
                        }
                    });
                }
            }
        });
    }
}