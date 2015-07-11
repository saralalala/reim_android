package netUtils.response.item;

import classes.model.Item;
import netUtils.response.common.BaseResponse;

public class GetItemResponse extends BaseResponse
{
    private Item item;

    public GetItemResponse(Object httpResponse)
    {
        super(httpResponse);
    }

    protected void constructData()
    {
        item = new Item(getDataObject());
    }

    public Item getItem()
    {
        return item;
    }
}
