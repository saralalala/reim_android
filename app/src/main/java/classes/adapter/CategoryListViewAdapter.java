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
import classes.utils.ViewUtils;

public class CategoryListViewAdapter extends BaseAdapter
{
    private LayoutInflater layoutInflater;
    private List<Category> categoryList;
    private boolean[] check;
    private int selectedColor;
    private int unselectedColor;

    public CategoryListViewAdapter(Context context, List<Category> categories, boolean[] checkList)
    {
        this.layoutInflater = LayoutInflater.from(context);
        this.categoryList = new ArrayList<>(categories);
        this.check = checkList;
        this.selectedColor = ViewUtils.getColor(R.color.major_dark);
        this.unselectedColor = ViewUtils.getColor(R.color.font_major_dark);
    }

    public View getView(int position, View convertView, ViewGroup parent)
    {
        ViewHolder viewHolder;
        if (convertView == null)
        {
            convertView = layoutInflater.inflate(R.layout.list_category, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.iconImageView = (ImageView) convertView.findViewById(R.id.iconImageView);
            viewHolder.nameTextView = (TextView) convertView.findViewById(R.id.nameTextView);
            viewHolder.noteTextView = (TextView) convertView.findViewById(R.id.noteTextView);

            convertView.setTag(viewHolder);
        }
        else
        {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Category category = categoryList.get(position);

        ViewUtils.setImageViewBitmap(category, viewHolder.iconImageView);

        if (category.getName().isEmpty())
        {
            viewHolder.nameTextView.setText(R.string.not_available);
        }
        else
        {
            viewHolder.nameTextView.setText(category.getName());
        }

        int visibility = category.getNote().isEmpty() ? View.GONE : View.VISIBLE;
        viewHolder.noteTextView.setVisibility(visibility);
        viewHolder.noteTextView.setText(category.getNote());

        if (check != null)
        {
            int color = check[position] ? selectedColor : unselectedColor;
            viewHolder.nameTextView.setTextColor(color);
        }

        return convertView;
    }

    public int getCount()
    {
        return categoryList.size();
    }

    public Category getItem(int position)
    {
        return categoryList.get(position);
    }

    public long getItemId(int position)
    {
        return position;
    }

    public void setCategory(List<Category> categories)
    {
        categoryList.clear();
        categoryList.addAll(categories);
    }

    public void setCheck(boolean[] checkList)
    {
        check = checkList;
    }

    private static class ViewHolder
    {
        ImageView iconImageView;
        TextView nameTextView;
        TextView noteTextView;
    }
}
