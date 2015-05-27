package netUtils.request.user;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

import netUtils.HttpConnectionCallback;
import netUtils.URLDef;
import netUtils.request.BaseRequest;

public class VerifyCodeRequest extends BaseRequest
{
    public VerifyCodeRequest(String phone)
    {
        super();

        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("phone", phone));
        setParams(params);

        appendUrl(URLDef.URL_CODE);
    }

    public void sendRequest(HttpConnectionCallback callback)
    {
        doPost(callback);
    }
}