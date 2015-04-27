package classes.base;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

import classes.utils.Utils;

public class Invite extends Message implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	public static final int TYPE_NEW = 0;
	public static final int TYPE_REJECTED = 1;
	public static final int TYPE_ACCEPTED = 2;
	
	private String inviteCode = "";
    private String invitor = "";
	private int typeCode = -1;

    public Invite()
    {

    }

    public Invite(JSONObject jObject, String currentNickname)
    {
        try
        {
            String invitor = jObject.getString("invitor");
            int activeType = jObject.getInt("actived");

            setServerID(jObject.getInt("id"));
            setInviteCode(jObject.getString("code"));
            setTypeCode(activeType);
            setInvitor(invitor);
            setType(Message.TYPE_INVITE);
            setHasBeenRead(Utils.intToBoolean(jObject.getInt("sread")));

            if (!invitor.equals(currentNickname) && activeType == Invite.TYPE_NEW)
            {
                String message = "用户" + invitor + "邀请您加入「" + jObject.getString("groupname") + "」";
                setTitle(message);
                setContent(message);
                setUpdateTime(jObject.getInt("invitedt"));
            }
            else if (invitor.equals(currentNickname) && activeType == Invite.TYPE_NEW)
            {
                String message = "您邀请了用户" + jObject.getString("iname") + "加入「" + jObject.getString("groupname") + "」";
                setTitle(message);
                setContent(message);
                setUpdateTime(jObject.getInt("invitedt"));
            }
            else if (!invitor.equals(currentNickname) && activeType == Invite.TYPE_REJECTED)
            {
                String message = "您拒绝了加入「" + jObject.getString("groupname") + "」的邀请";
                setTitle(message);
                setContent(message);
                setUpdateTime(jObject.getInt("activedt"));
            }
            else if (invitor.equals(currentNickname) && activeType == Invite.TYPE_REJECTED)
            {
                String message = "用户" + jObject.getString("iname") + "拒绝了加入「" + jObject.getString("groupname") + "」的邀请";
                setTitle(message);
                setContent(message);
                setUpdateTime(jObject.getInt("activedt"));
            }
            else if (!invitor.equals(currentNickname) && activeType == Invite.TYPE_ACCEPTED)
            {
                String message = "您已加入「" + jObject.getString("groupname") + "」";
                setTitle(message);
                setContent(message);
                setUpdateTime(jObject.getInt("activedt"));
            }
            else
            {
                String message = "用户" + jObject.getString("iname") + "已加入「" + jObject.getString("groupname") + "」";
                setTitle(message);
                setContent(message);
                setUpdateTime(jObject.getInt("activedt"));
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

	public String getInviteCode()
	{
		return inviteCode;
	}
	public void setInviteCode(String inviteCode)
	{
		this.inviteCode = inviteCode;
	}

    public String getInvitor()
    {
        return invitor;
    }
    public void setInvitor(String invitor)
    {
        this.invitor = invitor;
    }

	public int getTypeCode()
	{
		return typeCode;
	}
	public void setTypeCode(int typeCode)
	{
		this.typeCode = typeCode;
	}
}