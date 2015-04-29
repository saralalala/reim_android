package netUtils.request.user;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

import netUtils.HttpConnectionCallback;
import netUtils.URLDef;
import netUtils.request.BaseRequest;

public class ApplyRequest extends BaseRequest
{
    public ApplyRequest(int groupID, int guideVersion)
    {
        super();

        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("gid", Integer.toString(groupID)));
        params.add(new BasicNameValuePair("version", Integer.toString(guideVersion)));
        setParams(params);

        appendUrl(URLDef.URL_APPLY);
    }

	public void sendRequest(HttpConnectionCallback callback)
	{
		doPost(callback);
	}
}