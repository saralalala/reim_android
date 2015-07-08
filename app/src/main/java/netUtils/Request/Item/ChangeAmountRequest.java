package netUtils.request.item;

import classes.model.Item;
import netUtils.common.HttpConnectionCallback;
import netUtils.common.URLDef;
import netUtils.request.common.BaseRequest;

public class ChangeAmountRequest extends BaseRequest
{
    public ChangeAmountRequest(Item item)
    {
        super();

        addParams("iid", item.getServerID());
        addParams("amount", item.getAmount());
        addParams("currency", item.getCurrency().getCode());

        appendUrl(URLDef.URL_ITEM_UPDATE_AMOUNT);
    }

    public void sendRequest(HttpConnectionCallback callback)
    {
        doPut(callback);
    }
}