package netUtils.request.user;

import netUtils.common.HttpConnectionCallback;
import netUtils.common.URLDef;
import netUtils.request.common.BaseRequest;

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