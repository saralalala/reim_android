package classes.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.GregorianCalendar;

import classes.utils.Utils;

public class DidiExpense implements Serializable
{
    private static final long serialVersionUID = 1L;

    public static final int TYPE_TAXI = 0;
    public static final int TYPE_ZHUAN_CHE = 2;
    public static final int TYPE_KUAI_CHE = 4;
    public static final int TYPE_LIFT = -1;

    private int id;
    private String orderID = "";
    private String time = "";
    private String start = "";
    private String destination = "";
    private double amount = 0;
    private String city = "";
    private int type = TYPE_TAXI;
    private boolean isUsed = false;

    public DidiExpense(JSONObject jObject)
    {
        try
        {
            setId(jObject.getInt("id"));
            setOrderID(jObject.getString("oid"));
            setTime(jObject.getString("setuptime"));
            setStart(jObject.getString("fromAddress"));
            setDestination(jObject.getString("toAddress"));
            setType(jObject.optInt("product_type", 0));
            setUsed(Utils.intToBoolean(jObject.getInt("used")));
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

        GregorianCalendar greCal = new GregorianCalendar(year, month - 1, day, hour, minute, second);
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

    public String getDestination()
    {
        return destination;
    }
    public void setDestination(String destination)
    {
        this.destination = destination;
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

    public int getType()
    {
        return type;
    }
    public void setType(int type)
    {
        this.type = type;
    }

    public boolean isUsed()
    {
        return isUsed;
    }
    public void setUsed(boolean isUsed)
    {
        this.isUsed = isUsed;
    }

}
