package netUtils.request.group;

import netUtils.HttpConnectionCallback;
import netUtils.URLDef;
import netUtils.request.BaseRequest;

public class SearchGroupRequest extends BaseRequest
{
    public SearchGroupRequest(String keyword)
    {
        super();

        addParams("keyword", keyword);

        appendUrl(URLDef.URL_SEARCH_COMPANY);
    }

    public void sendRequest(HttpConnectionCallback callback)
    {
        doPost(callback);
    }
}
