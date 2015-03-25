package classes;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class StatCategory
{
	private int categoryID = -1;
	private double amount = 0;
    private int itemCount =  -1;
	private List<Integer> items = null;

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
}