package classes.adapter;

import java.util.ArrayList;
import java.util.List;

import com.rushucloud.reim.R;

import classes.Category;
import classes.utils.ViewUtils;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

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
		
		this.categoryList = new ArrayList<Category>(categories);
		this.subCategoryList = new ArrayList<List<Category>>(subCategories);
		this.checkList = new ArrayList<Boolean>(check);
		this.subCheckList = new ArrayList<List<Boolean>>(subCheck);
		
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
		if (convertView == null)
		{
			convertView = layoutInflater.inflate(R.layout.list_category_expandable, parent, false);
		}
		convertView.setBackgroundResource(R.drawable.me_item_drawable);
		
		ImageView iconImageView = (ImageView)convertView.findViewById(R.id.iconImageView);
		TextView nameTextView = (TextView)convertView.findViewById(R.id.nameTextView);
		
		Category category = categoryList.get(groupPosition);
		
		iconImageView.setImageResource(R.drawable.default_icon);
		if (!category.getIconPath().equals(""))
		{
			Bitmap bitmap = BitmapFactory.decodeFile(category.getIconPath());
			if (bitmap != null)
			{
				iconImageView.setImageBitmap(bitmap);				
			}
		}
		
		if (category.getName().equals(""))
		{
			nameTextView.setText(R.string.not_available);
		}
		else
		{
			nameTextView.setText(category.getName());			
		}

		int color = checkList.get(groupPosition) ? selectedColor : unselectedColor;
		nameTextView.setTextColor(color);
		
		return convertView;
	}

	public View getChildView(int groupPosition, int childPosition, boolean isLastChild,	View convertView, ViewGroup parent)
	{
		if (convertView == null)
		{
			convertView = layoutInflater.inflate(R.layout.list_category_expandable, parent, false);
		}		

		ImageView iconImageView = (ImageView)convertView.findViewById(R.id.iconImageView);
		TextView nameTextView = (TextView)convertView.findViewById(R.id.nameTextView);
		
		Category category = subCategoryList.get(groupPosition).get(childPosition);
		
		iconImageView.setImageResource(R.drawable.default_icon);
		if (!category.getIconPath().equals(""))
		{
			Bitmap bitmap = BitmapFactory.decodeFile(category.getIconPath());
			if (bitmap != null)
			{
				iconImageView.setImageBitmap(bitmap);				
			}
		}
		
		if (category.getName().equals(""))
		{
			nameTextView.setText(R.string.not_available);
		}
		else
		{
			nameTextView.setText(category.getName());			
		}

		int color = subCheckList.get(groupPosition).get(childPosition) ? selectedColor : unselectedColor;
		nameTextView.setTextColor(color);
		
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
}