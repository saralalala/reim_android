package classes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import database.DBManager;

public class User implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	private int serverID = -1;
	private String email = "";
	private String password = "";
	private String nickname = "";
	private String phone = "";
	private int imageID = -1;
	private String avatarPath = "";
	private int privilege = 0;
	private Boolean isActive = false;
	private Boolean isAdmin = false;
	private int groupID = -1;
	private int defaultManagerID = -1;
	private int serverUpdatedDate = -1;
	private int localUpdatedDate = -1;
	
	public int getServerID()
	{
		return serverID;
	}
	public void setServerID(int serverID)
	{
		this.serverID = serverID;
	}
	
	public String getEmail()
	{
		return email;
	}
	public void setEmail(String email)
	{
		this.email = email;
	}
	
	public String getPassword()
	{
		return password;
	}
	public void setPassword(String password)
	{
		this.password = password;
	}
	
	public String getNickname()
	{
		return nickname;
	}
	public void setNickname(String nickname)
	{
		this.nickname = nickname;
	}
	
	public String getPhone()
	{
		return phone;
	}
	public void setPhone(String phone)
	{
		this.phone = phone;
	}
	
	public int getImageID()
	{
		return imageID;
	}
	public void setImageID(int imageID)
	{
		this.imageID = imageID;
	}
	
	public String getAvatarPath()
	{
		return avatarPath;
	}
	public void setAvatarPath(String avatarPath)
	{
		this.avatarPath = avatarPath;
	}
	
	public int getPrivilege()
	{
		return privilege;
	}
	public void setPrivilege(int privilege)
	{
		this.privilege = privilege;
	}
	
	public Boolean isActive()
	{
		return isActive;
	}
	public void setIsActive(Boolean isActive)
	{
		this.isActive = isActive;
	}
	
	public Boolean isAdmin()
	{
		return isAdmin;
	}
	public void setIsAdmin(Boolean isAdmin)
	{
		this.isAdmin = isAdmin;
	}
	
	public int getGroupID()
	{
		return groupID;
	}
	public void setGroupID(int groupID)
	{
		this.groupID = groupID;
	}
	
	public int getDefaultManagerID()
	{
		return defaultManagerID;
	}
	public void setDefaultManagerID(int defaultManagerID)
	{
		this.defaultManagerID = defaultManagerID;
	}
	
	public int getServerUpdatedDate()
	{
		return serverUpdatedDate;
	}
	public void setServerUpdatedDate(int serverUpdatedDate)
	{
		this.serverUpdatedDate = serverUpdatedDate;
	}
	
	public int getLocalUpdatedDate()
	{
		return localUpdatedDate;
	}
	public void setLocalUpdatedDate(int localUpdatedDate)
	{
		this.localUpdatedDate = localUpdatedDate;
	}

	public static boolean[] getUsersCheck(List<User> userList, List<User> relevantUsers)
	{
		AppPreference appPreference = AppPreference.getAppPreference();
		
		boolean[] check = new boolean[userList.size()];
		for (int i = 0; i < userList.size(); i++)
		{
			if (userList.get(i).getServerID() == appPreference.getCurrentUserID())
			{
				check[i] = true;
			}
			else
			{
				check[i] = false;				
			}
		}
		
		if (relevantUsers == null)
		{
			return check;
		}
		
		for (int i = 0; i < userList.size(); i++)
		{
			check[i] = false;
			User user = userList.get(i);
			for (int j = 0; j < relevantUsers.size(); j++)
			{
				if (user.getServerID() == relevantUsers.get(j).getServerID())
				{
					check[i] = true;
				}
			}
		}
		return check;
	}

	public static String[] getUserNames(List<User> userList)
	{
		List<String> names = new ArrayList<String>();
		for (int i = 0; i < userList.size(); i++)
		{
			names.add(userList.get(i).getNickname());
		}
		return names.toArray(new String[names.size()]);		
	}

	public static String userListToString(List<User> userList)
	{
		String result = "";
		if (userList == null || userList.size() == 0)
		{
			return result;
		}
		
		for (int i = 0; i < userList.size(); i++)
		{
			result += userList.get(i).getNickname() + ",";
		}
		return result.substring(0, result.length()-1);
	}

	public static List<User> stringToUserList(String idString)
	{
		List<User> userList = new ArrayList<User>();
		DBManager dbManager = DBManager.getDBManager();
		List<Integer> idList = Utils.stringToIntList(idString);
		for (Integer integer : idList)
		{
			User user = dbManager.getUser(integer);
			if (user != null)
			{
				userList.add(user);				
			}
		}
		return userList;
	}
}
