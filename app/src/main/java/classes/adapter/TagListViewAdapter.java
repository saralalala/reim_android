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

import classes.model.Tag;
import classes.utils.ViewUtils;

public class TagListViewAdapter extends BaseAdapter
{
    private LayoutInflater layoutInflater;
    private List<Tag> tagList;
    private boolean[] check;
    private int selectedColor;
    private int unselectedColor;

    public TagListViewAdapter(Context context, List<Tag> tags, boolean[] checkList)
    {
        this.layoutInflater = LayoutInflater.from(context);
        this.tagList = new ArrayList<>(tags);
        this.check = checkList;
        this.selectedColor = ViewUtils.getColor(R.color.major_dark);
        this.unselectedColor = ViewUtils.getColor(R.color.font_major_dark);
    }

    public View getView(int position, View convertView, ViewGroup parent)
    {
        ViewHolder viewHolder;
        if (convertView == null)
        {
            convertView = layoutInflater.inflate(R.layout.list_tag, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.nameTextView = (TextView) convertView.findViewById(R.id.nameTextView);

            convertView.setTag(viewHolder);
        }
        else
        {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        if (check != null)
        {
            int color = check[position] ? R.color.list_item_pressed : R.color.list_item_unpressed;
            convertView.setBackgroundResource(color);
        }

        Tag tag = tagList.get(position);

        if (tag.getName().isEmpty())
        {
            viewHolder.nameTextView.setText(R.string.not_available);
        }
        else
        {
            viewHolder.nameTextView.setText(tag.getName());
        }

        if (check != null)
        {
            int color = check[position] ? selectedColor : unselectedColor;
            viewHolder.nameTextView.setTextColor(color);
        }

        return convertView;
    }

    public int getCount()
    {
        return tagList.size();
    }

    public Tag getItem(int position)
    {
        return tagList.get(position);
    }

    public long getItemId(int position)
    {
        return position;
    }

    public void setTagList(List<Tag> tags)
    {
        tagList.clear();
        tagList.addAll(tags);
    }

    public void setCheck(boolean[] checkList)
    {
        check = checkList;
    }

    private static class ViewHolder
    {
        TextView nameTextView;
    }
}