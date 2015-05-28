package netUtils.request.item;

import netUtils.HttpConnectionCallback;
import netUtils.URLDef;
import netUtils.request.BaseRequest;

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
