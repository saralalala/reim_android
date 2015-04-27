package netUtils.request.group;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

import netUtils.HttpConnectionCallback;
import netUtils.URLDef;
import netUtils.request.BaseRequest;

public class CreateGroupRequest extends BaseRequest
{
	public CreateGroupRequest(String groupName)
	{
		super();
		
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("name", groupName));
		setParams(params);

		appendUrl(URLDef.URL_GROUP);
	}

    public CreateGroupRequest(String groupName, String inviteList)
    {
        super();

        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("name", groupName));
        if (!inviteList.isEmpty())
        {
            params.add(new BasicNameValuePair("invites", inviteList));
        }
        setParams(params);

        appendUrl(URLDef.URL_GROUP);
    }


    public CreateGroupRequest(String groupName, String inviteList, int guideVersion)
    {
        super();

        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("name", groupName));
        params.add(new BasicNameValuePair("version", Integer.toString(guideVersion)));
        if (!inviteList.isEmpty())
        {
            params.add(new BasicNameValuePair("invites", inviteList));
        }
        setParams(params);

        appendUrl(URLDef.URL_GROUP);
    }

	public void sendRequest(HttpConnectionCallback callback)
	{
		doPost(callback);
	}
}
