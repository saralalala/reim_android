package classes;

import java.util.List;

public class StatisticsCategory
{
	private int categoryID = -1;
	private double amount = 0;
	private List<Integer> items = null;

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

	public List<Integer> getItems()
	{
		return items;
	}

	public void setItems(List<Integer> items)
	{
		this.items = items;
	}
}