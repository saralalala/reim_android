package classes.utils;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.rushucloud.reim.MainActivity;
import com.rushucloud.reim.R;
import com.rushucloud.reim.guide.GuideStartActivity;
import com.tencent.mm.sdk.modelmsg.SendAuth;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.modelmsg.WXWebpageObject;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import classes.model.User;
import classes.widget.ReimProgressDialog;
import netUtils.HttpConnectionCallback;
import netUtils.request.user.WeChatAccessTokenRequest;
import netUtils.request.user.WeChatOAuthRequest;
import netUtils.response.user.WeChatAccessTokenResponse;
import netUtils.response.user.WeChatOAuthResponse;

public class WeChatUtils
{
    public static final String APP_ID = "wx0900af80a9517d1f";
    public static final String APP_SECRET = "268b6cb859b7ee49e643425401c17655";

    private static IWXAPI api;
    private static Activity activity;

    public static IWXAPI getApi()
    {
        return api;
    }

    public static void regToWX()
    {
        api = WXAPIFactory.createWXAPI(ReimApplication.getContext(), APP_ID, true);
        api.registerApp(APP_ID);
    }

    public static void shareToWX(String url, String title, String description, int resID)
    {
        Bitmap bitmap = BitmapFactory.decodeResource(ReimApplication.getContext().getResources(), resID);

        WXWebpageObject webpage = new WXWebpageObject(url);

        WXMediaMessage message = new WXMediaMessage(webpage);
        message.title = title;
        message.description = description;
        message.setThumbImage(bitmap);

        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = String.valueOf(System.currentTimeMillis());
        req.message = message;

        api.sendReq(req);
    }

    public static void sendAuthRequest(Activity source)
    {
        if (api == null)
        {
            regToWX();
        }

        if (!api.isWXAppInstalled() || !api.isWXAppSupportAPI())
        {
            ViewUtils.showToast(activity, R.string.error_wechat_not_supported);
            return;
        }

        activity = source;
        ReimProgressDialog.setContext(activity);
        SendAuth.Req req = new SendAuth.Req();
        req.scope = "snsapi_userinfo";
        req.state = "reim_wechat_sign_in";
        api.sendReq(req);
    }

    public static void sendAccessTokenRequest(String code)
    {
        ReimProgressDialog.show();
        WeChatAccessTokenRequest request = new WeChatAccessTokenRequest(code);
        request.sendRequest(new HttpConnectionCallback()
        {
            public void execute(Object httpResponse)
            {
                WeChatAccessTokenResponse response = new WeChatAccessTokenResponse(httpResponse);
                if (response.getStatus())
                {
                    sendWeChatOAuthRequest(response.getAccessToken(), response.getOpenID(), response.getUnionID());
                }
                else
                {
                    activity.runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            ReimProgressDialog.dismiss();
                            ViewUtils.showToast(activity, R.string.error_wechat_auth);
                        }
                    });
                }
            }
        });
    }

    public static void sendWeChatOAuthRequest(String accessToken, String openID, final String unionID)
    {
        WeChatOAuthRequest request = new WeChatOAuthRequest(accessToken, openID, unionID);
        request.sendRequest(new HttpConnectionCallback()
        {
            public void execute(Object httpResponse)
            {
                final WeChatOAuthResponse response = new WeChatOAuthResponse(httpResponse);
                if (response.getStatus())
                {
                    int currentGroupID = -1;

                    DBManager dbManager = DBManager.getDBManager();
                    final AppPreference appPreference = AppPreference.getAppPreference();
                    appPreference.setServerToken(response.getServerToken());
                    appPreference.setUsername(unionID);
                    appPreference.setHasPassword(response.hasPassword());
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

                        dbManager.syncUser(currentUser);

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

                    // refresh UI
                    activity.runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            ReimProgressDialog.dismiss();
                            if (appPreference.getLastShownGuideVersion() < ReimApplication.GUIDE_VERSION)
                            {
                                ViewUtils.goForwardAndFinish(activity, GuideStartActivity.class);
                            }
                            else
                            {
                                ViewUtils.goForwardAndFinish(activity, MainActivity.class);
                            }
                        }
                    });
                }
                else
                {
                    AppPreference appPreference = AppPreference.getAppPreference();
                    appPreference.setUsername("");
                    appPreference.setPassword("");
                    appPreference.saveAppPreference();

                    activity.runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            ReimProgressDialog.dismiss();
                            ViewUtils.showToast(activity, R.string.failed_to_sign_in, response.getErrorMessage());
                        }
                    });
                }
            }
        });
    }
}