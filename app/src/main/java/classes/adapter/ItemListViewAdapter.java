package classes.adapter;

import android.content.Context;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import com.rushucloud.reim.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import classes.model.Category;
import classes.model.Item;
import classes.model.Tag;
import classes.model.User;
import classes.utils.Constant;
import classes.utils.ReimApplication;
import classes.utils.Utils;
import classes.utils.ViewUtils;

public class ItemListViewAdapter extends BaseAdapter
{
    private Context context;
    private LayoutInflater layoutInflater;
    private ItemFilter itemFilter;
    private List<Item> itemList;
    private List<Item> originalList;
    private SparseIntArray daysArray = new SparseIntArray();
    private final Object mLock = new Object();

    public ItemListViewAdapter(Context context, List<Item> items)
    {
        this.context = context;
        this.layoutInflater = LayoutInflater.from(context);
        this.itemList = new ArrayList<>(items);
        for (Item item : items)
        {
            daysArray.put(item.getLocalID(), item.getDurationDays());
        }
    }

    public View getView(int position, View convertView, ViewGroup parent)
    {
        Item item = this.getItem(position);
        int viewType = getItemViewType(position);
        HeaderViewHolder headerViewHolder;
        ItemViewHolder itemViewHolder;

        if(viewType == Constant.VIEW_TYPE_HEADER)
        {
            if(convertView == null)
            {
                convertView = layoutInflater.inflate(R.layout.list_header, parent, false);

                headerViewHolder = new HeaderViewHolder();
                headerViewHolder.headerTextView = (TextView) convertView.findViewById(R.id.headerTextView);

                convertView.setTag(headerViewHolder);
            }
            else
            {
                headerViewHolder = (HeaderViewHolder) convertView.getTag();
            }

            String date = item.getConsumedDateGroup();
            headerViewHolder.headerTextView.setText(Utils.dateToWeekday(date) + " " + date);
        }
        else
        {
            if(convertView == null)
            {
                convertView = layoutInflater.inflate(R.layout.list_item, parent, false);

                itemViewHolder = new ItemViewHolder();
                itemViewHolder.photoImageView = (ImageView) convertView.findViewById(R.id.photoImageView);
                itemViewHolder.statusTextView = (TextView) convertView.findViewById(R.id.statusTextView);
                itemViewHolder.typeTextView = (TextView) convertView.findViewById(R.id.typeTextView);
                itemViewHolder.symbolTextView = (TextView) convertView.findViewById(R.id.symbolTextView);
                itemViewHolder.amountTextView = (TextView) convertView.findViewById(R.id.amountTextView);
                itemViewHolder.noteTextView = (TextView) convertView.findViewById(R.id.noteTextView);
                itemViewHolder.reportTextView = (TextView) convertView.findViewById(R.id.reportTextView);
                itemViewHolder.vendorTextView = (TextView) convertView.findViewById(R.id.vendorTextView);
                itemViewHolder.categoryImageView = (ImageView) convertView.findViewById(R.id.categoryImageView);

                convertView.setTag(itemViewHolder);
            }
            else
            {
                itemViewHolder = (ItemViewHolder)convertView.getTag();
            }

            int photoVisibility = item.hasInvoice() ? View.VISIBLE : View.GONE;
            itemViewHolder.photoImageView.setVisibility(photoVisibility);

            itemViewHolder.statusTextView.setText(item.getStatusString());
            itemViewHolder.statusTextView.setBackgroundResource(item.getStatusBackground());

            int typeVisibility = item.getType() == Item.TYPE_REIM ? View.GONE : View.VISIBLE;
            itemViewHolder.typeTextView.setVisibility(typeVisibility);

            if (item.getType() == Item.TYPE_BUDGET && item.isAaApproved())
            {
                itemViewHolder.typeTextView.setText(R.string.status_budget);
                itemViewHolder.typeTextView.setBackgroundResource(R.drawable.status_item_approved);
            }
            else if (item.getType() == Item.TYPE_BUDGET)
            {
                itemViewHolder.typeTextView.setText(R.string.status_budget);
                itemViewHolder.typeTextView.setBackgroundResource(R.drawable.status_item_approve_ahead);
            }
            else if (item.getType() == Item.TYPE_BORROWING && item.isAaApproved())
            {
                itemViewHolder.typeTextView.setText(R.string.status_borrowing);
                itemViewHolder.typeTextView.setBackgroundResource(R.drawable.status_item_approved);
            }
            else if (item.getType() == Item.TYPE_BORROWING)
            {
                itemViewHolder.typeTextView.setText(R.string.status_borrowing);
                itemViewHolder.typeTextView.setBackgroundResource(R.drawable.status_item_approve_ahead);
            }

            itemViewHolder.symbolTextView.setText(item.getCurrency().getSymbol());

            itemViewHolder.amountTextView.setTypeface(ReimApplication.TypeFaceAleoLight);
            itemViewHolder.amountTextView.setText(Utils.formatDouble(item.getAmount()));

            int days = daysArray.get(item.getLocalID());
            String note = days > 0 ? item.getCurrency().getSymbol() + Utils.formatAmount(item.getAmount() / days) +
                            "/" + ViewUtils.getString(R.string.day) + "*" + days : "";
            itemViewHolder.noteTextView.setText(note);

            String vendor = item.getVendor().isEmpty() ? context.getString(R.string.vendor_not_available) : item.getVendor();
            itemViewHolder.vendorTextView.setText(vendor);

            String reportTitle = item.getBelongReport() == null ? context.getString(R.string.report_not_available) : item.getBelongReport().getTitle();
            itemViewHolder.reportTextView.setText(reportTitle);

            Category category = item.getCategory();
            if (category != null)
            {
                ViewUtils.setImageViewBitmap(category, itemViewHolder.categoryImageView);
            }
            else
            {
                itemViewHolder.categoryImageView.setVisibility(View.GONE);
            }
        }

        return convertView;
    }

