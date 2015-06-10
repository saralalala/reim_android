package netUtils.request.group;

import netUtils.common.HttpConnectionCallback;
import netUtils.common.URLDef;
import netUtils.request.common.BaseRequest;

public class DeleteGroupRequest extends BaseRequest
{
    public DeleteGroupRequest()
    {
        super();

        appendUrl(URLDef.URL_GROUP);
    }

    public void sendRequest(HttpConnectionCallback callback)
    {
        doDelete(callback);
    }
}