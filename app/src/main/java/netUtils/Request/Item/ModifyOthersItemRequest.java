package netUtils.request.item;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

import classes.model.Item;
import classes.utils.JSONUtils;
import netUtils.common.HttpConnectionCallback;
import netUtils.common.NetworkConstant;
import netUtils.common.URLDef;
import netUtils.request.common.BaseRequest;

public class ModifyOthersItemRequest extends BaseRequest
{
    public ModifyOthersItemRequest(Item item, boolean[] attributesCheck)
    {
        super();

        JSONArray jsonArray = new JSONArray();

        try
        {
            for (int i = 0; i < attributesCheck.length; i++)
            {
                JSONObject object = new JSONObject();
                switch (i + 1)
                {
                    case NetworkConstant.UPDATE_ITEM_TYPE_CATEGORY:
                        if (attributesCheck[i])
                        {
                            object.put("type", Integer.toString(NetworkConstant.UPDATE_ITEM_TYPE_CATEGORY));
                            object.put("val", item.getCategory().getServerID());
                        }
                        break;
                    case NetworkConstant.UPDATE_ITEM_TYPE_NOTE:
                        if (attributesCheck[i])
                        {
                            object.put("type", Integer.toString(NetworkConstant.UPDATE_ITEM_TYPE_NOTE));
                            object.put("val", item.getNote());
                        }
                        break;
                    case NetworkConstant.UPDATE_ITEM_TYPE_TAGS:
                        if (attributesCheck[i])
                        {
                            object.put("type", Integer.toString(NetworkConstant.UPDATE_ITEM_TYPE_TAGS));
                            object.put("val", item.getTagsID());
                        }
                        break;
                    case NetworkConstant.UPDATE_ITEM_TYPE_VENDOR:
                        if (attributesCheck[i])
                        {
                            object.put("type", Integer.toString(NetworkConstant.UPDATE_ITEM_TYPE_VENDOR));
                            object.put("val", item.getVendor());
                        }
                        break;
                    case NetworkConstant.UPDATE_ITEM_TYPE_USERS:
                        if (attributesCheck[i])
                        {
                            object.put("type", Integer.toString(NetworkConstant.UPDATE_ITEM_TYPE_USERS));
                            object.put("val", item.getRelevantUsersID());
                        }
                        break;
                    case NetworkConstant.UPDATE_ITEM_TYPE_AMOUNT:
                        if (attributesCheck[i])
                        {
                            object.put("type", Integer.toString(NetworkConstant.UPDATE_ITEM_TYPE_AMOUNT));
                            object.put("val", item.getAmount());
                        }
                        break;
                    case NetworkConstant.UPDATE_ITEM_TYPE_LOCATION:
                        if (attributesCheck[i])
                        {
                            object.put("type", Integer.toString(NetworkConstant.UPDATE_ITEM_TYPE_LOCATION));
                            object.put("val", item.getLocation());
                        }
                        break;
                    case NetworkConstant.UPDATE_ITEM_TYPE_CONSUMED_DATE:
                        if (attributesCheck[i])
                        {
                            object.put("type", Integer.toString(NetworkConstant.UPDATE_ITEM_TYPE_CONSUMED_DATE));
                            object.put("val", item.getConsumedDate());
                        }
                        break;
                    case NetworkConstant.UPDATE_ITEM_TYPE_EXTRA:
                        if (attributesCheck[i])
                        {
                            object.put("type", Integer.toString(NetworkConstant.UPDATE_ITEM_TYPE_EXTRA));
                            object.put("val", item.getExtraString());
                        }
                        break;
                    default:
                        break;
                }
                if (JSONUtils.optInt(object, "type", -1) != -1)
                {
                    jsonArray.add(object);
                }
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }

        addParams("iid", item.getServerID());
        addParams("opts", jsonArray.toString());

        appendUrl(URLDef.URL_ITEM_UPDATE);
    }

    public void sendRequest(HttpConnectionCallback callback)
    {
        doPost(callback);
    }
}