package classes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Invite implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	public static final int TYPE_NEW = 0;
	public static final int TYPE_REJECTED = 1;
	public static final int TYPE_ACCEPTED = 2;
	
	private String inviteCode = "";
	private String message = "";
	private int inviteTime = -1;
	private int typeCode = -1;
	
	public String getInviteCode()
	{
		return inviteCode;
	}
	public void setInviteCode(String inviteCode)
	{
		this.inviteCode = inviteCode;
	}
	
	public String getMessage()
	{
		return message;
	}
	public void setMessage(String message)
	{
		this.message = message;
	}
	
	public int getInviteTime()
	{
		return inviteTime;
	}
	public void setInviteTime(int inviteTime)
	{
		this.inviteTime = inviteTime;
	}
	
	public int getTypeCode()
	{
		return typeCode;
	}
	public void setTypeCode(int typeCode)
	{
		this.typeCode = typeCode;
	}
	
	public static List<Map<String, String>> getMessageList(List<Invite> inviteList)
	{
		List<Map<String, String>> resultList = new ArrayList<Map<String,String>>();
		if (inviteList != null)
		{
			for (Invite invite : inviteList)
			{
				Map<String, String> map = new HashMap<String, String>();
				map.put("message", invite.getMessage());
				map.put("time", Utils.secondToStringUpToMinute(invite.getInviteTime()));
				resultList.add(map);
			}
		}
		return resultList;
	}
}
