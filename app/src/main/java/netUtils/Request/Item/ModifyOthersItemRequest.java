package netUtils.request.item;

import org.json.JSONException;
import org.json.JSONObject;

import classes.model.Item;
import netUtils.common.HttpConnectionCallback;
import netUtils.common.NetworkConstant;
import netUtils.common.URLDef;
import netUtils.request.common.BaseRequest;

public class ModifyOthersItemRequest extends BaseRequest
{
    public ModifyOthersItemRequest(Item item, boolean[] attributesCheck)
    {
        super();

        JSONObject object = new JSONObject();

        try
        {
            for (int i = 0; i < attributesCheck.length; i++)
            {
                switch (i + 1)
                {
                    case NetworkConstant.UPDATE_ITEM_TYPE_CATEGORY:
                        if (attributesCheck[i])
                        {
                            object.put(Integer.toString(NetworkConstant.UPDATE_ITEM_TYPE_CATEGORY),
                                       item.getCategory().getServerID());
                        }
                        break;
                    case NetworkConstant.UPDATE_ITEM_TYPE_NOTE:
                        if (attributesCheck[i])
                        {
                            object.put(Integer.toString(NetworkConstant.UPDATE_ITEM_TYPE_NOTE),
                                       item.getNote());
                        }
                        break;
                    case NetworkConstant.UPDATE_ITEM_TYPE_TAGS:
                        if (attributesCheck[i])
                        {
                            object.put(Integer.toString(NetworkConstant.UPDATE_ITEM_TYPE_TAGS),
                                       item.getTagsID());
                        }
                        break;
                    case NetworkConstant.UPDATE_ITEM_TYPE_VENDOR:
                        if (attributesCheck[i])
                        {
                            object.put(Integer.toString(NetworkConstant.UPDATE_ITEM_TYPE_VENDOR),
                                       item.getVendor());
                        }
                        break;
                    case NetworkConstant.UPDATE_ITEM_TYPE_USERS:
                        if (attributesCheck[i])
                        {
                            object.put(Integer.toString(NetworkConstant.UPDATE_ITEM_TYPE_USERS),
                                       item.getRelevantUsersID());
                        }
                        break;
                    case NetworkConstant.UPDATE_ITEM_TYPE_AMOUNT:
                        if (attributesCheck[i])
                        {
                            object.put(Integer.toString(NetworkConstant.UPDATE_ITEM_TYPE_AMOUNT),
                                       item.getAmount());
                        }
                        break;
                    case NetworkConstant.UPDATE_ITEM_TYPE_LOCATION:
                        if (attributesCheck[i])
                        {
                            object.put(Integer.toString(NetworkConstant.UPDATE_ITEM_TYPE_LOCATION),
                                       item.getLocation());
                        }
                        break;
                    case NetworkConstant.UPDATE_ITEM_TYPE_CONSUMED_DATE:
                        if (attributesCheck[i])
                        {
                            object.put(Integer.toString(NetworkConstant.UPDATE_ITEM_TYPE_CONSUMED_DATE),
                                       item.getConsumedDate());
                        }
                        break;
                    default:
                        break;
                }
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }

        addParams("iid", item.getServerID());
        addParams("opts", object.toString());

        appendUrl(URLDef.URL_ITEM_UPDATE);
    }

    public void sendRequest(HttpConnectionCallback callback)
    {
        doPost(callback);
    }
}