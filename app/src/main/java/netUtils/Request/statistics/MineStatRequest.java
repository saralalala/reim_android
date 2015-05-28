package netUtils.request.statistics;

import netUtils.HttpConnectionCallback;
import netUtils.URLDef;
import netUtils.request.BaseRequest;

public class MineStatRequest extends BaseRequest
{
    public MineStatRequest()
    {
        super();

        appendUrl(URLDef.URL_STATISTICS);
    }

    public void sendRequest(HttpConnectionCallback callback)
    {
        doGet(callback);
    }
}
