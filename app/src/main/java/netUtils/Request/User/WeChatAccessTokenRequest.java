package netUtils.request.user;

import classes.utils.WeChatUtils;
import netUtils.HttpConnectionCallback;
import netUtils.URLDef;
import netUtils.request.BaseRequest;

public class WeChatAccessTokenRequest extends BaseRequest
{
    public WeChatAccessTokenRequest(String code)
    {
        addParams("appid", WeChatUtils.APP_ID);
        addParams("secret", WeChatUtils.APP_SECRET);
        addParams("code", code);
        addParams("grant_type", "authorization_code");

        setUrl(URLDef.URL_WECHAT);
    }

    public void sendRequest(HttpConnectionCallback callback)
    {
        doGet(callback);
    }
}