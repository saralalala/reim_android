package netUtils.request.common;

import netUtils.common.HttpConnectionCallback;
import netUtils.common.URLDef;

public class CommonRequest extends BaseRequest
{
    public CommonRequest()
    {
        super();

        appendUrl(URLDef.URL_COMMON);
    }

    public void sendRequest(HttpConnectionCallback callback)
    {
        doGet(callback);
    }
}