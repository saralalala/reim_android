package classes.model;

import java.util.List;

public class Package
{
	private int id;
	private String name;
	private int number;
	private int createdDate;
	private double amount;
	private int status;
	private List<Item> includedItems;
	
	public int getId()
	{
		return id;
	}
	public void setId(int id)
	{
		this.id = id;
	}
	public String getName()
	{
		return name;
	}
	public void setName(String name)
	{
		this.name = name;
	}
	public int getNumber()
	{
		return number;
	}
	public void setNumber(int number)
	{
		this.number = number;
	}
	public int getCreatedDate()
	{
		return createdDate;
	}
	public void setCreatedDate(int createdDate)
	{
		this.createdDate = createdDate;
	}
	public double getAmount()
	{
		return amount;
	}
	public void setAmount(double amount)
	{
		this.amount = amount;
	}
	public int getStatus()
	{
		return status;
	}
	public void setStatus(int status)
	{
		this.status = status;
	}
	public List<Item> getIncludedItems()
	{
		return includedItems;
	}
	public void setIncludedItems(List<Item> includedItems)
	{
		this.includedItems = includedItems;
	}
}
