package netUtils.request.proxy;

import netUtils.common.HttpConnectionCallback;
import netUtils.common.URLDef;
import netUtils.request.common.BaseRequest;

public class GetClientsRequest extends BaseRequest
{
    public GetClientsRequest()
    {
        super();

        appendUrl(URLDef.URL_WINGMAN);
        appendUrl(1);
    }

    public void sendRequest(HttpConnectionCallback callback)
    {
        doGet(callback);
    }
}