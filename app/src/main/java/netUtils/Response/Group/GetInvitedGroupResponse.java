package netUtils.response.group;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
            for (int i = 0; i < jsonArray.length(); i++)
            {
                JSONObject object = jsonArray.getJSONObject(i);
                Group group = new Group();
                group.setServerID(object.getInt("gid"));
                group.setName(object.getString("groupname"));
                group.setCreatedDate(object.getInt("createdt"));
                groupList.add(group);

                Invite invite = new Invite();
                invite.setServerID(object.getInt("id"));
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
