package netUtils.request.group;

import netUtils.common.HttpConnectionCallback;
import netUtils.common.URLDef;
import netUtils.request.common.BaseRequest;

public class GetGroupRequest extends BaseRequest
{
    public GetGroupRequest()
    {
        super();

        appendUrl(URLDef.URL_GROUP);
    }

    public void sendRequest(HttpConnectionCallback callback)
    {
        doGet(callback);
    }
}