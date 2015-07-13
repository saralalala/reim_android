package netUtils.response.report;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.List;

import classes.model.Comment;
import classes.model.Item;
import classes.model.Report;
import classes.model.User;
import classes.utils.DBManager;
import classes.utils.Utils;
import netUtils.response.common.BaseResponse;

public class GetReportResponse extends BaseResponse
{
    private Report report;
    private List<User> managerList;
    private List<User> ccList;
    private List<Item> itemList;
    private boolean containsUnsyncedUser;

    public GetReportResponse(Object httpResponse)
    {
        super(httpResponse);
    }

    protected void constructData()
    {
        try
        {
            JSONObject jObject = getDataObject();
            report = new Report(jObject);
            report.setMyDecision(jObject.getInteger("mdecision"));
            report.setIsCC(Utils.intToBoolean(jObject.getInteger("cc")));
            report.setStep(jObject.getInteger("step"));

            DBManager dbManager = DBManager.getDBManager();

            JSONObject receiverObject = jObject.getJSONObject("receivers");

            JSONArray managerArray = receiverObject.getJSONArray("managers");
            managerList = new ArrayList<>();
            List<User> localManagerList = new ArrayList<>();
            for (int i = 0; i < managerArray.size(); i++)
            {
                JSONObject object = managerArray.getJSONObject(i);
                int id = object.getInteger("id");

                User user = new User();
                user.setServerID(id);
                managerList.add(user);

                User localUser = dbManager.getUser(id);
                if (localUser != null)
                {
                    localManagerList.add(localUser);
                }
            }
            report.setManagerList(localManagerList);

            JSONArray ccArray = receiverObject.getJSONArray("cc");
            ccList = new ArrayList<>();
            List<User> localCCList = new ArrayList<>();
            for (int i = 0; i < ccArray.size(); i++)
            {
                JSONObject object = ccArray.getJSONObject(i);
                int id = object.getInteger("id");

                User user = new User();
                user.setServerID(id);
                ccList.add(user);

                User localUser = dbManager.getUser(id);
                if (localUser != null)
                {
                    localCCList.add(localUser);
                }
            }
            report.setCCList(localCCList);

            JSONArray commentArray = jObject.getJSONObject("comments").getJSONArray("data");
            List<Integer> idList = new ArrayList<>();
            List<Comment> commentList = new ArrayList<>();
            for (int i = 0; i < commentArray.size(); i++)
            {
                JSONObject object = commentArray.getJSONObject(i);
                Comment comment = new Comment();
                comment.setServerID(object.getInteger("cid"));
                comment.setContent(object.getString("comment"));
                comment.setCreatedDate(object.getInteger("lastdt"));
                comment.setLocalUpdatedDate(object.getInteger("lastdt"));
                comment.setServerUpdatedDate(object.getInteger("lastdt"));

                int id = object.getInteger("uid");

                if (!idList.contains(id))
                {
                    idList.add(id);
                }

                User reviewer = new User();
                reviewer.setServerID(id);
                comment.setReviewer(reviewer);

                commentList.add(comment);
            }
            report.setCommentList(commentList);

            int itemCount = 0;
            double amount = 0;

            itemList = new ArrayList<>();
            JSONArray itemArray = jObject.getJSONArray("items");
            for (int i = 0; i < itemArray.size(); i++)
            {
                Item item = new Item(itemArray.getJSONObject(i));
                itemList.add(item);
                itemCount++;
                amount += item.getAmount();
            }

            report.setAmount(Double.toString(amount));
            report.setItemCount(itemCount);

            containsUnsyncedUser = managerList.size() != localManagerList.size() ||
                                    ccList.size() != localCCList.size() ||
                                    !dbManager.isAllUsersInDatabase(idList);
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    public Report getReport()
    {
        return report;
    }

    public List<User> getManagerList()
    {
        return managerList;
    }

    public List<User> getCCList()
    {
        return ccList;
    }

    public List<Item> getItemList()
    {
        return itemList;
    }

    public boolean containsUnsyncedUser()
    {
        return containsUnsyncedUser;
    }
}
