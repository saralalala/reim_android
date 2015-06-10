package netUtils.request.user;

import netUtils.common.HttpConnectionCallback;
import netUtils.common.URLDef;
import netUtils.request.common.BaseRequest;

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
