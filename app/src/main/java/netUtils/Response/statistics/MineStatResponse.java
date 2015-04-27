package netUtils.response.statistics;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import classes.base.StatCategory;
import classes.utils.Utils;
import netUtils.response.BaseResponse;

public class MineStatResponse extends BaseResponse
{
    private boolean hasStaffData;
	private double newAmount;
	private double ongoingAmount;
	private List<StatCategory> statCategoryList;
	private HashMap<String, Double> monthsData;
	
	public MineStatResponse(Object httpResponse)
	{
		super(httpResponse);
	}

	protected void constructData()
	{
		try
		{
			JSONObject jObject = getDataObject();

            this.hasStaffData = Utils.intToBoolean(jObject.getInt("staff"));
			this.ongoingAmount = jObject.getDouble("process");
			this.newAmount = jObject.getDouble("new");
			
			JSONArray cates = jObject.getJSONArray("cates");
			this.statCategoryList = new ArrayList<StatCategory>();
			for (int i = 0; i < cates.length(); i++)
			{
				JSONObject object = cates.getJSONObject(i);				
				StatCategory category = new StatCategory();
				category.setCategoryID(object.getInt("id"));
				category.setAmount(object.getDouble("amount"));
				  
				List<Integer> itemIDList = new ArrayList<Integer>();
				JSONArray iids = object.getJSONArray("items");
				for (int j = 0; j < iids.length(); j++)
				{
					itemIDList.add(iids.getInt(j));
				}
				category.setItems(itemIDList);
				
				this.statCategoryList.add(category);
			}
			
			JSONObject months = jObject.optJSONObject("ms");
			this.monthsData = new HashMap<String, Double>();
			if (months != null)
			{
				for (Iterator<?> iterator = months.keys(); iterator.hasNext();)
				{
					String key = (String) iterator.next();
					Double value = months.getDouble(key);
					this.monthsData.put(key, value);
				}				
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

    public boolean hasStaffData()
    {
        return hasStaffData;
    }

    public double getNewAmount()
	{
		return newAmount;
	}

	public double getOngoingAmount()
	{
		return ongoingAmount;
	}

	public List<StatCategory> getStatCategoryList()
	{
		return statCategoryList;
	}

	public HashMap<String, Double> getMonthsData()
	{
		return monthsData;
	}
}