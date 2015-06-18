package netUtils.response.item;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import classes.model.DidiExpense;
import netUtils.response.common.BaseResponse;

public class DidiOrdersResponse extends BaseResponse
{
    private List<DidiExpense> didiExpenseList;

    public DidiOrdersResponse(Object httpResponse)
    {
        super(httpResponse);
    }

    protected void constructData()
    {
        try
        {
            JSONArray jsonArray = getDataArray();

            didiExpenseList = new ArrayList<>();
            for (int i = 0; i < jsonArray.length(); i++)
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
