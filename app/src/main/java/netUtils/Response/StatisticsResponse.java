package netUtils.Response;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import classes.StatisticsCategory;

public class StatisticsResponse extends BaseResponse
{
	private double newAmount;
	private double ongoingAmount;
	private List<StatisticsCategory> statCategoryList;
	private HashMap<String, Double> monthsData;
	
	public StatisticsResponse(Object httpResponse)
	{
		super(httpResponse);
	}

	protected void constructData()
	{
		try
		{
			JSONObject jObject = getDataObject();

			this.ongoingAmount = jObject.getDouble("process");
			this.newAmount = jObject.getDouble("new");
			
			JSONArray cates = jObject.getJSONArray("cates");
			this.statCategoryList = new ArrayList<StatisticsCategory>();
			for (int i = 0; i < cates.length(); i++)
			{
				JSONObject object = cates.getJSONObject(i);				
				StatisticsCategory category = new StatisticsCategory();
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

	public double getTotalAmount()
	{
		return newAmount + ongoingAmount;
	}

	public double getNewAmount()
	{
		return newAmount;
	}

	public double getOngoingAmount()
	{
		return ongoingAmount;
	}

	public List<StatisticsCategory> getStatCategoryList()
	{
		return statCategoryList;
	}

	public HashMap<String, Double> getMonthsData()
	{
		return monthsData;
	}
}