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
    private ArrayList<Integer> importedList = new ArrayList<>();

    public DidiExpenseListViewAdapter(Context context, List<DidiExpense> expenses)
    {
        this.layoutInflater = LayoutInflater.from(context);
        this.expenseList = new ArrayList<>(expenses);
    }

    public View getView(int position, View convertView, ViewGroup parent)
    {
        ViewHolder viewHolder;
        if (convertView == null)
        {
            convertView = layoutInflater.inflate(R.layout.list_didi_expense, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.timeTextView = (TextView) convertView.findViewById(R.id.timeTextView);
            viewHolder.usedTextView = (TextView) convertView.findViewById(R.id.usedTextView);
            viewHolder.startTextView = (TextView) convertView.findViewById(R.id.startTextView);
            viewHolder.destinationTextView = (TextView) convertView.findViewById(R.id.destinationTextView);
            viewHolder.priceTextView = (TextView) convertView.findViewById(R.id.priceTextView);

            convertView.setTag(viewHolder);
        }
        else
        {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        DidiExpense expense = expenseList.get(position);

        viewHolder.timeTextView.setText(expense.getTime());
        int visibility = expense.isUsed() || importedList.contains(expense.getId()) ? View.VISIBLE : View.INVISIBLE;

        viewHolder.usedTextView.setVisibility(visibility);
        viewHolder.startTextView.setText(expense.getStart());
        viewHolder.destinationTextView.setText(expense.getDestination());
        viewHolder.priceTextView.setText(ViewUtils.getString(R.string.rmb_symbol) + Utils.formatAmount(expense.getAmount()));

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

    public void setImportedList(ArrayList<Integer> imports)
    {
        importedList.clear();
        importedList.addAll(imports);
    }

    static class ViewHolder
    {
        TextView timeTextView;
        TextView usedTextView;
        TextView startTextView;
        TextView destinationTextView;
        TextView priceTextView;
    }
}