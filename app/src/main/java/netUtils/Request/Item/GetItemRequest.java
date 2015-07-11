package netUtils.request.item;

import netUtils.common.HttpConnectionCallback;
import netUtils.common.URLDef;
import netUtils.request.common.BaseRequest;

public class GetItemRequest extends BaseRequest
{
    public GetItemRequest(int itemID)
    {
        super();

        appendUrl(URLDef.URL_ITEM);
        appendUrl(itemID);
    }

    public void sendRequest(HttpConnectionCallback callback)
    {
        doGet(callback);
    }
}
