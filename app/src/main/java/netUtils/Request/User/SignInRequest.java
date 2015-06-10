package netUtils.request.user;

import netUtils.common.HttpConnectionCallback;
import netUtils.common.URLDef;
import netUtils.request.common.BaseRequest;

public class SignInRequest extends BaseRequest
{
    public SignInRequest()
    {
        super(5, 2);

        appendUrl(URLDef.URL_SIGN_IN);
    }

    public void sendRequest(HttpConnectionCallback callback)
    {
        doGet(callback);
    }
}