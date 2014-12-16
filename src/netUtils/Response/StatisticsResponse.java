package netUtils.Response;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import classes.StatisticsCategory;

public class StatisticsResponse extends BaseResponse
{
	private double newAmount;
	private double ongoingAmount;
	private double doneAmount;
	private List<StatisticsCategory> statCategoryList;
	private HashMap<String, String> monthsData;
	
	public StatisticsResponse(Object httpResponse)
	{
		super(httpResponse);
	}

	protected void constructData()
	{
		try
		{
			JSONObject jObject = getDataObject();
			this.doneAmount = jObject.getDouble("done");
			this.ongoingAmount = jObject.getDouble("process");
			this.newAmount = jObject.getDouble("new");
			JSONArray cates = jObject.getJSONArray("cates");
			this.statCategoryList = new LinkedList<StatisticsCategory>();
			for (int i = 0; i < cates.length(); i++)
			{
				JSONObject item = cates.getJSONObject(i);
				StatisticsCategory object = new StatisticsCategory();
				object.setCategoryID(item.getInt("id"));
				object.setAmount(item.getDouble("amount"));
				List<Integer> _items = new LinkedList<Integer>();
				JSONArray iids = item.getJSONArray("items");
				for (int j = 0; j < iids.length(); j++)
				{
					_items.add(iids.getInt(j));
				}
				object.setItems(_items);
				this.statCategoryList.add(object);
			}
			JSONObject months = jObject.optJSONObject("ms");
			this.monthsData = new HashMap<String, String>();
			if (months != null)
			{
				for (Iterator<?> iterator = months.keys(); iterator.hasNext();)
				{
					String key = (String) iterator.next();
					String value = String.valueOf(months.getLong(key));
					this.monthsData.put(key, value);
				}				
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public double getTotal()
	{
		return doneAmount + newAmount + ongoingAmount;
	}

	public double getNewAmount()
	{
		return newAmount;
	}

	public void setNewAmount(double newAmount)
	{
		this.newAmount = newAmount;
	}

	public double getOngoingAmount()
	{
		return ongoingAmount;
	}

	public void setOngoingAmount(double ongoingAmount)
	{
		this.ongoingAmount = ongoingAmount;
	}

	public double getDoneAmount()
	{
		return doneAmount;
	}

	public void setDoneAmount(double doneAmount)
	{
		this.doneAmount = doneAmount;
	}

	public List<StatisticsCategory> getStatCategoryList()
	{
		return statCategoryList;
	}

	public void setStatCategoryList(List<StatisticsCategory> statCategoryList)
	{
		this.statCategoryList = statCategoryList;
	}

	public HashMap<String, String> getMonthsData()
	{
		return monthsData;
	}

	public void setMonthsData(HashMap<String, String> monthsData)
	{
		this.monthsData = monthsData;
	}
}