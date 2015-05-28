package netUtils.request.user;

import netUtils.HttpConnectionCallback;
import netUtils.URLDef;
import netUtils.request.BaseRequest;

public class VerifyCodeRequest extends BaseRequest
{
    public VerifyCodeRequest(String phone)
    {
        super();

        addParams("phone", phone);

        appendUrl(URLDef.URL_CODE);
    }

    public void sendRequest(HttpConnectionCallback callback)
    {
        doPost(callback);
    }
}