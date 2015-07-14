package com.rushucloud.reim.report;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.rushucloud.reim.R;
import com.umeng.analytics.MobclickAgent;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import classes.model.Report;
import classes.model.User;
import classes.utils.AppPreference;
import classes.utils.Constant;
import classes.utils.DBManager;
import classes.utils.ReimApplication;
import classes.utils.Utils;
import classes.utils.ViewUtils;
import classes.widget.ReimProgressDialog;
import netUtils.common.HttpConnectionCallback;
import netUtils.request.report.ApproveReportRequest;
import netUtils.response.report.ApproveReportResponse;

public class FollowingActivity extends Activity
{
    // Widgets
    private TextView managerTextView;
    private TextView ccTextView;

    // Local Data
    private Report report;
    private List<User> managerList = new ArrayList<>();
    private List<User> ccList = new ArrayList<>();
    private boolean canBeFinished;

    // View
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_following);
        initData();
        initView();
    }

    protected void onResume()
    {
        super.onResume();
        MobclickAgent.onPageStart("FollowingActivity");
        MobclickAgent.onResume(this);
        ReimProgressDialog.setContext(this);
    }

    protected void onPause()
    {
        super.onPause();
        MobclickAgent.onPageEnd("FollowingActivity");
        MobclickAgent.onPause(this);
    }

    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_BACK)
        {
            goBackToApproveReportActivity();
        }
        return super.onKeyDown(keyCode, event);
    }

    @SuppressWarnings("unchecked")
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (resultCode == RESULT_OK)
        {
            switch (requestCode)
            {
                case Constant.ACTIVITY_PICK_MANAGER:
                {
                    List<User> managerList = (List<User>) data.getSerializableExtra("managers");
                    report.setManagerList(managerList);
                    managerTextView.setText(report.getManagersName());
                    break;
                }
                case Constant.ACTIVITY_PICK_CC:
                {
                    List<User> ccList = (List<User>) data.getSerializableExtra("ccs");
                    report.setCCList(ccList);
                    ccTextView.setText(report.getCCsName());
                    break;
                }
                default:
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void initView()
    {
        ImageView backImageView = (ImageView) findViewById(R.id.backImageView);
        backImageView.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                goBackToApproveReportActivity();
            }
        });

        managerTextView = (TextView) findViewById(R.id.managerTextView);
        managerTextView.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                MobclickAgent.onEvent(FollowingActivity.this, "UMENG_REPORT_NEXT_SEND");
                Intent intent = new Intent(FollowingActivity.this, PickManagerActivity.class);
                intent.putExtra("managers", (Serializable) report.getManagerList());
                intent.putExtra("sender", report.getSender().getServerID());
                intent.putExtra("fromFollowing", true);
                ViewUtils.goForwardForResult(FollowingActivity.this, intent, Constant.ACTIVITY_PICK_MANAGER);
            }
        });
        managerTextView.setText(report.getManagersName());

        ccTextView = (TextView) findViewById(R.id.ccTextView);
        ccTextView.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                MobclickAgent.onEvent(FollowingActivity.this, "UMENG_REPORT_NEXT_CC");
                Intent intent = new Intent(FollowingActivity.this, PickCCActivity.class);
                intent.putExtra("ccs", (Serializable) report.getCCList());
                intent.putExtra("sender", report.getSender().getServerID());
                intent.putExtra("fromFollowing", true);
                ViewUtils.goForwardForResult(FollowingActivity.this, intent, Constant.ACTIVITY_PICK_CC);
            }
        });

        Button finishButton = (Button) findViewById(R.id.finishButton);
        finishButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                sendApproveReportRequest(true);
            }
        });

        Button submitButton = (Button) findViewById(R.id.submitButton);
        submitButton.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                sendApproveReportRequest(false);
            }
        });

        if (!canBeFinished)
        {
            finishButton.setVisibility(View.GONE);
        }
    }

    private void goBackToApproveReportActivity()
    {
        report.setManagerList(managerList);
        report.setCCList(ccList);

        Bundle bundle = new Bundle();
        bundle.putSerializable("report", report);
        Intent intent = new Intent(FollowingActivity.this, ApproveReportActivity.class);
        intent.putExtras(bundle);
        ViewUtils.goBackWithIntent(this, intent);
    }

    private void goBackToMainActivity()
    {
        ReimApplication.setTabIndex(Constant.TAB_REPORT);
        ReimApplication.setReportTabIndex(Constant.TAB_REPORT_OTHERS);
        ViewUtils.goBack(this);
    }

    // Data
    private void initData()
    {
        report = (Report) getIntent().getSerializableExtra("report");
        canBeFinished = getIntent().getBooleanExtra("canBeFinished", true);
        if (!canBeFinished)
        {
            ArrayList<Integer> managerIDList = getIntent().getIntegerArrayListExtra("managerIDList");
            managerList.addAll(DBManager.getDBManager().getUsers(managerIDList));
        }

        if (managerList.isEmpty())
        {
            User currentUser = AppPreference.getAppPreference().getCurrentUser();
            if (report.getSender().getServerID() != currentUser.getDefaultManagerID())
            {
                managerList.addAll(currentUser.buildBaseManagerList());
            }
        }

        report.setManagerList(managerList);
    }

    // Network
    private void sendApproveReportRequest(boolean isFinished)
    {
        ReimProgressDialog.show();
        report.setMyDecision(Report.STATUS_APPROVED);

        ApproveReportRequest request = new ApproveReportRequest(report, isFinished);
        request.sendRequest(new HttpConnectionCallback()
        {
            public void execute(Object httpResponse)
            {
                final ApproveReportResponse response = new ApproveReportResponse(httpResponse);
                if (response.getStatus())
                {
                    int currentTime = Utils.getCurrentTime();
                    report.setManagerList(managerList);
                    report.setCCList(ccList);
                    report.setLocalUpdatedDate(currentTime);
                    report.setServerUpdatedDate(currentTime);
                    report.setStatus(response.getReportStatus());
                    DBManager.getDBManager().updateOthersReport(report);

                    runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            ReimProgressDialog.dismiss();
                            ViewUtils.showToast(FollowingActivity.this, R.string.prompt_report_approved);
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
                            ViewUtils.showToast(FollowingActivity.this, R.string.error_operation_failed, response.getErrorMessage());
                        }
                    });
                }
            }
        });
    }
}