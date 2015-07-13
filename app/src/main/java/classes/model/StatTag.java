package classes.model;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

public class StatTag
{
    private int tagID = -1;
    private double amount = 0;
    private int itemCount = 0;

    public StatTag(JSONObject jObject)
    {
        try
        {
            setTagID(jObject.getInteger("id"));
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