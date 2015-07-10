package com.rushucloud.reim.start;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.rushucloud.reim.R;
import com.rushucloud.reim.guide.GuideStartActivity;
import com.rushucloud.reim.main.MainActivity;
import com.umeng.analytics.MobclickAgent;

import classes.model.User;
import classes.utils.AppPreference;
import classes.utils.DBManager;
import classes.utils.ReimApplication;
import classes.utils.ViewUtils;
import classes.utils.WeChatUtils;
import classes.widget.ReimProgressDialog;
import netUtils.common.HttpConnectionCallback;
import netUtils.request.user.SandboxOAuthRequest;
import netUtils.response.user.SandboxOAuthResponse;

public class WelcomeActivity extends Activity
{
    // Local Data
    private AppPreference appPreference;
    private long exitTime = 0;

    // View
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_welcome);
        appPreference = AppPreference.getAppPreference();
        initView();
    }

    protected void onResume()
    {
        super.onResume();
        MobclickAgent.onPageStart("WelcomeActivity");
        MobclickAgent.onResume(this);
        ReimProgressDialog.setContext(this);
    }

    protected void onPause()
    {
        super.onPause();
        MobclickAgent.onPageEnd("WelcomeActivity");
        MobclickAgent.onPause(this);
    }

    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_BACK)
        {
            if (System.currentTimeMillis() - exitTime > 2000)
            {
                ViewUtils.showToast(WelcomeActivity.this, R.string.prompt_press_back_to_exit);
                exitTime = System.currentTimeMillis();
            }
            else
            {
                finish();
                DBManager dbManager = DBManager.getDBManager();
                dbManager.close();
                android.os.Process.killProcess(android.os.Process.myPid());
            }
            return true;
        }
        else
        {
            return super.onKeyDown(keyCode, event);
        }
    }

    private void initView()
    {
        Button signUpButton = (Button) findViewById(R.id.signUpButton);
        signUpButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                ViewUtils.goForwardAndFinish(WelcomeActivity.this, PhoneSignUpActivity.class);
            }
        });

        Button signInButton = (Button) findViewById(R.id.signInButton);
        signInButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                ViewUtils.goForwardAndFinish(WelcomeActivity.this, SignInActivity.class);
            }
        });

        TextView experienceTextView = (TextView) findViewById(R.id.experienceTextView);
        experienceTextView.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View view)
            {
                appPreference.setSandboxMode(true);
                appPreference.saveAppPreference();
                sendSandboxOAuthRequest();
            }
        });

        ImageView wechatImageView = (ImageView) findViewById(R.id.wechatImageView);
        wechatImageView.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                WeChatUtils.sendAuthRequest(WelcomeActivity.this, null);
            }
        });
    }

    // Network
    private void sendSandboxOAuthRequest()
    {
        ReimProgressDialog.show();
        SandboxOAuthRequest request = new SandboxOAuthRequest();
        request.sendRequest(new HttpConnectionCallback()
        {
            public void execute(Object httpResponse)
            {
                final SandboxOAuthResponse response = new SandboxOAuthResponse(httpResponse);
                if (response.getStatus())
                {
                    int currentGroupID = -1;

                    DBManager dbManager = DBManager.getDBManager();
                    final AppPreference appPreference = AppPreference.getAppPreference();
                    appPreference.setUsername(response.getOpenID());
                    appPreference.setServerToken(response.getServerToken());
                    appPreference.setCurrentUserID(response.getCurrentUser().getServerID());
                    appPreference.setProxyUserID(-1);
                    appPreference.setLastShownGuideVersion(response.getLastShownGuideVersion());
                    appPreference.setSyncOnlyWithWifi(true);
                    appPreference.setEnablePasswordProtection(true);
                    appPreference.setLastSyncTime(0);
                    appPreference.setLastGetOthersReportTime(0);
                    appPreference.setLastGetMineStatTime(0);
                    appPreference.setLastGetOthersStatTime(0);

                    if (response.getGroup() != null)
                    {
                        currentGroupID = response.getGroup().getServerID();

                        // update AppPreference
                        appPreference.setCurrentGroupID(currentGroupID);
                        appPreference.saveAppPreference();

                        // update members
                        User currentUser = response.getCurrentUser();
                        User localUser = dbManager.getUser(currentUser.getServerID());
                        if (localUser != null && currentUser.getAvatarID() == localUser.getAvatarID())
                        {
                            currentUser.setAvatarLocalPath(localUser.getAvatarLocalPath());
                        }

                        dbManager.updateGroupUsers(response.getMemberList(), currentGroupID);

                        dbManager.updateUser(currentUser);

                        // update set of books
                        dbManager.updateUserSetOfBooks(response.getSetOfBookList(), appPreference.getCurrentUserID());

                        // update categories
                        dbManager.updateGroupCategories(response.getCategoryList(), currentGroupID);

                        // update tags
                        dbManager.updateGroupTags(response.getTagList(), currentGroupID);

                        // update group info
                        dbManager.syncGroup(response.getGroup());
                    }
                    else
                    {
                        // update AppPreference
                        appPreference.setCurrentGroupID(currentGroupID);
                        appPreference.saveAppPreference();

                        // update current user
                        dbManager.syncUser(response.getCurrentUser());

                        // update set of books
                        dbManager.updateUserSetOfBooks(response.getSetOfBookList(), appPreference.getCurrentUserID());

                        // update categories
                        dbManager.updateGroupCategories(response.getCategoryList(), currentGroupID);
                    }

                    runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            ReimProgressDialog.dismiss();
                            if (appPreference.getLastShownGuideVersion() < ReimApplication.GUIDE_VERSION)
                            {
                                ViewUtils.goForwardAndFinish(WelcomeActivity.this, GuideStartActivity.class);
                            }
                            else
                            {
                                ViewUtils.goForwardAndFinish(WelcomeActivity.this, MainActivity.class);
                            }
                        }
                    });
                }
                else
                {
                    appPreference.setSandboxMode(false);
                    appPreference.saveAppPreference();

                    runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            ReimProgressDialog.dismiss();
                            ViewUtils.showToast(WelcomeActivity.this, R.string.failed_to_experience, response.getErrorMessage());
                        }
                    });
                }
            }
        });
    }
}