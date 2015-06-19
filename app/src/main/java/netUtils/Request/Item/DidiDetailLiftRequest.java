package netUtils.request.item;

import netUtils.common.HttpConnectionCallback;
import netUtils.common.URLDef;
import netUtils.request.common.BaseRequest;

public class DidiDetailLiftRequest extends BaseRequest
{
    public DidiDetailLiftRequest(String orderID, String token)
    {
        super();

        addParams("accuracy", "");
        addParams("appversion", "3.9.2");
        addParams("channel", "102");
        addParams("datatype", "101");
        addParams("imei", "");
        addParams("lat", "undefined");
        addParams("lng", "undefined");
        addParams("maptype", "soso");
        addParams("model", "android");
        addParams("networkType", "WIFI");
        addParams("order_id", orderID);
        addParams("os", "8.3");
        addParams("sig", "");
        addParams("token", token);

        setUrl(URLDef.URL_DIDI_LIFT_DETAIL);
    }

    public void sendRequest(HttpConnectionCallback callback)
    {
        doGet(callback);
    }
}