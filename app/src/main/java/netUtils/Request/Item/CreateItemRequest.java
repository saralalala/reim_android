package netUtils.request.item;

import org.json.JSONArray;
import org.json.JSONObject;

import classes.model.Image;
import classes.model.Item;
import classes.model.Tag;
import classes.model.User;
import classes.utils.Utils;
import netUtils.common.HttpConnectionCallback;
import netUtils.common.URLDef;
import netUtils.request.common.BaseRequest;

public class CreateItemRequest extends BaseRequest
{
    public CreateItemRequest(Item item)
    {
        super();

        try
        {
            JSONArray jsonArray = new JSONArray();

            int categoryID = item.getCategory() == null ? -1 : item.getCategory().getServerID();
            JSONObject jObject = new JSONObject();
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
