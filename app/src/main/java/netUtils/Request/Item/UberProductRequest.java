package netUtils.request.item;

import netUtils.common.HttpConnectionCallback;
import netUtils.common.HttpUtils;
import netUtils.common.URLDef;
import netUtils.request.common.BaseRequest;

public class UberProductRequest extends BaseRequest
{
    public UberProductRequest(String token, String productID)
    {
        super();

        addHeaders(HttpUtils.getUberTokenHeader(token));

        setUrl(URLDef.URL_UBER_PRODUCT);
        appendUrl(productID);
    }

    public void sendRequest(HttpConnectionCallback callback)
    {
        doGet(callback);
    }
}