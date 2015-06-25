package classes.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.rushucloud.reim.R;

import java.util.ArrayList;
import java.util.List;

import classes.model.DidiExpense;
import classes.utils.Utils;
import classes.utils.ViewUtils;

public class DidiExpenseListViewAdapter extends BaseAdapter
{
    private LayoutInflater layoutInflater;
    private List<DidiExpense> expenseList;

    public DidiExpenseListViewAdapter(Context context, List<DidiExpense> expenses)
    {
        this.layoutInflater = LayoutInflater.from(context);
        this.expenseList = new ArrayList<>(expenses);
    }

    public View getView(int position, View convertView, ViewGroup parent)
    {
        if (convertView == null)
        {
            convertView = layoutInflater.inflate(R.layout.list_didi_expense, parent, false);
        }

        DidiExpense expense = expenseList.get(position);

        TextView timeTextView = (TextView) convertView.findViewById(R.id.timeTextView);
        timeTextView.setText(expense.getTime());

        int visibility = expense.isUsed()? View.VISIBLE : View.INVISIBLE;
        TextView usedTextView = (TextView) convertView.findViewById(R.id.usedTextView);
        usedTextView.setVisibility(visibility);

        TextView startTextView = (TextView) convertView.findViewById(R.id.startTextView);
        startTextView.setText(expense.getStart());

        TextView destinationTextView = (TextView) convertView.findViewById(R.id.destinationTextView);
        destinationTextView.setText(expense.getDestionation());

        TextView priceTextView = (TextView) convertView.findViewById(R.id.priceTextView);
        priceTextView.setText(ViewUtils.getString(R.string.rmb_symbol) + Utils.formatAmount(expense.getAmount()));

        return convertView;
    }

    public int getCount()
    {
        return expenseList.size();
    }

    public DidiExpense getItem(int position)
    {
        return expenseList.get(position);
    }

    public long getItemId(int position)
    {
        return position;
    }

    public void setExpenseList(List<DidiExpense> expenses)
    {
        expenseList.clear();
        expenseList.addAll(expenses);
    }
}