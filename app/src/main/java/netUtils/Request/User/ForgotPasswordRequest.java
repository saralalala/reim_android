package netUtils.request.user;

import netUtils.common.HttpConnectionCallback;
import netUtils.common.URLDef;
import netUtils.request.common.BaseRequest;

public class ForgotPasswordRequest extends BaseRequest
{
    public ForgotPasswordRequest(int type, String arg)
    {
        super();

        addParams("type", Integer.toString(type));
        addParams("name", arg);

        appendUrl(URLDef.URL_PASSWORD);
    }

    public ForgotPasswordRequest(String phone, String verifyCode)
    {
        super();

        addParams("type", Integer.toString(1));
        addParams("name", phone);
        addParams("vcode", verifyCode);

        appendUrl(URLDef.URL_PASSWORD);
    }

    public void sendRequest(HttpConnectionCallback callback)
    {
        doPost(callback);
    }
}