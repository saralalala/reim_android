package netUtils.response.item;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;

import java.util.ArrayList;
import java.util.List;

import classes.model.Item;
import netUtils.response.common.BaseResponse;

public class SearchItemsResponse extends BaseResponse
{
    private List<Item> itemList;

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
