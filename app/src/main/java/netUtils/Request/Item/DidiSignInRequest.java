package netUtils.request.item;

import netUtils.common.HttpConnectionCallback;
import netUtils.common.URLDef;
import netUtils.request.common.BaseRequest;

public class DidiSignInRequest extends BaseRequest
{
    public DidiSignInRequest(String phone, String code)
    {
        super();

        addParams("lat", "undefined");
        addParams("lng", "undefined");
        addParams("channel", "undefined");
        addParams("phone", phone);
        addParams("code", code);
        addParams("openid", "oDe7ajgT3tsnY1QmLFo2QAQYq41E");

        setUrl(String.format(URLDef.URL_DIDI_SIGN_IN, phone));
    }

    public void sendRequest(HttpConnectionCallback callback)
    {
        doPost(callback);
    }
}
