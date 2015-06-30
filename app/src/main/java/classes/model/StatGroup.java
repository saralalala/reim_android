package classes.model;

import org.json.JSONException;
import org.json.JSONObject;

public class StatGroup
{
    private int groupID = -1;
    private String name = "";
    private double amount = 0;

    public StatGroup(JSONObject jObject)
    {
        try
        {
            setGroupID(jObject.getInt("id"));
            setName(jObject.getString("name"));
            setAmount(jObject.getDouble("amount"));
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
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

    public double getAmount()
    {
        return amount;
    }
    public void setAmount(double amount)
    {
        this.amount = amount;
    }
}