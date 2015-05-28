package netUtils.request.user;

import netUtils.HttpConnectionCallback;
import netUtils.URLDef;
import netUtils.request.BaseRequest;

public class WeChatOAuthRequest extends BaseRequest
{
    public WeChatOAuthRequest(String accessToken, String openID, String unionID)
    {
        super();

        addParams("token", accessToken);
        addParams("openid", openID);
        addParams("unionid", unionID);

        appendUrl(URLDef.URL_OAUTH);
    }

    public void sendRequest(HttpConnectionCallback callback)
    {
        doPost(callback);
    }
}