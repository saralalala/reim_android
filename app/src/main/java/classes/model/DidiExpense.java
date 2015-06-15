package classes.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.GregorianCalendar;

public class DidiExpense
{
    private String time = "";
    private String start = "";
    private String destionation = "";
    private double price = 0;

    public DidiExpense()
    {

    }

    public DidiExpense(JSONObject jObject)
    {
        try
        {
            JSONObject orderObject = jObject.getJSONObject("order");
            JSONObject priceObject = jObject.getJSONObject("price");

            setTime(orderObject.getString("departure_time"));

            String startName = orderObject.getString("start_name");
            String startAddress = orderObject.getString("start_address");
            startAddress = startAddress.contains(startName)? startAddress : startAddress + startName;
            setStart(startAddress);

            String endName = orderObject.getString("end_name");
            String endAddress = orderObject.getString("end_address");
            endAddress = endAddress.contains(endName)? endAddress : endAddress + endName;
            setDestionation(endAddress);

            setPrice(priceObject.getDouble("start_price") + priceObject.getDouble("normal_fee"));
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
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
        int hour = Integer.valueOf(time.substring(12, 14));
        int minute = Integer.valueOf(time.substring(15, 17));
        int second = Integer.valueOf(time.substring(18, 20));

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

    public double getPrice()
    {
        return price;
    }
    public void setPrice(double price)
    {
        this.price = price;
    }
}
