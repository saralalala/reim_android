package netUtils.response.group;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.List;

import classes.model.Group;
import netUtils.response.common.BaseResponse;

public class SearchGroupResponse extends BaseResponse
{
    List<Group> groupList;

    public SearchGroupResponse(Object httpResponse)
    {
        super(httpResponse);
    }

    protected void constructData()
    {
        try
        {
            JSONArray jsonArray = getDataArray();
            groupList = new ArrayList<>();
            for (int i = 0; i < jsonArray.size(); i++)
            {
                JSONObject object = jsonArray.getJSONObject(i);
                Group group = new Group();
                group.setServerID(object.getInteger("id"));
                group.setName(object.getString("company_name"));
                group.setCreatedDate(object.getInteger("createdt"));
                group.setLocalUpdatedDate(object.getInteger("lastdt"));
                group.setServerUpdatedDate(object.getInteger("lastdt"));
                groupList.add(group);
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    public List<Group> getGroupList()
    {
        return groupList;
    }
}
