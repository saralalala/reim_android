package netUtils.request.statistics;

import netUtils.common.HttpConnectionCallback;
import netUtils.common.URLDef;
import netUtils.request.common.BaseRequest;

public class OthersStatRequest extends BaseRequest
{
    public OthersStatRequest(int year, int month)
    {
        super();

        addParams("year", year);
        addParams("month", month);

        appendUrl(URLDef.URL_STATISTICS_OTHERS);
    }

    public OthersStatRequest(int year, int month, int categoryID, int tagID, int userID, String currencyCode, int status)
    {
        super();

        addParams("year", year);
        addParams("month", month);
        addParams("category", categoryID);
        addParams("tagID", tagID);
        addParams("user", userID);
        addParams("currency", currencyCode);
        addParams("status", status);

        appendUrl(URLDef.URL_STATISTICS_OTHERS);
    }

    public void sendRequest(HttpConnectionCallback callback)
    {
        doPost(callback);
    }
}
