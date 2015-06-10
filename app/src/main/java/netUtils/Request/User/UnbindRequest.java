package netUtils.request.user;

import netUtils.common.HttpConnectionCallback;
import netUtils.common.URLDef;
import netUtils.request.common.BaseRequest;

public class UnbindRequest extends BaseRequest
{
    public UnbindRequest(int type)
    {
        super();

        addParams("type", Integer.toString(type));

        appendUrl(URLDef.URL_BIND);
    }

    public void sendRequest(HttpConnectionCallback callback)
    {
        doPut(callback);
    }
}