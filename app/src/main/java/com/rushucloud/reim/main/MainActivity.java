package com.rushucloud.reim.main;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import com.mechat.mechatlibrary.MCClient;
import com.mechat.mechatlibrary.MCOnlineConfig;
import com.mechat.mechatlibrary.MCUserConfig;
import com.rushucloud.reim.R;
import com.rushucloud.reim.item.EditItemActivity;
import com.umeng.analytics.MobclickAgent;
import com.umeng.update.UmengUpdateAgent;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import classes.model.User;
import classes.utils.AppPreference;
import classes.utils.DBManager;
import classes.utils.PhoneUtils;
import classes.utils.ReimApplication;
import classes.utils.Utils;
import classes.utils.ViewUtils;
import classes.widget.ReimProgressDialog;
import classes.widget.Spotlight;
import classes.widget.TabItem;
import netUtils.common.HttpConnectionCallback;
import netUtils.common.HttpUtils;
import netUtils.common.NetworkConstant;
import netUtils.common.URLDef;
import netUtils.request.common.CommonRequest;
import netUtils.request.common.EventsRequest;
import netUtils.request.user.FeedbackRequest;
import netUtils.response.common.CommonResponse;
import netUtils.response.common.EventsResponse;
import netUtils.response.common.FeedbackResponse;

public class MainActivity extends FragmentActivity implements OnClickListener
{
    private long exitTime = 0;

    private ViewPager viewPager;
    private ImageView reportTipImageView;
    private ImageView meTipImageView;
    private RelativeLayout reimGuideLayout;
    private PopupWindow feedbackPopupWindow;
    private EditText feedbackEditText;
    private PopupWindow phonePopupWindow;
    private EditText codeEditText;
    private EditText phoneEditText;

    private AppPreference appPreference;
    private DBManager dbManager;
    private WebSocketClient webSocketClient;
    private boolean webSocketIsClosed = true;

