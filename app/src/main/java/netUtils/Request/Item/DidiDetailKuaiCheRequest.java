package netUtils.request.item;

import netUtils.common.HttpConnectionCallback;
import netUtils.common.URLDef;
import netUtils.request.common.BaseRequest;

public class DidiDetailKuaiCheRequest extends BaseRequest
{
    public DidiDetailKuaiCheRequest(String orderID, String token)
    {
        super();

        addParams("appversion", "3.9.2");
        addParams("channel", "102");
        addParams("datatype", "101");
        addParams("imei", "");
        addParams("lat", "undefined");
        addParams("lng", "undefined");
        addParams("maptype", "soso");
        addParams("model", "android");
        addParams("networkType", "android");
        addParams("oid", orderID);
        addParams("os", "5.3");
        addParams("sig", "");
        addParams("token", token);

        setUrl(URLDef.URL_DIDI_KUAI_CHE_DETAIL);
    }

    public void sendRequest(HttpConnectionCallback callback)
    {
        doGet(callback);
    }
}