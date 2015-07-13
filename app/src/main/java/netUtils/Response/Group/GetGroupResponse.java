package netUtils.response.group;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.List;

import classes.model.Group;
import classes.model.User;
import netUtils.response.common.BaseResponse;

public class GetGroupResponse extends BaseResponse
{
    private Group group;
    private List<User> memberList;

    public GetGroupResponse(Object httpResponse)
    {
        super(httpResponse);
    }

    protected void constructData()
    {
        try
        {
            JSONObject jObject = getDataObject();

            int groupID = -1;
            JSONObject groupObject = jObject.getJSONObject("ginfo");
            if (groupObject.getInteger("groupid") != -1)
            {
                group = new Group(groupObject);
                groupID = group.getServerID();
            }

            JSONArray memberArray = jObject.getJSONArray("gmember");
            memberList = new ArrayList<>();
            for (int i = 0; i < memberArray.size(); i++)
            {
                User user = new User(memberArray.getJSONObject(i), groupID);
                memberList.add(user);
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    public Group getGroup()
    {
        return group;
    }

    public List<User> getMemberList()
    {
        return memberList;
    }
}