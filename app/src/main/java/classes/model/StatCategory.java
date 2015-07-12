package classes.model;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

import classes.utils.PhoneUtils;

public class StatCategory
{
    private int categoryID = -1;
    private double amount = 0;
    private int itemCount = 0;
    private int iconID = -1;
    private String name = "";
    private int color = -1;

    public StatCategory()
    {

    }

    public StatCategory(JSONObject jObject)
    {
        try
        {
            setCategoryID(jObject.getInteger("id"));
            setAmount(jObject.getDouble("amount"));
            JSONArray iids = jObject.getJSONArray("items");
            int count = iids != null ? iids.size() : jObject.getInteger("count");
            setItemCount(count);
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    public int getCategoryID()
    {
        return categoryID;
    }
    public void setCategoryID(int categoryID)
    {
        this.categoryID = categoryID;
    }

    public double getAmount()
    {
        return amount;
    }
    public void setAmount(double amount)
    {
        this.amount = amount;
    }

    public int getItemCount()
    {
        return itemCount;
    }
    public void setItemCount(int itemCount)
    {
        this.itemCount = itemCount;
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

    public String getName()
    {
        return name;
    }
    public void setName(String name)
    {
        this.name = name;
    }

    public int getColor()
    {
        return color;
    }
    public void setColor(int color)
    {
        this.color = color;
    }
}