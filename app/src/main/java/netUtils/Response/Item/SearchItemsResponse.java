package netUtils.response.item;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import classes.model.Item;
import netUtils.response.BaseResponse;

public class SearchItemsResponse extends BaseResponse
{
    List<Item> itemList;

    public SearchItemsResponse(Object httpResponse)
    {
        super(httpResponse);
    }

    protected void constructData()
    {
        try
        {
            JSONArray jsonArray = getDataArray();

            itemList = new ArrayList<>();
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
