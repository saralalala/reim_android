package netUtils.request.user;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

import classes.model.User;
import netUtils.HttpConnectionCallback;
import netUtils.URLDef;
import netUtils.request.BaseRequest;

public class ModifyUserRequest extends BaseRequest
{
	public ModifyUserRequest(User user)
	{
		super();
		
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("email", user.getEmail()));
		params.add(new BasicNameValuePair("phone", user.getPhone()));
		params.add(new BasicNameValuePair("nickname", user.getNickname()));
		setParams(params);

		appendUrl(URLDef.URL_USER);
	}

    public ModifyUserRequest(User user, String verifyCode)
    {
        super();

        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("phone", user.getPhone()));
        params.add(new BasicNameValuePair("vcode", verifyCode));
        setParams(params);

        appendUrl(URLDef.URL_USER);
    }

	public void sendRequest(HttpConnectionCallback callback)
	{
		doPut(callback);
	}
}
