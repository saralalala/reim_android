package netUtils.request.statistics;

import netUtils.common.HttpConnectionCallback;
import netUtils.common.URLDef;
import netUtils.request.common.BaseRequest;

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
