package netUtils.request.user;

import netUtils.HttpConnectionCallback;
import netUtils.URLDef;
import netUtils.request.BaseRequest;

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
