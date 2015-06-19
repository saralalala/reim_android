package netUtils.request.item;

import netUtils.common.HttpConnectionCallback;
import netUtils.common.URLDef;
import netUtils.request.common.BaseRequest;

public class DidiDetailTaxiRequest extends BaseRequest
{
    public DidiDetailTaxiRequest(String orderID, String token)
    {
        super();

        addParams("webapp", "baidu");
        addParams("oid", orderID);
        addParams("token", token);

        setUrl(URLDef.URL_DIDI_TAXI_DETAIL);
    }

    public void sendRequest(HttpConnectionCallback callback)
    {
        doGet(callback);
    }
}