package netUtils.request.user;

import netUtils.common.HttpConnectionCallback;
import netUtils.common.URLDef;
import netUtils.request.common.BaseRequest;

public class SignOutRequest extends BaseRequest
{
    public SignOutRequest()
    {
        super();

        appendUrl(URLDef.URL_SIGN_OUT);
    }

    public void sendRequest(HttpConnectionCallback callback)
    {
        doGet(callback);
    }
}
