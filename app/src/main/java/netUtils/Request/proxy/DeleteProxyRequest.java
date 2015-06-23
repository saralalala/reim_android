package netUtils.request.proxy;

import netUtils.common.HttpConnectionCallback;
import netUtils.common.URLDef;
import netUtils.request.common.BaseRequest;

public class DeleteProxyRequest extends BaseRequest
{
    public DeleteProxyRequest(int userID)
    {
        super();

        appendUrl(URLDef.URL_WINGMAN);
        appendUrl(userID);
    }

    public void sendRequest(HttpConnectionCallback callback)
    {
        doDelete(callback);
    }
}