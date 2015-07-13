package netUtils.response.item;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.List;

import classes.model.Vendor;

public class GetVendorsResponse
{
    private boolean status;
    private List<Vendor> vendorList = new ArrayList<>();

    public GetVendorsResponse(Object httpResponse)
    {
        try
        {
            JSONObject jObject = JSON.parseObject((String) httpResponse);
            status = jObject.getString("status").equals("OK");

            JSONArray jsonArray = jObject.getJSONArray("businesses");
            int count = jsonArray.size();
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