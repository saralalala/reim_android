package netUtils.request.statistics;

import netUtils.HttpConnectionCallback;
import netUtils.URLDef;
import netUtils.request.BaseRequest;

public class OthersStatRequest extends BaseRequest
{
    public OthersStatRequest(int year, int month)
    {
        super();

        appendUrl(URLDef.URL_STATISTICS_OTHERS);
        appendUrl(year);
        appendUrl(month);
    }

    public OthersStatRequest(int year, int month, int categoryID, int tagID, int userID)
    {
        super();

        appendUrl(URLDef.URL_STATISTICS_OTHERS);
        appendUrl(year);
        appendUrl(month);
        appendUrl(categoryID);
        appendUrl(tagID);
        appendUrl(userID);
    }

    public void sendRequest(HttpConnectionCallback callback)
    {
        doGet(callback);
    }
}
