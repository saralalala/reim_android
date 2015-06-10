package netUtils.request.user;

import netUtils.common.HttpConnectionCallback;
import netUtils.common.URLDef;
import netUtils.request.common.BaseRequest;

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