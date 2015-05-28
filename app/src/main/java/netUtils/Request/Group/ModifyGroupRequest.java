package netUtils.request.group;

import netUtils.HttpConnectionCallback;
import netUtils.URLDef;
import netUtils.request.BaseRequest;

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
