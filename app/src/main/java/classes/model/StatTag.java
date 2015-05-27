package classes.model;

import org.json.JSONException;
import org.json.JSONObject;

public class StatTag
{
    private int tagID = -1;
    private double amount = 0;
    private int itemCount = -1;

    public StatTag(JSONObject jObject)
    {
        try
        {
            setTagID(jObject.getInt("id"));
            setAmount(jObject.getDouble("amount"));
            setItemCount(jObject.getInt("count"));
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    public int getTagID()
    {
        return tagID;
    }

    public void setTagID(int tagID)
    {
        this.tagID = tagID;
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
}