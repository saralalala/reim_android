package netUtils.request.item;

import netUtils.common.HttpConnectionCallback;
import netUtils.common.URLDef;
import netUtils.request.common.BaseRequest;

public class GetItemsRequest extends BaseRequest
{
    public GetItemsRequest(int pageIndex, int pageSize)
    {
        super();

        appendUrl(URLDef.URL_ITEM);
        appendUrl(pageIndex);
        appendUrl(pageSize);
    }

    public void sendRequest(HttpConnectionCallback callback)
    {
        doGet(callback);
    }
}
