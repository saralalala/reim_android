package netUtils.request.proxy;

import classes.model.Proxy;
import netUtils.common.HttpConnectionCallback;
import netUtils.common.URLDef;
import netUtils.request.common.BaseRequest;

public class ModifyProxyRequest extends BaseRequest
{
    public ModifyProxyRequest(Proxy proxy)
    {
        super();

        addParams("act", 2);
        addParams("proxy", proxy.getUser().getServerID());
        addParams("permission", proxy.getPermission());

        appendUrl(URLDef.URL_WINGMAN);
    }

    public void sendRequest(HttpConnectionCallback callback)
    {
        doPut(callback);
    }
}