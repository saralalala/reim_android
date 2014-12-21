package netUtils.Response.User;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import classes.Invite;

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
			JSONArray jsonArray = getDataArray();
			inviteList = new ArrayList<Invite>();
			for (int i = 0 ; i < jsonArray.length() ; i++)
			{
				JSONObject jObject = jsonArray.getJSONObject(i);
				
				Invite invite = new Invite();
				invite.setInviteCode(jObject.getString("code"));
				invite.setInviteTime(jObject.getInt("invitedt"));
				switch (jObject.getInt("actived"))
				{
					case Invite.TYPE_NEW:
					{
						String message = "用户" + jObject.getString("invitor") + "邀请您加入" + jObject.getString("groupname");
						invite.setMessage(message);						
						break;
					}
					case Invite.TYPE_REJECTED:
					{
						String message = "用户" + jObject.getString("invitor") + "拒绝了您的入组邀请";
						invite.setMessage(message);						
						break;
					}
					case Invite.TYPE_ACCEPTED:
					{
						String message = "用户" + jObject.getString("invitor") + "同意了您的入组邀请";
						invite.setMessage(message);						
						break;
					}
					default:
						break;
				}
				inviteList.add(invite);
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
