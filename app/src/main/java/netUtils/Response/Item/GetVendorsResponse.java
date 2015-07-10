package netUtils.response.item;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import classes.model.StatDepartment;
import classes.model.Vendor;

public class GetVendorsResponse
{
    private boolean status;
    private List<Vendor> vendorList = new ArrayList<>();

    public GetVendorsResponse(Object httpResponse)
    {
        try
        {
            JSONObject jObject = new JSONObject((String) httpResponse);
            status = jObject.getString("status").equals("OK");

            JSONArray jsonArray = jObject.getJSONArray("businesses");
            int count = jsonArray.length();
            for (int i = 0; i < count; i++)
            {
                vendorList.add(new Vendor(jsonArray.getJSONObject(i)));
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            status = false;
        }
    }

    public boolean getStatus()
    {
        return status;
    }

    public void setStatus(boolean status)
    {
        this.status = status;
    }

    public List<Vendor> getVendorList()
    {
        return vendorList;
    }
}