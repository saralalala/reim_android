package netUtils.request.user;

import netUtils.common.HttpConnectionCallback;
import netUtils.common.URLDef;
import netUtils.request.common.BaseRequest;

public class SandboxOAuthRequest extends BaseRequest
{
    public SandboxOAuthRequest()
    {
        super();

        appendUrl(URLDef.URL_OAUTH);
    }

    public void sendRequest(HttpConnectionCallback callback)
    {
        doPost(callback);
    }
}