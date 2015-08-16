package classes.adapter;

import android.content.Context;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.rushucloud.reim.R;

import java.util.ArrayList;
import java.util.List;

import classes.model.Category;
import classes.model.Group;
import classes.model.Item;
import classes.utils.AppPreference;
import classes.utils.ReimApplication;
import classes.utils.Utils;
import classes.utils.ViewUtils;

public class ReportItemListViewAdapter extends BaseAdapter
{
    private Context context;
    private LayoutInflater layoutInflater;
    private Group currentGroup;
    private List<Item> itemList;
    private List<Integer> chosenIDList;
    private SparseIntArray daysArray = new SparseIntArray();
    private SparseIntArray countArray = new SparseIntArray();

    public ReportItemListViewAdapter(Context context, List<Item> items, List<Integer> chosenList)
    {
        this.context = context;
        this.layoutInflater = LayoutInflater.from(context);
        this.currentGroup = AppPreference.getAppPreference().getCurrentGroup();
        this.itemList = new ArrayList<>(items);
        this.chosenIDList = new ArrayList<>(chosenList);
        for (Item item : items)
        {
            daysArray.put(item.getServerID(), item.getDurationDays());
            countArray.put(item.getLocalID(), item.getMemberCount());
        }
    }

    public View getView(int position, View convertView, ViewGroup parent)
    {
        if (position == 0)
        {
            return layoutInflater.inflate(R.layout.list_report_new_item, parent, false);
        }
        else
        {
            ViewHolder viewHolder;
            if(convertView == null || convertView.getTag() == null)
            {
                convertView = layoutInflater.inflate(R.layout.list_report_item_edit, parent, false);

                viewHolder = new ViewHolder();
                viewHolder.dateTextView = (TextView) convertView.findViewById(R.id.dateTextView);
                viewHolder.categoryImageView = (ImageView) convertView.findViewById(R.id.categoryImageView);
                viewHolder.categoryTextView = (TextView) convertView.findViewById(R.id.categoryTextView);
                viewHolder.symbolTextView = (TextView) convertView.findViewById(R.id.symbolTextView);
                viewHolder.amountTextView = (TextView) convertView.findViewById(R.id.amountTextView);
                viewHolder.noteTextView = (TextView) convertView.findViewById(R.id.noteTextView);
                viewHolder.vendorTextView = (TextView) convertView.findViewById(R.id.vendorTextView);
                viewHolder.warningImageView = (ImageView) convertView.findViewById(R.id.warningImageView);

                convertView.setTag(viewHolder);
            }
            else
            {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            Item item = itemList.get(position - 1);

            int color = chosenIDList.contains(item.getLocalID()) ? R.color.list_item_pressed : R.color.list_item_unpressed;
            convertView.setBackgroundResource(color);

            viewHolder.dateTextView.setText(Utils.secondToStringUpToDay(item.getConsumedDate()));

            viewHolder.symbolTextView.setText(item.getCurrency().getSymbol());

            viewHolder.amountTextView.setTypeface(ReimApplication.TypeFaceAleoLight);
            viewHolder.amountTextView.setText(Utils.formatDouble(item.getAmount()));

            String vendor = item.getVendor().isEmpty() ? context.getString(R.string.vendor_not_available) : item.getVendor();
            viewHolder.vendorTextView.setText(vendor);

            String note = !item.getNote().isEmpty() ? item.getNote() : "";
            int days = daysArray.get(item.getServerID());
            int count = countArray.get(item.getLocalID());
            if (days > 0)
            {
                note = item.getDailyAverage(days) + " " + note;
            }
            else if (count > 1)
            {
                note = item.getPerCapita(count) + " " + note;
            }
            viewHolder.noteTextView.setText(note);

            Category category = item.getCategory();
            ViewUtils.setImageViewBitmap(category, viewHolder.categoryImageView);

            String categoryName = category != null ? category.getName() : "";
            viewHolder.categoryTextView.setText(categoryName);

            if (item.missingInfo(currentGroup))
            {
                viewHolder.warningImageView.setVisibility(View.VISIBLE);
            }

            return convertView;
        }
    }

    public int getCount()
    {
        return itemList.size() + 1;
    }

    public Item getItem(int position)
    {
        return itemList.get(position);
    }

    public long getItemId(int position)
    {
        return position;
    }

    public void set(List<Item> items, List<Integer> chosenList)
    {
        itemList.clear();
        itemList.addAll(items);
        daysArray.clear();
        countArray.clear();
        for (Item item : items)
        {
            daysArray.put(item.getServerID(), item.getDurationDays());
            countArray.put(item.getLocalID(), item.getMemberCount());
        }

        chosenIDList.clear();
        chosenIDList.addAll(chosenList);
    }

    public void setChosenList(List<Integer> chosenList)
    {
        chosenIDList.clear();
        chosenIDList.addAll(chosenList);
    }

    private static class ViewHolder
    {
        TextView dateTextView;
        ImageView categoryImageView;
        TextView categoryTextView;
        TextView symbolTextView;
        TextView amountTextView;
        TextView noteTextView;
        TextView vendorTextView;
        ImageView warningImageView;
    }
}