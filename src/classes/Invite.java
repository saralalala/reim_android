package classes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Invite implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	private int inviteCode = -1;
	private String message = "";
	
	public int getInviteCode()
	{
		return inviteCode;
	}
	public void setInviteCode(int inviteCode)
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
