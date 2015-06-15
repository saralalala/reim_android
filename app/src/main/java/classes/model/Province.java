package classes.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Province
{
    private String name;
    private List<String> cityList;

    public Province()
    {
        super();
    }

    public Province(JSONObject jObject)
    {
        try
        {
            name = jObject.getString("name");
            cityList = new ArrayList<>();
            JSONArray cityArray = jObject.getJSONArray("city");
            for (int i = 0; i < cityArray.length(); i++)
            {
                cityList.add(cityArray.getString(i));
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    public String getName()
    {
        return name;
    }
    public void setName(String name)
    {
        this.name = name;
    }

    public List<String> getCityList()
    {
        return cityList;
    }
    public String[] getCityArray()
    {
        return cityList.toArray(new String[cityList.size()]);
    }

    public static String[] getProvinceArray(List<Province> provinceList)
    {
        String[] result = new String[provinceList.size()];
        for (int i = 0; i < provinceList.size(); i++)
        {
            result[i] = provinceList.get(i).getName();
        }
        return result;
    }

    public boolean equals(Object o)
    {
        if (o instanceof Province)
        {
            Province province = (Province) o;
            return province.getName().equals(this.getName());
        }
        return super.equals(o);
    }
}