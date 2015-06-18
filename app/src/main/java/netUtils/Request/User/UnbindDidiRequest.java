package netUtils.request.user;

import netUtils.common.HttpConnectionCallback;
import netUtils.common.URLDef;
import netUtils.request.common.BaseRequest;

public class UnbindDidiRequest extends BaseRequest
{
    public UnbindDidiRequest()
    {
        super();

        appendUrl(URLDef.URL_DIDI_BIND);
    }

    public void sendRequest(HttpConnectionCallback callback)
    {
        doDelete(callback);
    }
}