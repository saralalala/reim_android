package netUtils.request.user;

import netUtils.HttpConnectionCallback;
import netUtils.URLDef;
import netUtils.request.BaseRequest;

public class InviteRequest extends BaseRequest
{
    public InviteRequest(String username)
    {
        super();

        addParams("name", username);

        appendUrl(URLDef.URL_INVITE);
    }

    public void sendRequest(HttpConnectionCallback callback)
    {
        doPost(callback);
    }
}