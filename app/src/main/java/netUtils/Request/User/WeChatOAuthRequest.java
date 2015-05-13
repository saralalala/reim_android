package netUtils.request.user;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

import netUtils.HttpConnectionCallback;
import netUtils.URLDef;
import netUtils.request.BaseRequest;

public class WeChatOAuthRequest extends BaseRequest
{
	public WeChatOAuthRequest(String accessToken, String openID)
	{
		super();
		
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("token", accessToken));
        params.add(new BasicNameValuePair("openid", openID));
		setParams(params);
		
		appendUrl(URLDef.URL_OAUTH);
	}

	public void sendRequest(HttpConnectionCallback callback)
	{
		doPost(callback);
	}
}