package classes;

import java.util.HashMap;
import java.util.List;

public class StatisticsCategory
{
	private int _cateid = -1;
	private double amount = 0;
	private List<Integer> items = null;


	public int get_cateid()
	{
		return _cateid;
	}

	public void set_cateid(int _cateid)
	{
		this._cateid = _cateid;
	}

	public double getAmount()
	{
		return amount;
	}

	public void setAmount(double amount)
	{
		this.amount = amount;
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
