package classes.model;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import classes.utils.PhoneUtils;

public class Category implements Serializable
{
    private static final long serialVersionUID = 1L;

    public static final int TYPE_REIM = 0;
    public static final int TYPE_BUDGET = 1;
    public static final int TYPE_BORROWING = 2;

    private int serverID = -1;
    private String name = "";
    private double limit = 0;
    private int groupID = -1;
    private int parentID = 0;
    private int setOfBookID = 0;
    private int iconID = -1;
    private int type = TYPE_REIM;
    private int serverUpdatedDate = -1;
    private int localUpdatedDate = -1;

    public Category()
    {

    }

    public Category(Category category)
    {
        setServerID(category.getServerID());
        setName(category.getName());
        setLimit(category.getLimit());
        setGroupID(category.getGroupID());
        setParentID(category.getParentID());
        setSetOfBookID(category.getSetOfBookID());
        setLocalUpdatedDate(category.getLocalUpdatedDate());
        setServerUpdatedDate(category.getServerUpdatedDate());
        setType(category.getType());
        setIconID(category.getIconID());
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
            setSetOfBookID(jObject.optInt("sob_id", 0));
            setLocalUpdatedDate(jObject.getInt("lastdt"));
            setServerUpdatedDate(jObject.getInt("lastdt"));
            setType(jObject.getInt("prove_before"));
            setIconID(jObject.optInt("avatar", -1));
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

    public int getSetOfBookID()
    {
        return setOfBookID;
    }
    public void setSetOfBookID(int setOfBookID)
    {
        this.setOfBookID = setOfBookID;
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
        return iconID == -1 || iconID == 0 ? "" : PhoneUtils.getIconFilePath(iconID);
    }

    public int getType()
    {
        return type;
    }
    public void setType(int type)
    {
        this.type = type;
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

    public boolean equals(Object o)
    {
        if (o == null)
        {
            return false;
        }

        if (o instanceof Category)
        {
            Category category = (Category) o;
            return category.getName().equals(this.getName());
        }
        return super.equals(o);
    }

    public static List<Boolean> getCategoryCheck(List<Category> categoryList, Category category)
    {
        if (categoryList == null)
        {
            return null;
        }

        List<Boolean> check = new ArrayList<>();
        for (int i = 0; i < categoryList.size(); i++)
        {
            check.add(category != null && category.getServerID() == categoryList.get(i).getServerID());
        }
        return check;
    }

    public boolean hasUndownloadedIcon()
    {
        if (getIconPath().isEmpty() && getIconID() > 0)
        {
            return true;
        }

        if (!getIconPath().isEmpty())
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