package classes;

import java.util.ArrayList;
import java.util.List;

import database.DBManager;

public class Tag
{
	private int serverID = -1;
	private int groupID = -1;
	private String name = "";
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
	
	public int getGroupID()
	{
		return groupID;
	}
	public void setGroupID(int groupID)
	{
		this.groupID = groupID;
	}
	
	public String getName()
	{
		return name;
	}
	public void setName(String name)
	{
		this.name = name;
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

	public static boolean[] getTagsCheck(List<Tag> tagList, List<Tag> currentTags)
	{
		boolean[] check = new boolean[tagList.size()];
		if (currentTags == null)
		{
			for (int i = 0; i < tagList.size(); i++)
			{
				check[i] = false;
			}
			return check;
		}
		
		for (int i = 0; i < tagList.size(); i++)
		{
			check[i] = false;
			Tag tag = tagList.get(i);
			for (int j = 0; j < currentTags.size(); j++)
			{
				if (tag.getServerID() == currentTags.get(j).getServerID())
				{
					check[i] = true;
				}
			}
		}
		return check;
	}

	public static String[] getTagNames(List<Tag> tagList)
	{
		List<String> names = new ArrayList<String>();
		for (int i = 0; i < tagList.size(); i++)
		{
			names.add(tagList.get(i).getName());
		}
		return names.toArray(new String[names.size()]);		
	}

	public static String tagListToString(List<Tag> tagList)
	{
		String result = "";
		if (tagList == null || tagList.size() == 0)
		{
			return result;
		}
		
		for (int i = 0; i < tagList.size(); i++)
		{
			result += tagList.get(i).getName() + ",";
		}
		return result.substring(0, result.length()-1);
	}
	
	public static List<Tag> stringToTagList(String idString)
	{
		List<Tag> tagList = new ArrayList<Tag>();
		DBManager dbManager = DBManager.getDBManager();
		while (idString.indexOf(',') != -1)
		{
			int index = idString.indexOf(',');
			int serverID = Integer.valueOf(idString.substring(0, index));
			Tag tag = dbManager.getTag(serverID);
			tagList.add(tag);
			idString = idString.substring(index+1);
		}
		
		if (idString.length() != 0)
		{
			int serverID = Integer.valueOf(idString);
			Tag tag = dbManager.getTag(serverID);
			tagList.add(tag);
		}
		return tagList;
	}
	
	public boolean equals(Object o)
	{
		if (o instanceof Tag)
		{
			Tag tag = (Tag)o;
			return tag.getServerID() == this.getServerID();
		}
		return super.equals(o);
	}	
}
