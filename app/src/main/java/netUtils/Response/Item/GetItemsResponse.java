package netUtils.response.item;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
            for (int i = 0; i < jsonArray.length(); i++)
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
