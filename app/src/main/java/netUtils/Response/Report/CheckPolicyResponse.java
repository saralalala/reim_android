package netUtils.response.report;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.List;

import classes.utils.Utils;
import netUtils.response.common.BaseResponse;

public class CheckPolicyResponse extends BaseResponse
{
    private boolean isReportCanBeFinished;
    private ArrayList<Integer> managerIDList;

    public CheckPolicyResponse(Object httpResponse)
    {
        super(httpResponse);
    }

    protected void constructData()
    {
        try
        {
            JSONObject jObject = getDataObject();

            isReportCanBeFinished = Utils.intToBoolean(jObject.getInteger("complete"));

            managerIDList = new ArrayList<>();
            JSONArray idArray = jObject.getJSONArray("suggestion");
            for (int i = 0; i < idArray.size(); i++)
            {
                managerIDList.add(idArray.getInteger(i));
            }

        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    public boolean isReportCanBeFinished()
    {
        return isReportCanBeFinished;
    }

    public ArrayList<Integer> getManagerIDList()
    {
        return managerIDList;
    }
}
