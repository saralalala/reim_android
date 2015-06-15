package classes.model;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import classes.utils.DBManager;
import classes.utils.PhoneUtils;
import classes.utils.Utils;

public class Tag implements Serializable
{
    private static final long serialVersionUID = 1L;

    private int serverID = -1;
    private int groupID = -1;
    private String name = "";
    private int iconID = -1;
    private String iconPath = "";
    private int serverUpdatedDate = -1;
    private int localUpdatedDate = -1;

    public Tag()
    {

    }

    public Tag(JSONObject jObject)
    {
        try
        {
            setServerID(jObject.optInt("id", -1));
            setName(jObject.getString("name"));
            setGroupID(jObject.optInt("gid", -1));
            setLocalUpdatedDate(jObject.getInt("lastdt"));
            setServerUpdatedDate(jObject.getInt("lastdt"));
            int iconID = jObject.optInt("avatar", -1);
            setIconID(iconID);
            if (iconID != -1)
            {
                setIconPath(PhoneUtils.getIconFilePath(iconID));
            }
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

    public static boolean[] getTagsCheck(List<Tag> allTags, List<Tag> targetTags)
    {
        if (allTags == null || allTags.isEmpty())
        {
            return null;
        }

        boolean[] check = new boolean[allTags.size()];
        if (targetTags == null)
        {
            for (int i = 0; i < allTags.size(); i++)
            {
                check[i] = false;
            }
            return check;
        }

        for (int i = 0; i < allTags.size(); i++)
        {
            check[i] = false;
            Tag tag = allTags.get(i);
            for (int j = 0; j < targetTags.size(); j++)
            {
                if (tag.getServerID() == targetTags.get(j).getServerID())
                {
                    check[i] = true;
                    break;
                }
            }
        }
        return check;
    }

    public static String getTagsIDString(List<Tag> tagList)
    {
        if (tagList == null || tagList.isEmpty())
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

    public static List<Tag> idStringToTagList(String idString)
    {
        List<Tag> tagList = new ArrayList<>();
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

    public boolean hasUndownloadedIcon()
    {
        if (getIconPath().isEmpty() && getIconID() != -1 && getIconID() != 0)
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

    public boolean equals(Object o)
    {
        if (o instanceof Tag)
        {
            Tag tag = (Tag) o;
            return tag.getServerID() == this.getServerID();
        }
        return super.equals(o);
    }
}