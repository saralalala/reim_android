package netUtils.request.item;

import netUtils.common.HttpConnectionCallback;
import netUtils.common.URLDef;
import netUtils.request.common.BaseRequest;

public class SearchItemsRequest extends BaseRequest
{
    public SearchItemsRequest(String keyword)
    {
        addParams("keyword", keyword);

        appendUrl(URLDef.URL_SEARCH);
    }

    public void sendRequest(HttpConnectionCallback callback)
    {
        doPost(callback);
    }
}
