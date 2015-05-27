package netUtils.response.report;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import classes.model.ApproveInfo;
import netUtils.response.BaseResponse;

public class ApproveInfoResponse extends BaseResponse
{
    private String submitDate;
    private List<ApproveInfo> infoList;

    public ApproveInfoResponse(Object httpResponse)
    {
        super(httpResponse);
    }

    protected void constructData()
    {
        try
        {
            JSONObject jObject = getDataObject();

            JSONObject metaObject = jObject.getJSONObject("meta");
            submitDate = metaObject.getString("submitdt").substring(0, 16);

            JSONArray jsonArray = jObject.getJSONArray("data");
            infoList = new ArrayList<>();
            for (int i = 0; i < jsonArray.length(); i++)
            {
                ApproveInfo info = new ApproveInfo(jsonArray.getJSONObject(i));
                infoList.add(info);
            }

            Collections.sort(infoList);
            Collections.reverse(infoList);
        }
        catch (JSONException e)
        {
            if (submitDate == null)
            {
                submitDate = "";
            }
            if (infoList == null)
            {
                infoList = new ArrayList<>();
            }
            e.printStackTrace();
        }
    }

    public String getSubmitDate()
    {
        return submitDate;
    }

    public List<ApproveInfo> getInfoList()
    {
        return infoList;
    }
}