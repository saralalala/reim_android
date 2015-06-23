package netUtils.request.proxy;

import classes.model.Proxy;
import netUtils.common.HttpConnectionCallback;
import netUtils.common.URLDef;
import netUtils.request.common.BaseRequest;

public class CreateProxyRequest extends BaseRequest
{
    public CreateProxyRequest(Proxy proxy)
    {
        super();

        addParams("proxy", proxy.getUser().getServerID());
        addParams("permission", proxy.getPermission());

        appendUrl(URLDef.URL_WINGMAN);
    }

    public void sendRequest(HttpConnectionCallback callback)
    {
        doPost(callback);
    }
}