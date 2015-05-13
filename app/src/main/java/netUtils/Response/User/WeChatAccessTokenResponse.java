package netUtils.response.user;

import org.json.JSONException;
import org.json.JSONObject;

public class WeChatAccessTokenResponse
{
	private boolean status;
    private String accessToken;
    private String openID;

	public WeChatAccessTokenResponse(Object httpResponse)
	{
		try
		{
			JSONObject jObject = new JSONObject((String)httpResponse);
            accessToken = jObject.getString("access_token");
            openID = jObject.getString("openid");
            status = true;
		}
		catch (JSONException e)
		{
			e.printStackTrace();
			status = false;
            accessToken = "";
            openID = "";
		}
	}

	public boolean getStatus()
	{
		return status;
	}

	public void setStatus(boolean status)
	{
		this.status = status;
	}

    public String getAccessToken()
    {
        return accessToken;
    }

    public void setAccessToken(String accessToken)
    {
        this.accessToken = accessToken;
    }

    public String getOpenID()
    {
        return openID;
    }

    public void setOpenID(String openID)
    {
        this.openID = openID;
    }
}