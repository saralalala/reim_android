package netUtils.request.user;

import netUtils.common.HttpConnectionCallback;
import netUtils.common.URLDef;
import netUtils.request.common.BaseRequest;

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