package netUtils.Request.User;

import netUtils.HttpConnectionCallback;
import netUtils.Request.BaseRequest;

public class UserInfoRequest extends BaseRequest
{
	public UserInfoRequest(int userID)
	{
		super();

		appendUrl("/users/" + userID);
	}

	public void sendRequest(HttpConnectionCallback callback)
	{
		doGet(callback);
	}
}
