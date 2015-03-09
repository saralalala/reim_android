package netUtils.Response.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import classes.Invite;
import classes.utils.AppPreference;
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

                Invite invite = new Invite();
                invite.setInviteCode(jObject.getString("code"));
                invite.setTypeCode(activeType);
                invite.setInvitor(invitor);

				if (!invitor.equals(currentNickname) && activeType == Invite.TYPE_NEW)
				{
					String message = "用户" + invitor + "邀请您加入「" + jObject.getString("groupname") + "」";
                    invite.setMessage(message);
					invite.setUpdateTime(jObject.getInt("invitedt"));
				}
                else if (invitor.equals(currentNickname) && activeType == Invite.TYPE_NEW)
                {
                    String message = "您邀请了用户" + invitor + "加入「" + jObject.getString("groupname" + "」");
                    invite.setMessage(message);
                    invite.setUpdateTime(jObject.getInt("invitedt"));
                }
				else if (!invitor.equals(currentNickname) && activeType == Invite.TYPE_REJECTED)
				{
					String message = "您拒绝了加入「" + jObject.getString("groupname") + "」的邀请";
                    invite.setMessage(message);
					invite.setUpdateTime(jObject.getInt("activedt"));
				}
                else if (invitor.equals(currentNickname) && activeType == Invite.TYPE_REJECTED)
                {
                    String message = "用户" + jObject.getString("iname") + "拒绝了加入「" + jObject.getString("groupname") + "」的邀请";
                    invite.setMessage(message);
                    invite.setUpdateTime(jObject.getInt("activedt"));
                }
				else if (!invitor.equals(currentNickname) && activeType == Invite.TYPE_ACCEPTED)
				{
                    String message = "您已加入「" + jObject.getString("groupname") + "」";
                    invite.setMessage(message);
					invite.setUpdateTime(jObject.getInt("activedt"));
				}
                else
                {
                    String message = "用户" + jObject.getString("iname") + "已加入「" + jObject.getString("groupname") + "」";
                    invite.setMessage(message);
                    invite.setUpdateTime(jObject.getInt("activedt"));
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
