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

	private double _new_amount = 0;
	private double _process_amount = 0;
	private double _done_amount = 0;
	private List<StatisticsCategory> _sc = null;
	private HashMap<String, String> _ms = null;

	public HashMap<String, String> get_ms()
	{
		return _ms;
	}

	public void set_ms(HashMap<String, String> _ms)
	{
		this._ms = _ms;
	}

	public StatisticsResponse(Object httpResponse)
	{
		super(httpResponse);
		if (getStatus())
		{
			constructData();
		}
	}

	protected void constructData()
	{
		try
		{
			JSONObject jObject = getDataObject();
			this._done_amount = jObject.getDouble("done");
			this._process_amount = jObject.getDouble("process");
			this._new_amount = jObject.getDouble("new");
			JSONArray cates = jObject.getJSONArray("cates");
			this._sc = new LinkedList<StatisticsCategory>();
			for (int i = 0; i < cates.length(); i++)
			{
				JSONObject item = cates.getJSONObject(i);
				StatisticsCategory object = new StatisticsCategory();
				object.set_cateid(item.getInt("id"));
				object.setAmount(item.getDouble("amount"));
				List<Integer> _items = new LinkedList<Integer>();
				JSONArray iids = item.getJSONArray("items");
				for (int j = 0; j < iids.length(); j++)
				{
					_items.add(iids.getInt(j));
				}
				object.setItems(_items);
				this._sc.add(object);
			}
			JSONObject _month_data = jObject.getJSONObject("ms");
			this._ms = new HashMap<String, String>();
			for (Iterator<?> iter = _month_data.keys(); iter.hasNext();)
			{
				String str = (String) iter.next();
				String val = String.valueOf(_month_data.getLong(str));
				this._ms.put(str, val);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public double get_total()
	{
		return this._done_amount + this._new_amount + this._process_amount;
	}

	public double get_new_amount()
	{
		return _new_amount;
	}

	public void set_new_amount(double _new_amount)
	{
		this._new_amount = _new_amount;
	}

	public double get_process_amount()
	{
		return _process_amount;
	}

	public void set_process_amount(double _process_amount)
	{
		this._process_amount = _process_amount;
	}

	public double get_done_amount()
	{
		return _done_amount;
	}

	public void set_done_amount(double _done_amount)
	{
		this._done_amount = _done_amount;
	}

	public List<StatisticsCategory> get_sc()
	{
		return _sc;
	}

	public void set_sc(List<StatisticsCategory> _sc)
	{
		this._sc = _sc;
	}

}
