package netUtils.request.item;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

import netUtils.HttpConnectionCallback;
import netUtils.URLDef;
import netUtils.request.BaseRequest;

public class SearchItemsRequest extends BaseRequest
{
    public SearchItemsRequest(String keyword)
    {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("keyword", keyword));
        setParams(params);

        appendUrl(URLDef.URL_SEARCH);
    }

    public void sendRequest(HttpConnectionCallback callback)
    {
        doPost(callback);
    }
}
