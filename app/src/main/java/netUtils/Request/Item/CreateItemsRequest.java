package netUtils.request.item;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

import classes.model.Image;
import classes.model.Item;
import classes.model.Tag;
import classes.model.User;
import classes.utils.Utils;
import netUtils.HttpConnectionCallback;
import netUtils.URLDef;
import netUtils.request.BaseRequest;

public class CreateItemsRequest extends BaseRequest
{
    public CreateItemsRequest(List<Item> itemList)
    {
        super();

        try
        {
            JSONArray jsonArray = new JSONArray();

            for (Item item : itemList)
            {
                int categoryID = item.getCategory() == null ? -1 : item.getCategory().getServerID();
                JSONObject jObject = new JSONObject();
                jObject.put("id", item.getServerID());
                jObject.put("amount", item.getAmount());
                jObject.put("category", categoryID);
                jObject.put("merchants", item.getVendor());
                jObject.put("location", item.getLocation());
                jObject.put("latitude", Double.toString(item.getLatitude()));
                jObject.put("longitude", Double.toString(item.getLongitude()));
                jObject.put("uid", item.getConsumer().getServerID());
                jObject.put("prove_ahead", Integer.toString(item.getType()));
                jObject.put("image_id", Image.getImagesIDString(item.getInvoices()));
                jObject.put("uids", User.getUsersIDString(item.getRelevantUsers()));
                jObject.put("tags", Tag.getTagsIDString(item.getTags()));
                jObject.put("dt", item.getConsumedDate());
                jObject.put("note", item.getNote());
                jObject.put("reimbursed", Utils.booleanToString(item.needReimbursed()));
                jObject.put("local_id", item.getLocalID());

                jsonArray.put(jObject);
            }

            addParams("items", jsonArray.toString());

            appendUrl(URLDef.URL_ITEM);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void sendRequest(HttpConnectionCallback callback)
    {
        doPost(callback);
    }
}
