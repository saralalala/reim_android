package classes.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import classes.utils.PhoneUtils;

public class StatCategory
{
    private int categoryID = -1;
    private double amount = 0;
    private int itemCount = -1;
    private List<Integer> items = null;
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
            setCategoryID(jObject.getInt("id"));
            setAmount(jObject.getDouble("amount"));
            setItemCount(jObject.getInt("count"));
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

    public List<Integer> getItems()
    {
        return items;
    }

    public void setItems(List<Integer> items)
    {
        this.items = items;
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