    public int getCount()
    {
        return itemList.size();
    }

    public Item getItem(int position)
    {
        return itemList.get(position);
    }

    public long getItemId(int position)
    {
        return position;
    }

    public int getItemViewType(int position)
    {
        return itemList.get(position).getConsumedDateGroup().isEmpty() ? Constant.VIEW_TYPE_CONTENT : Constant.VIEW_TYPE_HEADER; // Type 两种 0和1
    }

    public int getViewTypeCount() {
        return 2;
    }

    public void setItemList(List<Item> items)
    {
        itemList.clear();
        itemList.addAll(items);
        daysArray.clear();
        for (Item item : items)
        {
            daysArray.put(item.getLocalID(), item.getDurationDays());
        }
    }

    public Filter getFilter()
    {
        if (itemFilter == null)
        {
            itemFilter = new ItemFilter();
        }
        return itemFilter;
    }

    private class ItemFilter extends Filter
    {
        protected FilterResults performFiltering(CharSequence constraint)
        {
            FilterResults results = new FilterResults();
            if (originalList == null)
            {
                synchronized (mLock)
                {
                    originalList = new ArrayList<>(itemList);
                }
            }
            if (constraint == null || constraint.length() == 0)
            {
                synchronized (mLock)
                {
                    ArrayList<Item> list = new ArrayList<>(originalList);
                    results.values = list;
                    results.count = list.size();
                }
            }
            else
            {
                Locale locale = Locale.getDefault();
                String constraintString = constraint.toString().toLowerCase(locale);
                ArrayList<Item> newValues = new ArrayList<>();
                for (Item item : originalList)
                {
                    if (item.getNote().contains(constraintString))
                    {
                        newValues.add(item);
                        continue;
                    }
                    if (item.getVendor().contains(constraintString))
                    {
                        newValues.add(item);
                        continue;
                    }
                    if (Double.toString(item.getAmount()).contains(constraintString))
                    {
                        newValues.add(item);
                        continue;
                    }
                    if (item.getConsumer() != null && item.getConsumer().getNickname().contains(constraintString))
                    {
                        newValues.add(item);
                        continue;
                    }
                    if (item.getCategory() != null && item.getCategory().getName().contains(constraintString))
                    {
                        newValues.add(item);
                        continue;
                    }
                    if (userCanBeFiltered(item, constraintString))
                    {
                        newValues.add(item);
                        continue;
                    }
                    if (tagCanBeFiltered(item, constraintString))
                    {
                        newValues.add(item);
                    }
                }

                results.values = newValues;
                results.count = newValues.size();
            }
            return results;
        }

        @SuppressWarnings("unchecked")
        protected void publishResults(CharSequence constraint, FilterResults results)
        {
            itemList = (ArrayList<Item>) results.values;
            if (results.count > 0)
            {
                notifyDataSetChanged();
            }
            else
            {
                notifyDataSetInvalidated();
            }
        }
    }

    private boolean userCanBeFiltered(Item item, String constraint)
    {
        if (item.getRelevantUsers() == null)
        {
            return false;
        }

        for (User user : item.getRelevantUsers())
        {
            if (user.getNickname().contains(constraint))
            {
                return true;
            }
            if (user.getEmail().contains(constraint))
            {
                return true;
            }
            if (user.getPhone().contains(constraint))
            {
                return true;
            }
        }
        return false;
    }

    private boolean tagCanBeFiltered(Item item, String constraint)
    {
        if (item.getTags() == null)
        {
            return false;
        }

        for (Tag tag : item.getTags())
        {
            if (tag.getName().contains(constraint))
            {
                return true;
            }
        }
        return false;
    }

    private static class HeaderViewHolder
    {
        TextView headerTextView;
    }

    private static class ItemViewHolder
    {
        ImageView photoImageView ;
        TextView statusTextView ;
        TextView typeTextView ;
        TextView symbolTextView ;
        TextView amountTextView ;
        TextView noteTextView;
        TextView reportTextView ;
        TextView vendorTextView ;
        ImageView categoryImageView ;
    }
}