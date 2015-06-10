package netUtils.request.group;

import netUtils.common.HttpConnectionCallback;
import netUtils.common.URLDef;
import netUtils.request.common.BaseRequest;

public class GetInvitedGroupRequest extends BaseRequest
{
    public GetInvitedGroupRequest()
    {
        super();

        appendUrl(URLDef.URL_INVITES);
    }

    public void sendRequest(HttpConnectionCallback callback)
    {
        doGet(callback);
    }
}