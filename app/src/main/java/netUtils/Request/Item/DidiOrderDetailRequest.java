package netUtils.request.item;

import netUtils.common.HttpConnectionCallback;
import netUtils.common.URLDef;
import netUtils.request.common.BaseRequest;

public class DidiOrderDetailRequest extends BaseRequest
{
    public DidiOrderDetailRequest(String orderID)
    {
        super();

        addParams("webapp", "baidu");
        addParams("oid", orderID);
        addParams("token", "IXaxc9iKsjNJMY%2Bi5u7f%2BuSQmfDRuKswqiP45pdQQYVUjk0KgzAQRu%2FyrVPIGFOjlykhD" +
                "lVITMnPooh379Cuuvre4g1vTvS%2Br1gAhS8MjsbJamcGPZpRK5QcGQsp%2BKfsTSBx2%2FJasZyoSYZEtGYmMy" +
                "nU3EsQb7gUQmHf%2BNH2xP9S4BglScYRuZns3Uo9bP44OP5eWX3z7f2SO319AgAA%2F%2F8%3D");

        setUrl(URLDef.URL_DIDI_DETAIL);
    }

    public void sendRequest(HttpConnectionCallback callback)
    {
        doGet(callback);
    }
}