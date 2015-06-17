package netUtils.request.item;

import netUtils.common.HttpConnectionCallback;
import netUtils.common.URLDef;
import netUtils.request.common.BaseRequest;

public class DidiGetSMSRequest extends BaseRequest
{
    public DidiGetSMSRequest(String phone)
    {
        super();

        addParams("phone", phone);
        addParams("smstype", 0);
        addParams("source", 2);

        setUrl(URLDef.URL_DIDI_GET_SMS);
    }

    public void sendRequest(HttpConnectionCallback callback)
    {
        doGet(callback);
    }
}
