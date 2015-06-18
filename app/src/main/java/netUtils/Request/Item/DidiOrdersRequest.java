package netUtils.request.item;

import netUtils.common.HttpConnectionCallback;
import netUtils.common.URLDef;
import netUtils.request.common.BaseRequest;

public class DidiOrdersRequest extends BaseRequest
{
    public DidiOrdersRequest()
    {
        super();

        appendUrl(URLDef.URL_DIDI_ORDER);
    }

    public DidiOrdersRequest(int pageIndex, int pageSize)
    {
        super();

        appendUrl(URLDef.URL_DIDI_ORDER);
        appendUrl(pageIndex);
        appendUrl(pageSize);
    }

    public void sendRequest(HttpConnectionCallback callback)
    {
        doGet(callback);
    }
}
