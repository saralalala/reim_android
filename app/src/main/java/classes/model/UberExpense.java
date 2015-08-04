package classes.model;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

import java.io.Serializable;

import classes.utils.Utils;

public class UberExpense implements Serializable
{
    private static final long serialVersionUID = 1L;

    private int id;
    private String productID = "";
    private int time = -1;
    private double amount = 0;
    private String city = "";
    private double startLatitude = 0;
    private double startLongitude = 0;
    private String currencyCode = "CNY";
    private boolean isUsed = false;
    private boolean isCompleted = false;

    public UberExpense(JSONObject jObject)
    {
        try
        {
            JSONObject cityObject = jObject.getJSONObject("start_city");
            setId(jObject.getInteger("id"));
            setProductID(jObject.getString("product_id"));
            setTime(jObject.getInteger("start_time"));
            setCity(cityObject.getString("display_name"));
            setStartLatitude(cityObject.getDouble("latitude"));
            setStartLongitude(cityObject.getDouble("longitude"));
            setCurrencyCode(jObject.getString("currency_code"));
            setIsUsed(Utils.intToBoolean(jObject.getInteger("used")));
            setIsCompleted(jObject.getString("status").equals("completed"));
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    public int getId()
    {
        return id;
    }
    public void setId(int id)
    {
        this.id = id;
    }

    public String getProductID()
    {
        return productID;
    }
    public void setProductID(String productID)
    {
        this.productID = productID;
    }

    public int getTime()
    {
        return time;
    }
    public void setTime(int time)
    {
        this.time = time;
    }

    public double getAmount()
    {
        return amount;
    }
    public void setAmount(double amount)
    {
        this.amount = amount;
    }

    public String getCity()
    {
        return city;
    }
    public void setCity(String city)
    {
        this.city = city;
    }

    public double getStartLatitude()
    {
        return startLatitude;
    }
    public void setStartLatitude(double startLatitude)
    {
        this.startLatitude = startLatitude;
    }

    public double getStartLongitude()
    {
        return startLongitude;
    }
    public void setStartLongitude(double startLongitude)
    {
        this.startLongitude = startLongitude;
    }

    public String getCurrencyCode()
    {
        return currencyCode;
    }
    public void setCurrencyCode(String currencyCode)
    {
        this.currencyCode = currencyCode;
    }

    public boolean isUsed()
    {
        return isUsed;
    }
    public void setIsUsed(boolean isUsed)
    {
        this.isUsed = isUsed;
    }

    public boolean isCompleted()
    {
        return isCompleted;
    }
    public void setIsCompleted(boolean isCompleted)
    {
        this.isCompleted = isCompleted;
    }
}