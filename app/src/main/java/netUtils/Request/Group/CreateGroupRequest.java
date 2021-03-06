package netUtils.request.group;

import classes.utils.Utils;
import netUtils.common.HttpConnectionCallback;
import netUtils.common.URLDef;
import netUtils.request.common.BaseRequest;

public class CreateGroupRequest extends BaseRequest
{
    public CreateGroupRequest(String groupName, boolean forceCreate)
    {
        super();

        addParams("name", groupName);
        addParams("force", Utils.booleanToString(forceCreate));

        appendUrl(URLDef.URL_GROUP);
    }

    public CreateGroupRequest(String groupName, boolean forceCreate, int guideVersion)
    {
        super();

        addParams("name", groupName);
        addParams("force", Utils.booleanToString(forceCreate));
        addParams("version", Integer.toString(guideVersion));

        appendUrl(URLDef.URL_GROUP);
    }

    public void sendRequest(HttpConnectionCallback callback)
    {
        doPost(callback);
    }
}
