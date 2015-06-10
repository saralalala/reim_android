package netUtils.response.user;

import org.json.JSONException;
import org.json.JSONObject;

import netUtils.response.common.BaseResponse;

public class BindWeChatResponse extends BaseResponse
{
    private String wechatNickname;

    public BindWeChatResponse(Object httpResponse)
    {
        super(httpResponse);
    }

    protected void constructData()
    {
        try
        {
            JSONObject jObject = getDataObject();
            wechatNickname = jObject.getString("weixin_nickname");
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    public String getWechatNickname()
    {
        return wechatNickname;
    }
}
