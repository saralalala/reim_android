package netUtils.request.group;

import netUtils.HttpConnectionCallback;
import netUtils.URLDef;
import netUtils.request.BaseRequest;

public class GetInvitedGroupRequest extends BaseRequest
{
    public GetInvitedGroupRequest()
    {
        super();

        appendUrl(URLDef.URL_INVITED_COMPANY);
    }

    public void sendRequest(HttpConnectionCallback callback)
    {
        doGet(callback);
    }
}