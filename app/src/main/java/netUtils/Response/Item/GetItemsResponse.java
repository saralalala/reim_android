package netUtils.response.item;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.List;

import classes.model.Item;
import netUtils.response.common.BaseResponse;

public class GetItemsResponse extends BaseResponse
{
    private List<Item> itemList;

    public GetItemsResponse(Object httpResponse)
    {
        super(httpResponse);
    }

    protected void constructData()
    {
        try
        {
            JSONObject jObject = getDataObject();

            itemList = new ArrayList<>();
            JSONArray jsonArray = jObject.getJSONArray("items");
            for (int i = 0; i < jsonArray.size(); i++)
            {
                Item item = new Item(jsonArray.getJSONObject(i));
                itemList.add(item);
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    public List<Item> getItemList()
    {
        return itemList;
    }

    public void setItemList(List<Item> itemList)
    {
        this.itemList = itemList;
    }
}