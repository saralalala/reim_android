package netUtils.request.statistics;

import java.util.GregorianCalendar;

import classes.utils.AppPreference;
import netUtils.common.HttpConnectionCallback;
import netUtils.common.URLDef;
import netUtils.request.common.BaseRequest;

public class MineStatDetailRequest extends BaseRequest
{
    public MineStatDetailRequest(int year, int month, int tagID, int categoryID, String currencyCode)
    {
        super();

        long startTime = 0;
        long endTime = 0;

        if (year != 0 && month != 0)
        {
            GregorianCalendar greCal = new GregorianCalendar(year, month - 1, 1);
            startTime = greCal.getTimeInMillis() / 1000;

            month++;
            if (month == 13)
            {
                year++;
                month = 1;
            }

            greCal.set(year, month - 1, 1);
            endTime = greCal.getTimeInMillis() / 1000;
        }

        appendUrl(URLDef.URL_STATISTICS);
        appendUrl(startTime);
        appendUrl(endTime);
        appendUrl(tagID);
        appendUrl(AppPreference.getAppPreference().getCurrentUserID());
        appendUrl(categoryID);
        appendUrl(currencyCode);
    }

    public void sendRequest(HttpConnectionCallback callback)
    {
        doGet(callback);
    }
}
