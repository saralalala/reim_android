package netUtils.request;

import netUtils.HttpConnectionCallback;
import netUtils.URLDef;

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