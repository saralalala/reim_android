package netUtils.request.user;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

import netUtils.HttpConnectionCallback;
import netUtils.URLDef;
import netUtils.request.BaseRequest;

public class ForgotPasswordRequest extends BaseRequest
{
    public ForgotPasswordRequest(int type, String arg)
    {
        super();

        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("type", Integer.toString(type)));
        params.add(new BasicNameValuePair("name", arg));
        setParams(params);

        appendUrl(URLDef.URL_PASSWORD);
    }

    public ForgotPasswordRequest(String phone, String verifyCode)
    {
        super();

        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("type", Integer.toString(1)));
        params.add(new BasicNameValuePair("name", phone));
        params.add(new BasicNameValuePair("vcode", verifyCode));
        setParams(params);

        appendUrl(URLDef.URL_PASSWORD);
    }

	public void sendRequest(HttpConnectionCallback callback)
	{
		doPost(callback);
	}
}