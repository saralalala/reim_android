package netUtils.request.user;

import netUtils.common.HttpConnectionCallback;
import netUtils.common.URLDef;
import netUtils.request.common.BaseRequest;

public class BindDidiRequest extends BaseRequest
{
    public BindDidiRequest(String phone, String token)
    {
        super();

        addParams("phone", phone);
        addParams("token", token);

        appendUrl(URLDef.URL_DIDI_BIND);
    }

    public void sendRequest(HttpConnectionCallback callback)
    {
        doPost(callback);
    }
}
