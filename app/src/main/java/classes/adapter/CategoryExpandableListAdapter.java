package classes.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.rushucloud.reim.R;

import java.util.ArrayList;
import java.util.List;

import classes.model.Category;
import classes.utils.ViewUtils;

public class CategoryExpandableListAdapter extends BaseExpandableListAdapter
{
    private LayoutInflater layoutInflater;
    private List<Category> categoryList;
    private List<List<Category>> subCategoryList;
    private List<Boolean> checkList;
    private List<List<Boolean>> subCheckList;
    private int selectedColor;
    private int unselectedColor;

    public CategoryExpandableListAdapter(Context context, List<Category> categories, List<List<Category>> subCategories,
                                         List<Boolean> check, List<List<Boolean>> subCheck)
    {
        this.layoutInflater = LayoutInflater.from(context);
        this.categoryList = new ArrayList<>(categories);
        this.subCategoryList = new ArrayList<>(subCategories);
        this.checkList = new ArrayList<>(check);
        this.subCheckList = new ArrayList<>(subCheck);
        this.selectedColor = ViewUtils.getColor(R.color.major_dark);
        this.unselectedColor = ViewUtils.getColor(R.color.font_major_dark);
    }

    public int getGroupCount()
    {
        return categoryList.size();
    }

    public int getChildrenCount(int groupPosition)
    {
        return subCategoryList.get(groupPosition).size();
    }

    public Category getGroup(int groupPosition)
    {
        return categoryList.get(groupPosition);
    }

    public Category getChild(int groupPosition, int childPosition)
    {
        return subCategoryList.get(groupPosition).get(childPosition);
    }

    public long getGroupId(int groupPosition)
    {
        return 0;
    }

    public long getChildId(int groupPosition, int childPosition)
    {
        return 0;
    }

    public boolean hasStableIds()
    {
        return false;
    }

    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent)
    {
        GroupViewHolder groupViewHolder;
        if (convertView == null)
        {
            convertView = layoutInflater.inflate(R.layout.list_category_expandable, parent, false);

            groupViewHolder = new GroupViewHolder();
            groupViewHolder.iconImageView = (ImageView) convertView.findViewById(R.id.iconImageView);
            groupViewHolder.nameTextView = (TextView) convertView.findViewById(R.id.nameTextView);
            groupViewHolder.noteTextView = (TextView) convertView.findViewById(R.id.noteTextView);

            convertView.setTag(groupViewHolder);
        }
        else
        {
            groupViewHolder = (GroupViewHolder) convertView.getTag();
        }
        convertView.setBackgroundResource(R.drawable.me_item_drawable);

        Category category = categoryList.get(groupPosition);

        ViewUtils.setImageViewBitmap(category, groupViewHolder.iconImageView);

        if (category.getName().isEmpty())
        {
            groupViewHolder.nameTextView.setText(R.string.not_available);
        }
        else
        {
            groupViewHolder. nameTextView.setText(category.getName());
        }

        int visibility = category.getNote().isEmpty() ? View.GONE : View.VISIBLE;
        groupViewHolder.noteTextView.setVisibility(visibility);
        groupViewHolder.noteTextView.setText(category.getNote());

        int color = checkList.get(groupPosition) ? selectedColor : unselectedColor;
        groupViewHolder.nameTextView.setTextColor(color);

        return convertView;
    }

    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent)
    {
        ChildViewHolder childViewHolder;
        if (convertView == null)
        {
            convertView = layoutInflater.inflate(R.layout.list_category_expandable, parent, false);

            childViewHolder = new ChildViewHolder();
            childViewHolder.iconImageView = (ImageView) convertView.findViewById(R.id.iconImageView);
            childViewHolder.nameTextView = (TextView) convertView.findViewById(R.id.nameTextView);
            childViewHolder.noteTextView = (TextView) convertView.findViewById(R.id.noteTextView);

            convertView.setTag(childViewHolder);
        }
        else
        {
            childViewHolder = (ChildViewHolder) convertView.getTag();
        }

        Category category = subCategoryList.get(groupPosition).get(childPosition);

        ViewUtils.setImageViewBitmap(category, childViewHolder.iconImageView);

        childViewHolder.nameTextView.setText(R.string.not_available);

        if (category.getName().isEmpty())
        {
            childViewHolder.nameTextView.setText(R.string.not_available);
        }
        else
        {
            childViewHolder.nameTextView.setText(category.getName());
        }

        int visibility = category.getNote().isEmpty() ? View.GONE : View.VISIBLE;
        childViewHolder.noteTextView.setVisibility(visibility);
        childViewHolder.noteTextView.setText(category.getNote());

        int color = subCheckList.get(groupPosition).get(childPosition) ? selectedColor : unselectedColor;
        childViewHolder.nameTextView.setTextColor(color);

        return convertView;
    }

    public boolean isChildSelectable(int groupPosition, int childPosition)
    {
        return true;
    }

    public void setCategory(List<Category> categories, List<List<Category>> subCategories)
    {
        categoryList.clear();
        categoryList.addAll(categories);
        subCategoryList.clear();
        subCategoryList.addAll(subCategories);
    }

    public void setCheck(List<Boolean> check, List<List<Boolean>> subCheck)
    {
        checkList.clear();
        checkList.addAll(check);
        subCheckList.clear();
        subCheckList.addAll(subCheck);
    }

    private static class GroupViewHolder
    {
        ImageView iconImageView;
        TextView nameTextView;
        TextView noteTextView;
    }

    private static class ChildViewHolder
    {
        ImageView iconImageView;
        TextView nameTextView;
        TextView noteTextView;
    }
}