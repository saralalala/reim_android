package classes.model;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

public class StatUser
{
    private int userID = -1;
    private double amount = 0;
    private int itemCount = 0;

    public StatUser(JSONObject jObject)
    {
        try
        {
            setUserID(jObject.getInteger("id"));
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

    public int getUserID()
    {
        return userID;
    }
    public void setUserID(int userID)
    {
        this.userID = userID;
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