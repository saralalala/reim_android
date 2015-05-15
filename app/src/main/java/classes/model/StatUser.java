package classes.model;

import org.json.JSONException;
import org.json.JSONObject;

public class StatUser
{
	private int userID = -1;
	private double amount = 0;
    private int itemCount =  -1;

    public StatUser(JSONObject jObject)
    {
        try
        {
            setUserID(jObject.getInt("id"));
            setAmount(jObject.getDouble("amount"));
            setItemCount(jObject.getInt("count"));
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