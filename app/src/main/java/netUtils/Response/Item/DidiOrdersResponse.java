package netUtils.response.item;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;

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
            for (int i = 0; i < jsonArray.size(); i++)
            {
                DidiExpense expense = new DidiExpense(jsonArray.getJSONObject(i));
                if (!expense.isClosed())
                {
                    didiExpenseList.add(expense);
                }
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