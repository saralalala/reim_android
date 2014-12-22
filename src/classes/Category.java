package classes;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import classes.Utils.Utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class Category
{
	private int serverID = -1;
	private String name = "";
	private double limit = 0;
	private int groupID = -1;
	private int parentID = 0;
	private int iconID = -1;
	private String iconPath = "";
	private boolean isProveAhead = false;
	private int serverUpdatedDate = -1;
	private int localUpdatedDate = -1;

	public Category()
	{
		
	}
	
	public Category(JSONObject jObject)
	{
		try
		{
			setServerID(jObject.optInt("id", -1));
			setName(jObject.getString("category_name"));
			setLimit(jObject.getDouble("max_limit"));
			setGroupID(jObject.optInt("gid", -1));
			setParentID(jObject.optInt("pid", -1));
			setLocalUpdatedDate(jObject.getInt("lastdt"));
			setServerUpdatedDate(jObject.getInt("lastdt"));
			setIsProveAhead(Utils.intToBoolean(jObject.getInt("prove_before")));
			int iconID = jObject.optInt("avatar", -1);
			setIconID(iconID);
			setIconPath(Utils.getIconFilePath(iconID));
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
	
	public String getName()
	{
		return name;
	}
	public void setName(String name)
	{
		this.name = name;
	}

	public double getLimit()
	{
		return limit;
	}
	public void setLimit(double limit)
	{
		this.limit = limit;
	}

	public int getGroupID()
	{
		return groupID;
	}
	public void setGroupID(int groupID)
	{
		this.groupID = groupID;
	}
	
	public int getParentID()
	{
		return parentID;
	}
	public void setParentID(int parentID)
	{
		this.parentID = parentID;
	}

	public int getIconID()
	{
		return iconID;
	}
	public void setIconID(int iconID)
	{
		this.iconID = iconID;
	}
	
	public String getIconPath()
	{
		return iconPath;
	}
	public void setIconPath(String iconPath)
	{
		this.iconPath = iconPath;
	}
	
	public boolean isProveAhead()
	{
		return isProveAhead;
	}
	public void setIsProveAhead(boolean isProveAhead)
	{
		this.isProveAhead = isProveAhead;
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

	public static List<Boolean> getCategoryCheck(List<Category> categoryList, Category category)
	{	
		if (categoryList == null)
		{
			return null;
		}
		
		List<Boolean> check = new ArrayList<Boolean>();
		for (int i = 0; i < categoryList.size(); i++)
		{
			check.add(category != null && category.getServerID() == categoryList.get(i).getServerID());
		}
		return check;
	}
	
	public static String[] getCategoryNames(List<Category> categoryList)
	{
		List<String> names = new ArrayList<String>();
		for (int i = 0; i < categoryList.size(); i++)
		{
			String max = categoryList.get(i).getLimit() == 0 ? "(MAX:Unlimited)" : "(MAX:￥" + categoryList.get(i).getLimit() + ")";
			names.add(categoryList.get(i).getName() + max);	
		}
		return names.toArray(new String[names.size()]);
	}

	public boolean hasUndownloadedIcon()
	{
		if (getIconPath().equals("") && getIconID() > 0)
		{
			return true;
		}
		
		if (!getIconPath().equals(""))
		{
			Bitmap bitmap = BitmapFactory.decodeFile(getIconPath());
			if (bitmap == null)
			{
				return true;
			}
		}
		return false;
	}
}