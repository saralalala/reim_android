package classes.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.GregorianCalendar;

public class DidiExpense implements Serializable
{
    private static final long serialVersionUID = 1L;

    private String orderID = "";
    private String time = "";
    private String start = "";
    private String destionation = "";
    private double amount = 0;

    public DidiExpense(JSONObject jObject)
    {
        try
        {
            setOrderID(jObject.getString("orderid"));
            setTime(jObject.getString("setuptime"));
            setStart(jObject.getString("fromAddress"));
            setDestionation(jObject.getString("toAddress"));
            setAmount(jObject.optDouble("amount", 0));
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    public String getOrderID()
    {
        return orderID;
    }
    public void setOrderID(String orderID)
    {
        this.orderID = orderID;
    }

    public String getTime()
    {
        return time;
    }
    public void setTime(String time)
    {
        this.time = time;
    }
    public int getTimeStamp()
    {
        int year = Integer.valueOf(time.substring(0, 4));
        int month = Integer.valueOf(time.substring(5, 7));
        int day = Integer.valueOf(time.substring(8, 10));
        int hour = Integer.valueOf(time.substring(11, 13));
        int minute = Integer.valueOf(time.substring(14, 16));
        int second = Integer.valueOf(time.substring(17));

        GregorianCalendar greCal = new GregorianCalendar(year, month, day, hour, minute, second);
        return (int) (greCal.getTimeInMillis() / 1000);
    }

    public String getStart()
    {
        return start;
    }
    public void setStart(String start)
    {
        this.start = start;
    }

    public String getDestionation()
    {
        return destionation;
    }
    public void setDestionation(String destionation)
    {
        this.destionation = destionation;
    }

    public double getAmount()
    {
        return amount;
    }
    public void setAmount(double amount)
    {
        this.amount = amount;
    }
}
