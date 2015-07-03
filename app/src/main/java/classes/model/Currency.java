package classes.model;

import com.rushucloud.reim.R;

import java.util.List;

import classes.utils.ViewUtils;

public class Currency
{
	private String code = "CNY";
	private String symbol = "";
    private double rate = 0;

    public Currency()
    {

    }

	public Currency(String code, String symbol, double rate)
	{
		this.code = code;
		this.symbol = symbol;
        this.rate = rate;
	}

	public String getName()
	{
        int nameID = -1;
        switch (code)
        {
            case "CNY":
                nameID = R.string.currency_cny;
                break;
            case "USD":
                nameID = R.string.currency_usd;
                break;
            case "EUR":
                nameID = R.string.currency_eur;
                break;
            case "HKD":
                nameID = R.string.currency_hkd;
                break;
            case "MOP":
                nameID = R.string.currency_mop;
                break;
            case "TWD":
                nameID = R.string.currency_twd;
                break;
            case "JPY":
                nameID = R.string.currency_jpy;
                break;
            case "KER":
                nameID = R.string.currency_ker;
                break;
            case "GBP":
                nameID = R.string.currency_gbp;
                break;
            case "RUB":
                nameID = R.string.currency_rub;
                break;
            case "SGD":
                nameID = R.string.currency_sgd;
                break;
            case "PHP":
                nameID = R.string.currency_php;
                break;
            case "IDR":
                nameID = R.string.currency_idr;
                break;
            case "MYR":
                nameID = R.string.currency_myr;
                break;
            case "THB":
                nameID = R.string.currency_thb;
                break;
            case "CAD":
                nameID = R.string.currency_cad;
                break;
            case "AUD":
                nameID = R.string.currency_aud;
                break;
            case "NZD":
                nameID = R.string.currency_nzd;
                break;
            case "CHF":
                nameID = R.string.currency_chf;
                break;
            case "DKK":
                nameID = R.string.currency_dkk;
                break;
            case "NOK":
                nameID = R.string.currency_nok;
                break;
            case "SEK":
                nameID = R.string.currency_sek;
                break;
            case "BRL":
                nameID = R.string.currency_brl;
                break;
        }

		return nameID == -1 ? "" : ViewUtils.getString(nameID);
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

    public double getRate()
    {
        return rate;
    }
    public void setRate(double rate)
    {
        this.rate = rate;
    }

    public boolean equals(Object o)
    {
        if (o == null)
        {
            return false;
        }

        if (o instanceof Currency)
        {
            Currency currency = (Currency) o;
            return currency.getCode().equals(this.getCode());
        }
        return super.equals(o);
    }

    public boolean isCNY()
    {
        return getCode().equals("CNY");
    }

    public static String[] listToArray(List<Currency> currencyList)
    {
        String[] result = new String[currencyList.size()];
        for (int i = 0; i < currencyList.size(); i++)
        {
            Currency currency = currencyList.get(i);
            result[i] = currency.getName() + "              (" + currency.getCode() + ")";
        }
        return result;
    }
}