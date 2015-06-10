package netUtils.response.group;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
            groupList = new ArrayList<Group>();
            for (int i = 0; i < jsonArray.length(); i++)
            {
                JSONObject object = jsonArray.getJSONObject(i);
                Group group = new Group();
                group.setServerID(object.getInt("id"));
                group.setName(object.getString("company_name"));
                group.setCreatedDate(object.getInt("createdt"));
                group.setLocalUpdatedDate(object.getInt("lastdt"));
                group.setServerUpdatedDate(object.getInt("lastdt"));
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
