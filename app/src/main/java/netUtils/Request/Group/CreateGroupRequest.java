package netUtils.request.group;

import classes.utils.Utils;
import netUtils.HttpConnectionCallback;
import netUtils.URLDef;
import netUtils.request.BaseRequest;

public class CreateGroupRequest extends BaseRequest
{
    public CreateGroupRequest(String groupName, boolean forceCreate)
    {
        super();

        addParams("name", groupName);
        addParams("force", Utils.booleanToString(forceCreate));

        appendUrl(URLDef.URL_GROUP);
    }

    public CreateGroupRequest(String groupName, String inviteList, int guideVersion, boolean forceCreate)
    {
        super();

        addParams("name", groupName);
        addParams("force", Utils.booleanToString(forceCreate));
        addParams("version", Integer.toString(guideVersion));
        if (!inviteList.isEmpty())
        {
            addParams("invites", inviteList);
        }

        appendUrl(URLDef.URL_GROUP);
    }

    public void sendRequest(HttpConnectionCallback callback)
    {
        doPost(callback);
    }
}
