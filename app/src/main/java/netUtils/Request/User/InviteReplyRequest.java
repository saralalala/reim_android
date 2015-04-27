package netUtils.request.user;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

import netUtils.HttpConnectionCallback;
import netUtils.URLDef;
import netUtils.request.BaseRequest;

public class InviteReplyRequest extends BaseRequest
{
	public InviteReplyRequest(int agree, String inviteCode)
	{
		super();
		
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("status", Integer.toString(agree)));
		params.add(new BasicNameValuePair("code", inviteCode));
		setParams(params);

		appendUrl(URLDef.URL_INVITE);
	}

    public InviteReplyRequest(int agree, String inviteCode, int guideVersion)
    {
        super();

        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("status", Integer.toString(agree)));
        params.add(new BasicNameValuePair("code", inviteCode));
        params.add(new BasicNameValuePair("version", Integer.toString(guideVersion)));
        setParams(params);

        appendUrl(URLDef.URL_INVITE);
    }
	
	public void sendRequest(HttpConnectionCallback callback)
	{
		doPut(callback);
	}
}
