package classes.adapter;

import android.content.Context;
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

    public ReportItemListViewAdapter(Context context, List<Item> items, List<Integer> chosenList)
    {
        this.context = context;
        this.layoutInflater = LayoutInflater.from(context);
        this.currentGroup = AppPreference.getAppPreference().getCurrentGroup();
        this.itemList = new ArrayList<>(items);
        this.chosenIDList = new ArrayList<>(chosenList);
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
                viewHolder.symbolTextView = (TextView) convertView.findViewById(R.id.symbolTextView);
                viewHolder.amountTextView = (TextView) convertView.findViewById(R.id.amountTextView);
                viewHolder.vendorTextView = (TextView) convertView.findViewById(R.id.vendorTextView);
                viewHolder.categoryImageView = (ImageView) convertView.findViewById(R.id.categoryImageView);
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

            viewHolder.symbolTextView.setText(item.getCurrency().getSymbol());

            viewHolder.amountTextView.setTypeface(ReimApplication.TypeFaceAleoLight);
            viewHolder.amountTextView.setText(Utils.formatDouble(item.getAmount()));

            String vendor = item.getVendor().isEmpty() ? context.getString(R.string.vendor_not_available) : item.getVendor();
            viewHolder.vendorTextView.setText(vendor);

            if (item.missingInfo(currentGroup))
            {
                viewHolder.warningImageView.setVisibility(View.VISIBLE);
            }
            else
            {
                Category category = item.getCategory();

                if (category != null)
                {
                    ViewUtils.setImageViewBitmap(category, viewHolder.categoryImageView);
                }
                else
                {
                    viewHolder.categoryImageView.setVisibility(View.INVISIBLE);
                }
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
        TextView symbolTextView;
        TextView amountTextView;
        TextView vendorTextView;
        ImageView categoryImageView;
        ImageView warningImageView;
    }
}