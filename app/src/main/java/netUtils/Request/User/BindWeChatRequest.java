package netUtils.request.user;

import classes.utils.AppPreference;
import netUtils.common.HttpConnectionCallback;
import netUtils.common.URLDef;
import netUtils.request.common.BaseRequest;

public class BindWeChatRequest extends BaseRequest
{
    public BindWeChatRequest(String password, String openID, String unionID, String token)
    {
        super();

        addParams("name", AppPreference.getAppPreference().getUsername());
        addParams("password", password);
        addParams("openid", openID);
        addParams("unionid", unionID);
        addParams("token", token);

        appendUrl(URLDef.URL_BIND);
    }

    public void sendRequest(HttpConnectionCallback callback)
    {
        doPost(callback);
    }
}
