package netUtils.response.user;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

public class WeChatAccessTokenResponse
{
    private boolean status;
    private String accessToken;
    private String openID;
    private String unionID;

    public WeChatAccessTokenResponse(Object httpResponse)
    {
        try
        {
            JSONObject jObject = JSON.parseObject((String) httpResponse);
            accessToken = jObject.getString("access_token");
            openID = jObject.getString("openid");
            unionID = jObject.getString("unionid");
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

    public String getOpenID()
    {
        return openID;
    }

    public String getUnionID()
    {
        return unionID;
    }
}