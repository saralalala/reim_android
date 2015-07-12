package netUtils.response.user;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.List;

import classes.model.DidiExpense;
import netUtils.response.common.BaseResponse;

public class BindDidiResponse extends BaseResponse
{
    private List<DidiExpense> didiExpenseList;

    public BindDidiResponse(Object httpResponse)
    {
        super(httpResponse);
    }

    protected void constructData()
    {
        try
        {
            JSONArray jsonArray = getDataArray();

            didiExpenseList = new ArrayList<>();
            for (int i = 0; i < jsonArray.size(); i++)
            {
                JSONObject object = jsonArray.getJSONObject(i);
                didiExpenseList.add(new DidiExpense(object));
            }

        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    public List<DidiExpense> getDidiExpenseList()
    {
        return didiExpenseList;
    }
}
