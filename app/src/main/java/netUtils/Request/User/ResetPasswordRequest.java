package netUtils.request.user;

import netUtils.HttpConnectionCallback;
import netUtils.URLDef;
import netUtils.request.BaseRequest;

public class ResetPasswordRequest extends BaseRequest
{
    public ResetPasswordRequest(String password, String code)
    {
        super();

        addParams("password", password);
        addParams("code", code);

        appendUrl(URLDef.URL_PASSWORD);
    }

    public void sendRequest(HttpConnectionCallback callback)
    {
        doPut(callback);
    }
}
