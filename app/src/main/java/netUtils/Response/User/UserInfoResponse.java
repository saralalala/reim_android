package netUtils.response.user;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

import classes.model.User;
import netUtils.response.common.BaseResponse;

public class UserInfoResponse extends BaseResponse
{
    private User user;

    public UserInfoResponse(Object httpResponse)
    {
        super(httpResponse);
    }

    protected void constructData()
    {
        try
        {
            JSONObject jObject = getDataObject();
            User user = new User();
            user.setEmail(jObject.getString("email"));
            user.setIsActive(Boolean.valueOf(jObject.getString("valid")));
            user.setDefaultManagerID(Integer.valueOf(jObject.getString("manager")));
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    public User getUser()
    {
        return user;
    }

    public void setUser(User user)
    {
        this.user = user;
    }
}
