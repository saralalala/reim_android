package com.rushucloud.reim.start;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.rushucloud.reim.R;
import com.rushucloud.reim.guide.GuideStartActivity;
import com.rushucloud.reim.main.MainActivity;
import com.umeng.analytics.MobclickAgent;

import classes.model.User;
import classes.utils.AppPreference;
import classes.utils.DBManager;
import classes.utils.PhoneUtils;
import classes.utils.ReimApplication;
import classes.utils.ViewUtils;
import netUtils.common.HttpConnectionCallback;
import netUtils.request.user.SandboxOAuthRequest;
import netUtils.request.user.SignInRequest;
import netUtils.response.user.SandboxOAuthResponse;
import netUtils.response.user.SignInResponse;

public class SplashActivity extends Activity
{
    // Local Data
    private AppPreference appPreference;

    // View
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_splash);
        appPreference = AppPreference.getAppPreference();
        start();
    }

    protected void onResume()
    {
        super.onResume();
        MobclickAgent.onPageStart("SplashActivity");
        MobclickAgent.onResume(this);
    }

    protected void onPause()
    {
        super.onPause();
        MobclickAgent.onPageEnd("SplashActivity");
        MobclickAgent.onPause(this);
    }

    private void start()
    {
        if (appPreference.getUsername().isEmpty())
        {
            Thread splashThread = new Thread()
            {
                public void run()
                {
                    try
                    {
                        int waitingTime = 0;
                        int splashTime = 2000;
                        while (waitingTime < splashTime)
                        {
                            sleep(100);
                            waitingTime += 100;
                        }
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                    finally
                    {
                        ViewUtils.goForwardAndFinish(SplashActivity.this, WelcomeActivity.class);
                    }
                }
            };
            splashThread.start();
        }
        else
        {
            if (PhoneUtils.isNetworkConnected())
            {
                if (appPreference.isSandboxMode())
                {
                    sendSandboxOAuthRequest();
                }
                else
                {
                    sendSignInRequest();
                }
            }
            else
            {
                Thread splashThread = new Thread()
                {
                    public void run()
                    {
                        try
                        {
                            int waitingTime = 0;
                            int splashTime = 2000;
                            while (waitingTime < splashTime)
                            {
                                sleep(100);
                                waitingTime += 100;
                            }
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                        finally
                        {
                            ViewUtils.goForwardAndFinish(SplashActivity.this, MainActivity.class);
                        }
                    }
                };
                splashThread.start();
            }
        }
    }

    // Network
    private void sendSignInRequest()
    {
        SignInRequest request = new SignInRequest();
        request.sendRequest(new HttpConnectionCallback()
        {
            public void execute(Object httpResponse)
            {
                final SignInResponse response = new SignInResponse(httpResponse);
                if (response.getStatus())
                {
                    int currentGroupID = -1;

                    DBManager dbManager = DBManager.getDBManager();
                    final AppPreference appPreference = AppPreference.getAppPreference();
                    appPreference.setServerToken(response.getServerToken());
                    appPreference.setCurrentUserID(response.getCurrentUser().getServerID());
                    appPreference.setLastShownGuideVersion(response.getLastShownGuideVersion());
                    appPreference.setSyncOnlyWithWifi(true);
                    appPreference.setEnablePasswordProtection(true);

                    if (response.getGroup() != null)
                    {
                        currentGroupID = response.getGroup().getServerID();

                        // update AppPreference
                        appPreference.setCurrentGroupID(currentGroupID);
                        appPreference.saveAppPreference();

                        // update members
                        User currentUser = response.getCurrentUser();
                        User localUser = dbManager.getUser(response.getCurrentUser().getServerID());
                        if (localUser != null && currentUser.getAvatarID() == localUser.getAvatarID())
                        {
                            currentUser.setAvatarLocalPath(localUser.getAvatarLocalPath());
                        }

                        dbManager.updateGroupUsers(response.getMemberList(), currentGroupID);

                        dbManager.updateUser(currentUser);

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

                        // update categories
                        dbManager.updateGroupCategories(response.getCategoryList(), currentGroupID);
                    }

                    runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            if (appPreference.getLastShownGuideVersion() < ReimApplication.GUIDE_VERSION)
                            {
                                ViewUtils.goForwardAndFinish(SplashActivity.this, GuideStartActivity.class);
                            }
                            else
                            {
                                ViewUtils.goForwardAndFinish(SplashActivity.this, MainActivity.class);
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
                            ViewUtils.showToast(SplashActivity.this, R.string.failed_to_sign_in, response.getErrorMessage());
                            Intent intent = new Intent(SplashActivity.this, SignInActivity.class);
                            intent.putExtra("username", AppPreference.getAppPreference().getUsername());
                            intent.putExtra("password", AppPreference.getAppPreference().getPassword());
                            ViewUtils.goForwardAndFinish(SplashActivity.this, intent);
                        }
                    });
                }
            }
        });
    }

    private void sendSandboxOAuthRequest()
    {
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

                        // update categories
                        dbManager.updateGroupCategories(response.getCategoryList(), currentGroupID);
                    }

                    runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            if (appPreference.getLastShownGuideVersion() < ReimApplication.GUIDE_VERSION)
                            {
                                ViewUtils.goForwardAndFinish(SplashActivity.this, GuideStartActivity.class);
                            }
                            else
                            {
                                ViewUtils.goForwardAndFinish(SplashActivity.this, MainActivity.class);
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
                            ViewUtils.showToast(SplashActivity.this, R.string.failed_to_experience, response.getErrorMessage());
                            ViewUtils.goForwardAndFinish(SplashActivity.this, SignInActivity.class);
                        }
                    });
                }
            }
        });
    }
}