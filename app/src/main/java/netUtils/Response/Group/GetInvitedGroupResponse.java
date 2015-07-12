package netUtils.response.group;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.List;

import classes.model.Group;
import classes.model.Invite;
import netUtils.response.common.BaseResponse;

public class GetInvitedGroupResponse extends BaseResponse
{
    List<Group> groupList;
    List<Invite> inviteList;

    public GetInvitedGroupResponse(Object httpResponse)
    {
        super(httpResponse);
    }

    protected void constructData()
    {
        try
        {
            JSONArray jsonArray = getDataArray();
            groupList = new ArrayList<>();
            inviteList = new ArrayList<>();
            for (int i = 0; i < jsonArray.size(); i++)
            {
                JSONObject object = jsonArray.getJSONObject(i);
                Group group = new Group();
                group.setServerID(object.getInteger("gid"));
                group.setName(object.getString("groupname"));
                group.setCreatedDate(object.getInteger("createdt"));
                groupList.add(group);

                Invite invite = new Invite();
                invite.setServerID(object.getInteger("id"));
                invite.setInviteCode(object.getString("code"));
                inviteList.add(invite);
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

    public List<Invite> getInviteList()
    {
        return inviteList;
    }
}
