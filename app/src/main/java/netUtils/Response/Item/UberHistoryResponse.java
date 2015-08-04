package netUtils.response.item;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;

import java.util.ArrayList;
import java.util.List;

import classes.model.UberExpense;
import netUtils.response.common.BaseResponse;

public class UberHistoryResponse extends BaseResponse
{
    private List<UberExpense> uberExpenseList;

    public UberHistoryResponse(Object httpResponse)
    {
        super(httpResponse);
    }

    protected void constructData()
    {
        try
        {
            JSONArray jsonArray = getDataObject().getJSONArray("history");

            uberExpenseList = new ArrayList<>();
            for (int i = 0; i < jsonArray.size(); i++)
            {
                UberExpense expense = new UberExpense(jsonArray.getJSONObject(i));
                if (expense.isCompleted())
                {
                    uberExpenseList.add(expense);
                }
            }

        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    public List<UberExpense> getUberExpenseList()
    {
        return uberExpenseList;
    }
}