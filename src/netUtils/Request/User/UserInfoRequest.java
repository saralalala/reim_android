package netUtils.Request.User;

import netUtils.HttpConnectionCallback;
import netUtils.Request.BaseRequest;

public class UserInfoRequest extends BaseRequest
{
	public UserInfoRequest(int userID)
	{
		super();

		String urlSuffix = "/users/" + userID;
		appendUrl(urlSuffix);
	}

	public void sendRequest(HttpConnectionCallback callback)
	{
		doGet(callback);
	}
}
