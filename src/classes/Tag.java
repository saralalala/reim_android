package classes;

import java.util.ArrayList;
import java.util.List;

import android.text.TextUtils;

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

	public static String[] getTagsName(List<Tag> tagList)
	{
		List<String> names = new ArrayList<String>();
		for (int i = 0; i < tagList.size(); i++)
		{
			names.add(tagList.get(i).getName());
		}
		return names.toArray(new String[names.size()]);		
	}

	public static String getTagsNameString(List<Tag> tagList)
	{
		if (tagList == null || tagList.size() == 0)
		{
			return "";
		}
		
		String[] tagNames = new String[tagList.size()];
		for (int i = 0; i < tagList.size(); i++)
		{
			tagNames[i] = tagList.get(i).getName();
		}
		return TextUtils.join(",", tagNames);
	}	
	
	public static String getTagsIDString(List<Tag> tagList)
	{
		if (tagList == null || tagList.size() == 0)
		{
			return "";
		}
		
		Integer[] tagIDs = new Integer[tagList.size()];
		for (int i = 0; i < tagList.size(); i++)
		{
			tagIDs[i] = tagList.get(i).getServerID();
		}
		
		return TextUtils.join(",", tagIDs);
	}
	
	public static List<Tag> stringToTagList(String idString)
	{
		List<Tag> tagList = new ArrayList<Tag>();
		DBManager dbManager = DBManager.getDBManager();
		List<Integer> idList = Utils.stringToIntList(idString);
		for (Integer integer : idList)
		{
			Tag tag = dbManager.getTag(integer);
			if (tag != null)
			{
				tagList.add(tag);				
			}
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