    private List<Fragment> fragmentList = new ArrayList<>();
    private List<TabItem> tabItemList = new ArrayList<>();

    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        UmengUpdateAgent.setDefault();
        UmengUpdateAgent.update(this);
        initData();
        initView();
    }

    protected void onResume()
    {
        super.onResume();
        MobclickAgent.onResume(this);
        ReimProgressDialog.setContext(this);

        viewPager.setCurrentItem(ReimApplication.getTabIndex(), false);
        resetTabItems();
        tabItemList.get(ReimApplication.getTabIndex()).setIconAlpha(1);
        fragmentList.get(viewPager.getCurrentItem()).setUserVisibleHint(true);

        initData();
        if (ReimApplication.getTabIndex() == ReimApplication.TAB_REIM)
        {
            dealWithReimGuideLayout();
        }
        else if (ReimApplication.getTabIndex() == ReimApplication.TAB_REPORT)
        {
            dealWithReportGuideLayout();
        }

        if (PhoneUtils.isNetworkConnected())
        {
            sendGetEventsRequest();
            if (webSocketClient == null || webSocketIsClosed)
            {
                connectWebSocket();
            }
        }
    }

    protected void onPause()
    {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_BACK)
        {
            if (System.currentTimeMillis() - exitTime > 2000)
            {
                ViewUtils.showToast(MainActivity.this, R.string.prompt_press_back_to_exit);
                exitTime = System.currentTimeMillis();
            }
            else
            {
                finish();
                dbManager.closeDatabase();
                if (webSocketClient != null && !webSocketIsClosed)
                {
                    webSocketClient.close();
                }
                android.os.Process.killProcess(android.os.Process.myPid());
            }
            return true;
        }
        else
        {
            return super.onKeyDown(keyCode, event);
        }
    }

    private void initData()
    {
        appPreference = AppPreference.getAppPreference();
        dbManager = DBManager.getDBManager();
    }

    private void initView()
    {
        ReimFragment reimFragment = new ReimFragment();
        ReportFragment reportFragment = new ReportFragment();
        StatisticsFragment statisticsFragment = new StatisticsFragment();
        MeFragment meFragment = new MeFragment();

        fragmentList.add(reimFragment);
        fragmentList.add(reportFragment);
        fragmentList.add(statisticsFragment);
        fragmentList.add(meFragment);

        FragmentPagerAdapter adapter = new FragmentPagerAdapter(getSupportFragmentManager())
        {
            public int getCount()
            {
                return fragmentList.size();
            }

            public Fragment getItem(int arg0)
            {
                return fragmentList.get(arg0);
            }

            public void destroyItem(ViewGroup container, int position, Object object)
            {

            }
        };
        viewPager = (ViewPager) findViewById(R.id.viewPager);
        viewPager.setAdapter(adapter);
        viewPager.setOnPageChangeListener(new OnPageChangeListener()
        {
            public void onPageSelected(int arg0)
            {
                ReimApplication.setTabIndex(arg0);
            }

            public void onPageScrolled(int arg0, float arg1, int arg2)
            {
                if (arg1 > 0)
                {
                    TabItem leftItem = tabItemList.get(arg0);
                    TabItem rightItem = tabItemList.get(arg0 + 1);

                    leftItem.setIconAlpha(1 - arg1);
                    rightItem.setIconAlpha(arg1);
                }
            }

            public void onPageScrollStateChanged(int arg0)
            {
                if (arg0 == 2)
                {
                    int currentIndex = viewPager.getCurrentItem();
                    if (currentIndex == ReimApplication.TAB_REIM)
                    {
                        dealWithReimGuideLayout();
                    }
                    else if (currentIndex == ReimApplication.TAB_REPORT)
                    {
                        showReportTip(false);
                        dealWithReportGuideLayout();
                    }
                    else if (currentIndex == ReimApplication.TAB_ME)
                    {
                        showMeTip(false);
                    }
                }
            }
        });

        LinearLayout tabLayout = (LinearLayout) findViewById(R.id.tabLayout);
        tabLayout.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                hideReimGuideLayout();
            }
        });

        TabItem tabItemReim = (TabItem) findViewById(R.id.tabItemReim);
        TabItem tabItemReport = (TabItem) findViewById(R.id.tabItemReport);
        TabItem tabItemStat = (TabItem) findViewById(R.id.tabItemStat);
        TabItem tabItemMe = (TabItem) findViewById(R.id.tabItemMe);

        tabItemReim.setOnClickListener(this);
        tabItemReport.setOnClickListener(this);
        tabItemStat.setOnClickListener(this);
        tabItemMe.setOnClickListener(this);

        tabItemList.add(tabItemReim);
        tabItemList.add(tabItemReport);
        tabItemList.add(tabItemStat);
        tabItemList.add(tabItemMe);

        tabItemReim.setIconAlpha(1);

        reportTipImageView = (ImageView) findViewById(R.id.reportTipImageView);
        meTipImageView = (ImageView) findViewById(R.id.meTipImageView);

        Button addButton = (Button) findViewById(R.id.addButton);
        addButton.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                hideReimGuideLayout();
                Intent intent = new Intent(MainActivity.this, EditItemActivity.class);
                intent.putExtra("fromReim", true);
                ViewUtils.goForward(MainActivity.this, intent);
            }
        });

        ImageView feedbackImageView = (ImageView) findViewById(R.id.feedbackImageView);
        feedbackImageView.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                MobclickAgent.onEvent(MainActivity.this, "UMENG_HELP");

                if (PhoneUtils.isNetworkConnected())
                {
                    startMeChatActivity();
                }
                else
                {
                    ViewUtils.showToast(MainActivity.this, R.string.error_feedback_network_unavailable);
                }
            }
        });

        reimGuideLayout = (RelativeLayout) findViewById(R.id.reimGuideLayout);

        initFeedbackWindow();
        initPhoneWindow();
    }

    private void initFeedbackWindow()
    {
        View feedbackView = View.inflate(this, R.layout.window_feedback, null);

        feedbackEditText = (EditText) feedbackView.findViewById(R.id.feedbackEditText);

        Button submitButton = (Button) feedbackView.findViewById(R.id.submitButton);
        submitButton.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                MobclickAgent.onEvent(MainActivity.this, "UMENG_HELP_SUBMIT");

                if (PhoneUtils.isNetworkConnected())
                {
                    User user = AppPreference.getAppPreference().getCurrentUser();
                    String feedback = feedbackEditText.getText().toString();
                    if (feedback.isEmpty())
                    {
                        ViewUtils.showToast(MainActivity.this, R.string.error_feedback_empty);
                    }
                    else if (user != null && Utils.isPhone(user.getPhone()))
                    {
                        sendFeedbackRequest(feedbackEditText.getText().toString(), user.getPhone());
                    }
                    else
                    {
                        feedbackPopupWindow.dismiss();
                        showPhoneWindow();
                    }
                }
                else
                {
                    ViewUtils.showToast(MainActivity.this, R.string.error_feedback_network_unavailable);
                }
            }
        });

        Button cancelButton = (Button) feedbackView.findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                MobclickAgent.onEvent(MainActivity.this, "UMENG_HELP_CANCEL");
                feedbackPopupWindow.dismiss();
            }
        });

        feedbackPopupWindow = ViewUtils.buildCenterPopupWindow(this, feedbackView);
    }

    private void initPhoneWindow()
    {
        View phoneView = View.inflate(this, R.layout.window_feedback_phone, null);

        codeEditText = (EditText) phoneView.findViewById(R.id.codeEditText);
        phoneEditText = (EditText) phoneView.findViewById(R.id.phoneEditText);

        Button submitButton = (Button) phoneView.findViewById(R.id.submitButton);
        submitButton.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                if (PhoneUtils.isNetworkConnected())
                {
                    String code = codeEditText.getText().toString();
                    String phone = phoneEditText.getText().toString();
                    if (code.isEmpty())
                    {
                        ViewUtils.showToast(MainActivity.this, R.string.error_feedback_code_empty);
                    }
                    else if (phone.isEmpty())
                    {
                        ViewUtils.showToast(MainActivity.this, R.string.error_phone_empty);
                    }
                    else
                    {
                        sendFeedbackRequest(feedbackEditText.getText().toString(), code + "-" + phone);
                    }
                }
                else
                {
                    ViewUtils.showToast(MainActivity.this, R.string.error_feedback_network_unavailable);
                }
            }
        });

        Button skipButton = (Button) phoneView.findViewById(R.id.skipButton);
        skipButton.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                sendFeedbackRequest(feedbackEditText.getText().toString(), "");
            }
        });

        phonePopupWindow = ViewUtils.buildCenterPopupWindow(this, phoneView);
    }

    private void showFeedbackWindow()
    {
        feedbackEditText.setText("");

        feedbackPopupWindow.showAtLocation(findViewById(R.id.containerLayout), Gravity.CENTER, 0, 0);
        feedbackPopupWindow.update();
    }

    private void showPhoneWindow()
    {
        codeEditText.setText(R.string.feedback_code);
        phoneEditText.setText("");

        phonePopupWindow.showAtLocation(findViewById(R.id.containerLayout), Gravity.CENTER, 0, 0);
        phonePopupWindow.update();
    }

    private void startMeChatActivity()
    {
        User currentUser = appPreference.getCurrentUser();

        MCOnlineConfig onlineConfig = new MCOnlineConfig();
        MCClient.getInstance().startMCConversationActivity(onlineConfig);

        MCUserConfig mcUserConfig = new MCUserConfig();
        Map<String,String> userInfo = new HashMap<>();
        if (currentUser != null)
        {
            userInfo.put(MCUserConfig.PersonalAccount.NICK_NAME, currentUser.getNickname());
            userInfo.put(MCUserConfig.Contact.TEL, currentUser.getPhone());
            userInfo.put(MCUserConfig.Contact.EMAIL, currentUser.getEmail());
        }
        Map<String, String> userInfoExtra = new HashMap<>();
        userInfoExtra.put("AndroidVersion",Integer.toString(Build.VERSION.SDK_INT));
        userInfoExtra.put("AppVersion", PhoneUtils.getAppVersion());
        mcUserConfig.setUserInfo(this, userInfo, userInfoExtra, null);
    }

    private void resetTabItems()
    {
        for (int i = 0; i < tabItemList.size(); i++)
        {
            tabItemList.get(i).setIconAlpha(0);
        }
    }

    private void showReportTip(boolean hasUnreadReports)
    {
        int visibility = hasUnreadReports ? View.VISIBLE : View.GONE;
        reportTipImageView.setVisibility(visibility);
    }

    private void showMeTip(boolean hasMessages)
    {
        int visibility = hasMessages ? View.VISIBLE : View.GONE;
        meTipImageView.setVisibility(visibility);
    }

    private void dealWithReimGuideLayout()
    {
        if (appPreference.needToShowReimGuide())
        {
            reimGuideLayout.setVisibility(View.VISIBLE);
            reimGuideLayout.setOnClickListener(new OnClickListener()
            {
                public void onClick(View v)
                {
                    hideReimGuideLayout();
                }
            });
        }
    }

    private void dealWithReportGuideLayout()
    {
        if (appPreference.needToShowReportGuide())
        {
            int width = ViewUtils.getPhoneWindowWidth(this);
            int height = ViewUtils.getPhoneWindowHeight(this);
            float radius = ViewUtils.dpToPixel(21);
            int margin = ViewUtils.dpToPixel(25);
            RelativeLayout spotlightLayout = (RelativeLayout) findViewById(R.id.spotlightLayout);
            spotlightLayout.addView(new Spotlight(this, width, height, width - margin, margin, radius, ViewUtils.getColor(R.color.hint_light_grey)));

            final RelativeLayout reportGuideLayout = (RelativeLayout) findViewById(R.id.reportGuideLayout);
            reportGuideLayout.setVisibility(View.VISIBLE);
            reportGuideLayout.setOnClickListener(new OnClickListener()
            {
                public void onClick(View v)
                {
                    reportGuideLayout.setVisibility(View.GONE);
                    AppPreference appPreference = AppPreference.getAppPreference();
                    appPreference.setNeedToShowReportGuide(false);
                    appPreference.saveAppPreference();
                }
            });
        }
    }

    private void hideReimGuideLayout()
    {
        if (reimGuideLayout.getVisibility() == View.VISIBLE)
        {
            reimGuideLayout.setVisibility(View.GONE);
            AppPreference appPreference = AppPreference.getAppPreference();
            appPreference.setNeedToShowReimGuide(false);
            appPreference.saveAppPreference();
        }
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
                    User currentUser = AppPreference.getAppPreference().getCurrentUser();
                    if (currentUser != null)
                    {
                        currentUser.setAppliedCompany(response.getAppliedCompany());
                        currentUser.setIsActive(response.isCurrentUserActived());
                        DBManager.getDBManager().updateUser(currentUser);
                    }

                    if (response.needToRefresh() && PhoneUtils.isNetworkConnected())
                    {
                        sendCommonRequest();
                    }

                    runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            ReimApplication.setMineUnreadList(response.getMineUnreadList());
                            ReimApplication.setOthersUnreadList(response.getOthersUnreadList());
                            ReimApplication.setUnreadMessagesCount(response.getUnreadMessagesCount());
                            ReimApplication.setHasUnreadMessages(response.hasUnreadMessages());
                            showReportTip(response.hasUnreadReports());
                            showMeTip(response.hasUnreadMessages());

                            if (viewPager.getCurrentItem() == ReimApplication.TAB_ME)
                            {
                                MeFragment fragment = (MeFragment) fragmentList.get(ReimApplication.TAB_ME);
                                fragment.showTip();
                            }
                            else if (viewPager.getCurrentItem() == ReimApplication.TAB_REPORT)
                            {
                                ReportFragment fragment = (ReportFragment) fragmentList.get(ReimApplication.TAB_REPORT);
                                fragment.showBadge();
                            }
                        }
                    });
                }
            }
        });
    }

    private void sendCommonRequest()
    {
        CommonRequest request = new CommonRequest();
        request.sendRequest(new HttpConnectionCallback()
        {
            public void execute(Object httpResponse)
            {
                final CommonResponse response = new CommonResponse(httpResponse);
                if (response.getStatus())
                {
                    int currentGroupID = -1;

                    DBManager dbManager = DBManager.getDBManager();
                    appPreference.setServerToken(response.getServerToken());
                    appPreference.setCurrentUserID(response.getCurrentUser().getServerID());

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

                        dbManager.syncUser(currentUser);

                        // update categories
                        dbManager.updateGroupCategories(response.getCategoryList(), currentGroupID);

                        // update tags
                        dbManager.updateGroupTags(response.getTagList(), currentGroupID);

                        // update group info
                        dbManager.syncGroup(response.getGroup());

                        if (viewPager.getCurrentItem() == ReimApplication.TAB_ME)
                        {
                            runOnUiThread(new Runnable()
                            {
                                public void run()
                                {
                                    MeFragment fragment = (MeFragment) fragmentList.get(ReimApplication.TAB_ME);
                                    fragment.loadProfileView();
                                }
                            });
                        }
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
                }
            }
        });
    }

    private void sendFeedbackRequest(String feedback, String contactInfo)
    {
        ReimProgressDialog.show();
        FeedbackRequest request = new FeedbackRequest(feedback, contactInfo, PhoneUtils.getAppVersion());
        request.sendRequest(new HttpConnectionCallback()
        {
            public void execute(Object httpResponse)
            {
                final FeedbackResponse response = new FeedbackResponse(httpResponse);
                runOnUiThread(new Runnable()
                {
                    public void run()
                    {
                        if (response.getStatus())
                        {
                            ReimProgressDialog.dismiss();
                            feedbackPopupWindow.dismiss();
                            phonePopupWindow.dismiss();
                            ViewUtils.showToast(MainActivity.this, R.string.succeed_in_sending_feedback);
                        }
                        else
                        {
                            ReimProgressDialog.dismiss();
                            ViewUtils.showToast(MainActivity.this, R.string.failed_to_send_feedback, response.getErrorMessage());
                        }
                    }
                });
            }
        });
    }

    private void connectWebSocket()
    {
        try
        {
            URI uri = new URI(URLDef.WEBSOCKET_URI);
            webSocketClient = new WebSocketClient(uri)
            {
                public void onOpen(ServerHandshake handShakeData)
                {
                    System.out.println("onOpen");
                    webSocketIsClosed = false;
                    webSocketClient.send(HttpUtils.getJWTString());
                }

                public void onMessage(String message)
                {
                    System.out.println("onMessage:" + message);

                    try
                    {
                        JSONObject jObject = new JSONObject(message);
                        int type = jObject.getJSONObject("extra").getInt("type");
                        if (type > 0 && type == NetworkConstant.PUSH_TYPE_REPORT)
                        {
                            final boolean myReport = jObject.getJSONObject("extra").getInt("uid") == appPreference.getCurrentUserID();
                            runOnUiThread(new Runnable()
                            {
                                public void run()
                                {
                                    sendGetEventsRequest();
                                    ReportFragment fragment = (ReportFragment) fragmentList.get(ReimApplication.TAB_REPORT);
                                    if (myReport && viewPager.getCurrentItem() == ReimApplication.TAB_REPORT &&
                                            ReimApplication.getReportTabIndex() == ReimApplication.TAB_REPORT_MINE)
                                    {
                                        fragment.syncReports();
                                    }
                                    else if (!myReport && viewPager.getCurrentItem() == ReimApplication.TAB_REPORT &&
                                            ReimApplication.getReportTabIndex() == ReimApplication.TAB_REPORT_OTHERS)
                                    {
                                        appPreference.setLastGetOthersReportTime(0);
                                        appPreference.saveAppPreference();
                                        fragment.setListView(ReimApplication.TAB_REPORT_OTHERS, false);
                                    }
                                    showReportTip(true);
                                }
                            });
                        }
                        else if (type > 0)
                        {
                            runOnUiThread(new Runnable()
                            {
                                public void run()
                                {
                                    ReimApplication.setHasUnreadMessages(true);
                                    showMeTip(true);

                                    if (viewPager.getCurrentItem() == ReimApplication.TAB_ME)
                                    {
                                        runOnUiThread(new Runnable()
                                        {
                                            public void run()
                                            {
                                                MeFragment fragment = (MeFragment) fragmentList.get(ReimApplication.TAB_ME);
                                                fragment.showTip();
                                            }
                                        });
                                    }
                                }
                            });
                        }
                    }
                    catch (JSONException e)
                    {
                        e.printStackTrace();
                    }
                }

                public void onClose(int code, String reason, boolean remote)
                {
                    webSocketIsClosed = true;
                }

                public void onError(Exception ex)
                {
                    System.out.println("onError:" + ex.getLocalizedMessage());
                    webSocketIsClosed = true;
                }
            };
            webSocketClient.connect();
        }
        catch (Exception e)
        {
            System.out.println(e.getLocalizedMessage());
            e.printStackTrace();
        }
    }

    public void onClick(View v)
    {
        resetTabItems();
        hideReimGuideLayout();

        int position = 0;
        switch (v.getId())
        {
            case R.id.tabItemReim:
            {
                MobclickAgent.onEvent(MainActivity.this, "UMENG_ITEM");
                position = ReimApplication.TAB_REIM;
                dealWithReimGuideLayout();
                break;
            }
            case R.id.tabItemReport:
            {
                MobclickAgent.onEvent(MainActivity.this, "UMENG_REPORT");

                position = ReimApplication.TAB_REPORT;
                showReportTip(false);
                dealWithReportGuideLayout();
                break;
            }
            case R.id.tabItemStat:
            {
                position = ReimApplication.TAB_STATISTICS;
                break;
            }
            case R.id.tabItemMe:
            {
                position = ReimApplication.TAB_ME;
                showMeTip(false);
                break;
            }
            default:
                break;
        }

        ReimApplication.setTabIndex(position);
        viewPager.setCurrentItem(position, false);
        tabItemList.get(position).setIconAlpha(1.0f);
    }
}