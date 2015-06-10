package netUtils.request.group;

import netUtils.common.HttpConnectionCallback;
import netUtils.common.URLDef;
import netUtils.request.common.BaseRequest;

public class ModifyGroupRequest extends BaseRequest
{
    public ModifyGroupRequest(String groupName)
    {
        super();

        addParams("name", groupName);

        appendUrl(URLDef.URL_GROUP);
    }

    public void sendRequest(HttpConnectionCallback callback)
    {
        doPut(callback);
    }
}
