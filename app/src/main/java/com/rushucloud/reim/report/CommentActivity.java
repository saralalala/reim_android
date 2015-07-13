package com.rushucloud.reim.report;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.rushucloud.reim.R;
import com.rushucloud.reim.main.MainActivity;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.List;

import classes.adapter.CommentListViewAdapter;
import classes.model.Comment;
import classes.model.Image;
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
import netUtils.request.common.DownloadImageRequest;
import netUtils.request.group.GetGroupRequest;
import netUtils.request.report.GetReportRequest;
import netUtils.request.report.ModifyReportRequest;
import netUtils.response.common.DownloadImageResponse;
import netUtils.response.group.GetGroupResponse;
import netUtils.response.report.GetReportResponse;
import netUtils.response.report.ModifyReportResponse;

public class CommentActivity extends Activity
{
    // Widgets
    private TextView commentTextView;
    private ListView commentListView;
    private CommentListViewAdapter adapter;
    private EditText commentEditText;

    // Local Data
    private DBManager dbManager;
    private Report report;
    private List<Comment> commentList = new ArrayList<>();
    private boolean myReport;
    private boolean newReport;
    private boolean fromPush;
    private int pushType;

    // View
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_comment);
        initData();
        initView();
    }

    protected void onResume()
    {
        super.onResume();
        MobclickAgent.onPageStart("CommentActivity");
        MobclickAgent.onResume(this);
        ReimProgressDialog.setContext(this);
        if (fromPush)
        {
            sendGetReportRequest(report.getServerID());
        }
        else
        {
            refreshView();
        }
    }

    protected void onPause()
    {
        super.onPause();
        MobclickAgent.onPageEnd("CommentActivity");
        MobclickAgent.onPause(this);
    }

    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_BACK)
        {
            goBack();
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
                if (myReport)
                {
                    MobclickAgent.onEvent(CommentActivity.this, "UMENG_REPORT_MINE_COMMENT_CLOSE");
                    String event = newReport ? "UMENG_REPORT_NEW_COMMENT_CANCEL" : "UMENG_REPORT_EDIT_COMMENT_CANCEL";
                    MobclickAgent.onEvent(CommentActivity.this, event);
                }
                else
                {
                    MobclickAgent.onEvent(CommentActivity.this, "UMENG_REPORT_OTHER_COMMENT_CLOSE");
                }
                goBack();
            }
        });

        commentTextView = (TextView) findViewById(R.id.commentTextView);

        adapter = new CommentListViewAdapter(this, commentList);
        commentListView = (ListView) findViewById(R.id.commentListView);
        commentListView.setAdapter(adapter);

        commentEditText = (EditText) findViewById(R.id.commentEditText);
        commentEditText.setOnFocusChangeListener(ViewUtils.onFocusChangeListener);

        TextView sendTextView = (TextView) findViewById(R.id.sendTextView);
        sendTextView.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                if (myReport)
                {
                    MobclickAgent.onEvent(CommentActivity.this, "UMENG_REPORT_MINE_COMMENT_SEND");
                    String event = newReport ? "UMENG_REPORT_NEW_COMMENT_SUBMIT" : "UMENG_REPORT_EDIT_COMMENT_SUBMIT";
                    MobclickAgent.onEvent(CommentActivity.this, event);
                }
                else
                {
                    MobclickAgent.onEvent(CommentActivity.this, "UMENG_REPORT_OTHER_COMMENT_SEND");
                }

                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(commentEditText.getWindowToken(), 0);

                String comment = commentEditText.getText().toString();
                if (!PhoneUtils.isNetworkConnected())
                {
                    ViewUtils.showToast(CommentActivity.this, R.string.error_comment_network_unavailable);
                }
                else if (comment.isEmpty())
                {
                    ViewUtils.showToast(CommentActivity.this, R.string.error_comment_empty);
                }
                else
                {
                    sendCommentRequest(comment);
                }
            }
        });
    }

    private void refreshView()
    {
        if (commentList == null || commentList.isEmpty())
        {
            commentListView.setVisibility(View.GONE);
            commentTextView.setVisibility(View.VISIBLE);
        }
        else
        {
            commentListView.setVisibility(View.VISIBLE);
            commentTextView.setVisibility(View.GONE);

            adapter.setComments(commentList);
            adapter.notifyDataSetChanged();

            if (PhoneUtils.isNetworkConnected())
            {
                for (Comment comment : commentList)
                {
                    User user = comment.getReviewer();
                    if (user != null && user.hasUndownloadedAvatar())
                    {
                        sendDownloadAvatarRequest(comment, user);
                    }
                }
            }
        }
    }

    private void goBack()
    {
        if (fromPush)
        {
            Bundle bundle = new Bundle();
            bundle.putSerializable("report", report);
            bundle.putBoolean("fromPush", fromPush);
            bundle.putBoolean("myReport", myReport);

            Intent intent = new Intent();
            intent.putExtras(bundle);

            if (pushType == NetworkConstant.PUSH_REPORT_TYPE_MINE_REJECTED_ONLY_COMMENT)
            {
                intent.setClass(this, EditReportActivity.class);
            }
            else if (pushType == NetworkConstant.PUSH_REPORT_TYPE_OTHERS_APPROVED_ONLY_COMMENT)
            {
                intent.setClass(this, ApproveReportActivity.class);
            }
            else
            {
                intent.setClass(this, ShowReportActivity.class);
            }

            ViewUtils.goBackWithIntent(this, intent);
        }
        else
        {
            ViewUtils.goBack(this);
        }
    }

    private void goBackToMainActivity()
    {
        int reportTabIndex = myReport ? Constant.TAB_REPORT_MINE : Constant.TAB_REPORT_OTHERS;
        ReimApplication.setTabIndex(Constant.TAB_REPORT);
        ReimApplication.setReportTabIndex(reportTabIndex);
        Intent intent = new Intent(CommentActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        ViewUtils.goBackWithIntent(this, intent);
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
            newReport = bundle.getBoolean("newReport", false);
            pushType = bundle.getInt("pushType");

            if (myReport)
            {
                commentList = dbManager.getReportComments(report.getLocalID());
            }
            else
            {
                commentList = dbManager.getOthersReportComments(report.getServerID());
            }

            if (commentList != null && !commentList.isEmpty())
            {
                Comment.sortByCreateDate(commentList);
            }
        }
    }

    private void updateReport(Report responseReport)
    {
        report = new Report(responseReport);

        for (Comment comment : report.getCommentList())
        {
            User user = dbManager.getUser(comment.getReviewer().getServerID());
            comment.setReviewer(user);
        }

        commentList.clear();
        commentList.addAll(report.getCommentList());

        if (myReport)
        {
            Report localReport = dbManager.getReportByServerID(report.getServerID());
            if (localReport != null)
            {
                int reportLocalID = localReport.getLocalID();
                dbManager.deleteReportComments(reportLocalID);
                for (Comment comment : report.getCommentList())
                {
                    comment.setReportID(reportLocalID);
                    dbManager.insertComment(comment);
                }
            }
        }
        else
        {
            dbManager.deleteOthersReportComments(report.getServerID());
            for (Comment comment : report.getCommentList())
            {
                comment.setReportID(report.getServerID());
                dbManager.insertOthersComment(comment);
            }
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
                        updateReport(response.getReport());
                    }
                    else
                    {
                        Report report = response.getReport();
                        report.setManagerList(response.getManagerList());
                        report.setCCList(response.getCCList());
                        sendGetGroupRequest(response.getReport());
                    }

                    runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            if (!response.containsUnsyncedUser())
                            {
                                ReimProgressDialog.dismiss();
                                refreshView();
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
                            ViewUtils.showToast(CommentActivity.this, R.string.failed_to_get_data, response.getErrorMessage());
                        }
                    });
                }
            }
        });
    }

    private void sendDownloadAvatarRequest(final Comment comment, final User user)
    {
        DownloadImageRequest request = new DownloadImageRequest(user.getAvatarServerPath());
        request.sendRequest(new HttpConnectionCallback()
        {
            public void execute(Object httpResponse)
            {
                DownloadImageResponse response = new DownloadImageResponse(httpResponse);
                if (response.getBitmap() != null)
                {
                    String avatarPath = PhoneUtils.saveOriginalBitmapToFile(response.getBitmap(), Image.TYPE_AVATAR, user.getAvatarID());
                    user.setAvatarLocalPath(avatarPath);
                    user.setLocalUpdatedDate(Utils.getCurrentTime());
                    user.setServerUpdatedDate(user.getLocalUpdatedDate());
                    dbManager.updateUser(user);

                    comment.setReviewer(user);

                    runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            adapter.setComments(commentList);
                            adapter.notifyDataSetChanged();
                        }
                    });
                }
            }
        });
    }

    private void sendCommentRequest(final String commentContent)
    {
        ReimProgressDialog.show();

        ModifyReportRequest request = new ModifyReportRequest(report, commentContent);
        request.sendRequest(new HttpConnectionCallback()
        {
            public void execute(Object httpResponse)
            {
                final ModifyReportResponse response = new ModifyReportResponse(httpResponse);
                if (response.getStatus())
                {
                    User user = AppPreference.getAppPreference().getCurrentUser();
                    int currentTime = Utils.getCurrentTime();

                    Comment comment = new Comment();
                    comment.setContent(commentContent);
                    comment.setCreatedDate(currentTime);
                    comment.setLocalUpdatedDate(currentTime);
                    comment.setServerUpdatedDate(currentTime);
                    comment.setReviewer(user);

                    if (myReport)
                    {
                        comment.setReportID(report.getLocalID());
                        dbManager.insertComment(comment);
                    }
                    else
                    {
                        comment.setReportID(report.getServerID());
                        dbManager.insertOthersComment(comment);
                    }

                    commentList.add(comment);
                    Comment.sortByCreateDate(commentList);

                    runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            ReimProgressDialog.dismiss();
                            ViewUtils.showToast(CommentActivity.this, R.string.succeed_in_sending_comment);
                            commentEditText.setText("");
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
                            ViewUtils.showToast(CommentActivity.this, R.string.failed_to_send_comment, response.getErrorMessage());
                        }
                    });
                }
            }
        });
    }

    private void sendGetGroupRequest(final Report responseReport)
    {
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
                        }
                    }

                    dbManager.updateGroupUsers(memberList, currentGroupID);

                    // update group info
                    dbManager.syncGroup(response.getGroup());

                    // update report
                    updateReport(responseReport);

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
                            ViewUtils.showToast(CommentActivity.this, R.string.failed_to_get_data, response.getErrorMessage());
                            goBackToMainActivity();
                        }
                    });
                }
            }
        });
    }
}