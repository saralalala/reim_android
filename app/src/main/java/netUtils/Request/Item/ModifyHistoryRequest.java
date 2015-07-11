package netUtils.request.item;

import netUtils.common.HttpConnectionCallback;
import netUtils.common.URLDef;
import netUtils.request.common.BaseRequest;

public class ModifyHistoryRequest extends BaseRequest
{
    public ModifyHistoryRequest(int itemID)
    {
        super();

        appendUrl(URLDef.URL_MODIFY_HISTORY);
        appendUrl(itemID);
    }

    public void sendRequest(HttpConnectionCallback callback)
    {
        doGet(callback);
    }
}