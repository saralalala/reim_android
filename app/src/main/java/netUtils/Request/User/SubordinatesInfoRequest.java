package netUtils.request.user;

import netUtils.HttpConnectionCallback;
import netUtils.URLDef;
import netUtils.request.BaseRequest;

public class SubordinatesInfoRequest extends BaseRequest
{
    public SubordinatesInfoRequest(int pageIndex, int pageSize)
    {
        super();

        appendUrl(URLDef.URL_SUBORDINATE);
        appendUrl(pageIndex);
        appendUrl(pageSize);
    }

    public void sendRequest(HttpConnectionCallback callback)
    {
        doGet(callback);
    }
}
