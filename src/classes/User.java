package classes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;

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
	private boolean isActive = false;
	private boolean isAdmin = false;
	private int groupID = -1;
	private int defaultManagerID = -1;
	private int serverUpdatedDate = -1;
	private int localUpdatedDate = -1;
	
	public User()
	{
		
	}
	
	public User(JSONObject jObject, int groupID)
	{
		try
		{
			setServerID(Integer.valueOf(jObject.getString("id")));
			setEmail(jObject.getString("email"));
			setPhone(jObject.getString("phone"));
			setNickname(jObject.getString("nickname"));
			setDefaultManagerID(jObject.getInt("manager_id"));
			setAvatarPath("");
			setIsAdmin(Utils.intToBoolean(jObject.getInt("admin")));
			setGroupID(groupID);
			setLocalUpdatedDate(jObject.getInt("dt"));
			setServerUpdatedDate(jObject.getInt("dt"));
			String imageID = jObject.getString("avatar");
			if (imageID.equals(""))
			{
				setImageID(-1);					
			}
			else
			{
				setImageID(Integer.valueOf(imageID));
			}
		}
		catch (NumberFormatException e)
		{
			e.printStackTrace();
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}		
	}
	
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
	
	public boolean isActive()
	{
		return isActive;
	}
	public void setIsActive(boolean isActive)
	{
		this.isActive = isActive;
	}
	
	public boolean isAdmin()
	{
		return isAdmin;
	}
	public void setIsAdmin(boolean isAdmin)
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

	public boolean hasUndownloadedAvatar()
	{
		return getAvatarPath().equals("") && getImageID() != -1 && getImageID() != 0;
	}
	
	public static boolean[] getUsersCheck(List<User> allUsers, List<User> targetUsers)
	{		
		boolean[] check = new boolean[allUsers.size()];
		for (int i = 0; i < check.length; i++)
		{
			check[i] = false;
		}
		
		if (targetUsers == null)
		{
			return check;
		}
		
		for (int i = 0; i < allUsers.size(); i++)
		{
			User user = allUsers.get(i);
			for (int j = 0; j < targetUsers.size(); j++)
			{
				if (user.getServerID() == targetUsers.get(j).getServerID())
				{
					check[i] = true;
				}
			}
		}
		return check;
	}

	public static String[] getUsersName(List<User> userList)
	{
		String[] userNames = new String[userList.size()];
		for (int i = 0; i < userList.size(); i++)
		{
			userNames[i] = userList.get(i).getNickname();
		}
		return userNames;
	}

	public static String getUsersNameString(List<User> userList)
	{
		if (userList == null || userList.size() == 0)
		{
			return "";
		}
		
		return TextUtils.join(",", getUsersName(userList));
	}
	
	public static String getUsersIDString(List<User> userList)
	{
		if (userList == null || userList.size() == 0)
		{
			return "";
		}
		
		Integer[] userIDs = new Integer[userList.size()];
		for (int i = 0; i < userList.size(); i++)
		{
			userIDs[i] = userList.get(i).getServerID();
		}
		
		return TextUtils.join(",", userIDs);
	}

	public static List<User> idStringToUserList(String idString)
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

	public static List<User> removeCurrentUserFromList(List<User> userList)
	{
		if (userList == null)
		{
			return new ArrayList<User>();
		}
		
		int currentUserID = AppPreference.getAppPreference().getCurrentUserID();
		List<User> tempList = new ArrayList<User>();
		for (User user : userList)
		{
			if (user.getServerID() != currentUserID)
			{
				tempList.add(user);
			}
		}
		
		return tempList;
	}
}
