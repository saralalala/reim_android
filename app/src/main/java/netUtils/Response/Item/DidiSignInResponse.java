package netUtils.response.item;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

import classes.utils.JSONUtils;

public class DidiSignInResponse
{
    private boolean status;
    private String token;

    public DidiSignInResponse(Object httpResponse)
    {
        try
        {
            JSONObject jObject = JSON.parseObject((String) httpResponse);
            status = jObject.getInteger("errno") == 0;
            token = JSONUtils.optString(jObject, "token", "");
        }
        catch (JSONException e)
        {
            e.printStackTrace();
            status = false;
        }
    }

    public boolean getStatus()
    {
        return status;
    }

    public String getToken()
    {
        return token;
    }
}