package classes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Invite implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	private String inviteCode = "";
	private String message = "";
	
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
	
	public static List<String> getMessageList(List<Invite> inviteList)
	{
		List<String> messageList = new ArrayList<String>();
		for (Invite invite : inviteList)
		{
			messageList.add(invite.getMessage());
		}
		return messageList;
	}
}
