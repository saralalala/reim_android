package netUtils.request.statistics;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

import netUtils.HttpConnectionCallback;
import netUtils.URLDef;
import netUtils.request.BaseRequest;

public class OthersStatRequest extends BaseRequest
{
    public OthersStatRequest(int year, int month)
    {
        super();

        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("year", Integer.toString(year)));
        params.add(new BasicNameValuePair("month", Integer.toString(month)));
        setParams(params);

        appendUrl(URLDef.URL_STATISTICS_OTHERS);
    }

	public OthersStatRequest(int year, int month, int categoryID, int subCategoryID, int tagID, int userID)
	{
		super();

        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("year", Integer.toString(year)));
        params.add(new BasicNameValuePair("month", Integer.toString(month)));
        if (categoryID != -1)
        {
            params.add(new BasicNameValuePair("categoryID", Integer.toString(categoryID)));
        }
        if (subCategoryID != -1)
        {
            params.add(new BasicNameValuePair("subCategoryID", Integer.toString(subCategoryID)));
        }
        if (tagID != -1)
        {
            params.add(new BasicNameValuePair("tagID", Integer.toString(tagID)));
        }
        if (userID != -1)
        {
            params.add(new BasicNameValuePair("userID", Integer.toString(userID)));
        }
        setParams(params);

		appendUrl(URLDef.URL_STATISTICS_OTHERS);
	}

	public void sendRequest(HttpConnectionCallback callback)
	{
		doGet(callback);
	}
}
