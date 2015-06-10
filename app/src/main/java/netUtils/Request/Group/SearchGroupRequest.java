package netUtils.request.group;

import netUtils.common.HttpConnectionCallback;
import netUtils.common.URLDef;
import netUtils.request.common.BaseRequest;

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
