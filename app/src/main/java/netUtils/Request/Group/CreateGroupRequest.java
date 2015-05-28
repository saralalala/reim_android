package netUtils.request.group;

import netUtils.HttpConnectionCallback;
import netUtils.URLDef;
import netUtils.request.BaseRequest;

public class CreateGroupRequest extends BaseRequest
{
    public CreateGroupRequest(String groupName)
    {
        super();

        addParams("name", groupName);

        appendUrl(URLDef.URL_GROUP);
    }

    public CreateGroupRequest(String groupName, String inviteList, int guideVersion)
    {
        super();

        addParams("name", groupName);
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
