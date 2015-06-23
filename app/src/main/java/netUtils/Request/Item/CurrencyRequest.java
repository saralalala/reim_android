package netUtils.request.item;

import netUtils.common.HttpConnectionCallback;
import netUtils.common.URLDef;
import netUtils.request.common.BaseRequest;

public class CurrencyRequest extends BaseRequest
{
    public CurrencyRequest()
    {
        super();

        appendUrl(URLDef.URL_CURRENCY);
    }

    public void sendRequest(HttpConnectionCallback callback)
    {
        doGet(callback);
    }
}
