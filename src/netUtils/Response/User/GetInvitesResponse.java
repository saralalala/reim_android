package netUtils.Response.User;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import classes.Invite;
import classes.Utils.AppPreference;

import netUtils.Response.BaseResponse;

public class GetInvitesResponse extends BaseResponse
{
	private List<Invite> inviteList;
	
	public GetInvitesResponse(Object httpResponse)
	{
		super(httpResponse);
	}

	protected void constructData()
	{
		try
		{
			String currentNickname = AppPreference.getAppPreference().getCurrentUser().getNickname();
			
			JSONArray jsonArray = getDataArray();
			inviteList = new ArrayList<Invite>();
			for (int i = 0 ; i < jsonArray.length() ; i++)
			{
				JSONObject jObject = jsonArray.getJSONObject(i);
				
				String invitor = jObject.getString("invitor");
				int activeType = jObject.getInt("actived");
				if (!invitor.equals(currentNickname) && activeType == Invite.TYPE_NEW)
				{
					String message = "用户" + invitor + "邀请您加入" + jObject.getString("groupname");
					
					Invite invite = new Invite();
					invite.setInviteCode(jObject.getString("code"));
					invite.setUpdateTime(jObject.getInt("invitedt"));
					invite.setMessage(message);
					
					inviteList.add(invite);
				}
				else if (invitor.equals(currentNickname) && activeType == Invite.TYPE_REJECTED)
				{
					String message = "用户" + jObject.getString("iname") + "拒绝了您发送的加入" + jObject.getString("groupname") + "的入组邀请";
					
					Invite invite = new Invite();
					invite.setInviteCode(jObject.getString("code"));
					invite.setUpdateTime(jObject.getInt("activedt"));
					invite.setMessage(message);
					
					inviteList.add(invite);
				}
				else if (invitor.equals(currentNickname) && activeType == Invite.TYPE_ACCEPTED)
				{
					String message = "用户" + jObject.getString("iname") + "同意了您发送的加入" + jObject.getString("groupname") + "的入组邀请";
					
					Invite invite = new Invite();
					invite.setInviteCode(jObject.getString("code"));
					invite.setUpdateTime(jObject.getInt("activedt"));
					invite.setMessage(message);
					
					inviteList.add(invite);
				}
			}			
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}		
	}

	public List<Invite> getInviteList()
	{
		return inviteList;
	}

	public void setInviteList(List<Invite> inviteList)
	{
		this.inviteList = inviteList;
	}
}
