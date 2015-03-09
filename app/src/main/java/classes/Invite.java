package classes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import classes.utils.Utils;

public class Invite implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	public static final int TYPE_NEW = 0;
	public static final int TYPE_REJECTED = 1;
	public static final int TYPE_ACCEPTED = 2;
	
	private String inviteCode = "";
    private String invitor = "";
	private String message = "";
	private int updateTime = -1;
	private int typeCode = -1;
	
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

	public String getMessage()
	{
		return message;
	}
	public void setMessage(String message)
	{
		this.message = message;
	}
	
	public int getUpdateTime()
	{
		return updateTime;
	}
	public void setUpdateTime(int updateTime)
	{
		this.updateTime = updateTime;
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
				map.put("time", Utils.secondToStringUpToMinute(invite.getUpdateTime()));
				resultList.add(map);
			}
		}
		return resultList;
	}
	
	public static void sortByUpdateDate(List<Invite> inviteList)
    {
    	Collections.sort(inviteList, new Comparator<Invite>()
		{
			public int compare(Invite invite1, Invite invite2)
			{
				return (int) (invite2.getUpdateTime() - invite1.getUpdateTime());
			}
		});
    }
}