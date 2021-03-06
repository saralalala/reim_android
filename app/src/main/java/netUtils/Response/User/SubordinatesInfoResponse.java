package netUtils.response.user;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

import java.util.List;

import classes.model.User;
import netUtils.response.common.BaseResponse;

public class SubordinatesInfoResponse extends BaseResponse
{
    private List<User> userList;

    private int total;

    public SubordinatesInfoResponse(Object httpResponse)
    {
        super(httpResponse);
    }

    protected void constructData()
    {
        try
        {
            JSONObject jObject = getDataObject();
            JSONArray jsonArray = jObject.getJSONArray("data");
            setTotal(Integer.valueOf(jObject.getString("total")));
            for (int i = 0; i < jsonArray.size(); i++)
            {
                JSONObject object = jsonArray.getJSONObject(i);
                User user = new User();
                user.setEmail(object.getString("email"));
                user.setServerID(Integer.valueOf(object.getString("id")));
                userList.add(user);
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    public List<User> getUserList()
    {
        return userList;
    }

    public int getTotal()
    {
        return total;
    }

    public void setTotal(int total)
    {
        this.total = total;
    }
}