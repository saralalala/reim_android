package netUtils.request.proxy;

import netUtils.common.HttpConnectionCallback;
import netUtils.common.URLDef;
import netUtils.request.common.BaseRequest;

public class GetProxiesRequest extends BaseRequest
{
    public GetProxiesRequest()
    {
        super();

        appendUrl(URLDef.URL_WINGMAN);
        appendUrl(0);
    }

    public void sendRequest(HttpConnectionCallback callback)
    {
        doGet(callback);
    }
}