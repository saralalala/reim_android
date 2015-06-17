package classes.model;

import classes.utils.ViewUtils;

public class Currency
{
	private String name = "";
	private String code = "";
	private String symbol = "";

	public Currency(int nameID, String code, String symbol)
	{
		this.name = ViewUtils.getString(nameID);
		this.code = code;
		this.symbol = symbol;
	}

	public String getName()
	{
		return name;
	}
	public void setName(String name)
	{
		this.name = name;
	}

	public String getCode()
	{
		return code;
	}
	public void setCode(String code)
	{
		this.code = code;
	}

	public String getSymbol()
	{
		return symbol;
	}
	public void setSymbol(String symbol)
	{
		this.symbol = symbol;
	}
}