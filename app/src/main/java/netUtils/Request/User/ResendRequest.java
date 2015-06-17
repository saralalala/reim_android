package netUtils.request.user;

import netUtils.common.HttpConnectionCallback;
import netUtils.common.URLDef;
import netUtils.request.common.BaseRequest;

public class ResendRequest extends BaseRequest
{
    public ResendRequest()
    {
        super();

        appendUrl(URLDef.URL_RESEND);
    }

    public void sendRequest(HttpConnectionCallback callback)
    {
        doPost(callback);
    }
}