package netUtils.request.user;

import netUtils.HttpConnectionCallback;
import netUtils.URLDef;
import netUtils.request.BaseRequest;

public class InviteRequest extends BaseRequest
{
    public InviteRequest(String inviteList)
    {
        super();

        addParams("invites", inviteList);

        appendUrl(URLDef.URL_INVITES);
    }

    public void sendRequest(HttpConnectionCallback callback)
    {
        doPost(callback);
    }
}