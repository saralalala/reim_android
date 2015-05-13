package netUtils.request.user;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

import classes.utils.WeChatUtils;
import netUtils.HttpConnectionCallback;
import netUtils.URLDef;
import netUtils.request.BaseRequest;

public class WeChatAccessTokenRequest extends BaseRequest
{
	public WeChatAccessTokenRequest(String code)
	{
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("appid", WeChatUtils.APP_ID));
		params.add(new BasicNameValuePair("secret", WeChatUtils.APP_SECRET));
        params.add(new BasicNameValuePair("code", code));
        params.add(new BasicNameValuePair("grant_type", "authorization_code"));
		setParams(params);

		setUrl(URLDef.URL_WECHAT);
	}

	public void sendRequest(HttpConnectionCallback callback)
	{
		doGet(callback);
	}
}