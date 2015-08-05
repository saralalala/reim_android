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

import classes.model.UberExpense;
import classes.utils.Utils;
import classes.utils.ViewUtils;

public class UberExpenseListViewAdapter extends BaseAdapter
{
    private LayoutInflater layoutInflater;
    private List<UberExpense> expenseList;
    private ArrayList<Integer> importedList = new ArrayList<>();

    public UberExpenseListViewAdapter(Context context, List<UberExpense> expenses)
    {
        this.layoutInflater = LayoutInflater.from(context);
        this.expenseList = new ArrayList<>(expenses);
    }

    public View getView(int position, View convertView, ViewGroup parent)
    {
        ViewHolder viewHolder;
        if (convertView == null)
        {
            convertView = layoutInflater.inflate(R.layout.list_uber_expense, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.timeTextView = (TextView) convertView.findViewById(R.id.timeTextView);
            viewHolder.usedTextView = (TextView) convertView.findViewById(R.id.usedTextView);
            viewHolder.startTextView = (TextView) convertView.findViewById(R.id.startTextView);
            viewHolder.priceTextView = (TextView) convertView.findViewById(R.id.priceTextView);

            convertView.setTag(viewHolder);
        }
        else
        {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        UberExpense expense = expenseList.get(position);

        viewHolder.timeTextView.setText(expense.getTime());
        int visibility = expense.isUsed() || importedList.contains(expense.getId()) ? View.VISIBLE : View.INVISIBLE;

        viewHolder.usedTextView.setVisibility(visibility);
        viewHolder.startTextView.setText(expense.getStart());
        viewHolder.priceTextView.setText(ViewUtils.getString(R.string.rmb_symbol) + Utils.formatAmount(expense.getAmount()));

        return convertView;
    }

    public int getCount()
    {
        return expenseList.size();
    }

    public UberExpense getItem(int position)
    {
        return expenseList.get(position);
    }

    public long getItemId(int position)
    {
        return position;
    }

    public void setExpenseList(List<UberExpense> expenses)
    {
        expenseList.clear();
        expenseList.addAll(expenses);
    }

    public void setImportedList(ArrayList<Integer> imports)
    {
        importedList.clear();
        importedList.addAll(imports);
    }

    private static class ViewHolder
    {
        TextView timeTextView;
        TextView usedTextView;
        TextView startTextView;
        TextView priceTextView;
    }
}