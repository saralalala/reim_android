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
    private List<Item> itemList;

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
            List<User> managerList = new ArrayList<>();
            for (int i = 0; i < managerArray.size(); i++)
            {
                JSONObject object = managerArray.getJSONObject(i);
                User user = dbManager.getUser(object.getInteger("id"));
                if (user != null)
                {
                    managerList.add(user);
                }
            }
            report.setManagerList(managerList);

            JSONArray ccArray = receiverObject.getJSONArray("cc");
            List<User> ccList = new ArrayList<>();
            for (int i = 0; i < ccArray.size(); i++)
            {
                JSONObject object = ccArray.getJSONObject(i);
                User user = dbManager.getUser(object.getInteger("id"));
                if (user != null)
                {
                    ccList.add(user);
                }
            }
            report.setCCList(ccList);

            JSONArray commentArray = jObject.getJSONObject("comments").getJSONArray("data");
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

                User reviewer = new User();
                reviewer.setServerID(object.getInteger("uid"));
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

    public void setReport(Report report)
    {
        this.report = report;
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
