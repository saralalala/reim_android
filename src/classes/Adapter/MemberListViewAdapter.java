package classes.adapter;

import java.util.ArrayList;
import java.util.List;

import classes.User;

import com.rushucloud.reim.R;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class MemberListViewAdapter extends BaseAdapter
{
	private LayoutInflater layoutInflater;
	private List<User> memberList;
	private boolean[] check;
	private int selectedColor;
	private int unselectedColor;
	
	public MemberListViewAdapter(Context context, List<User> userList, boolean[] checkList)
	{
		this.layoutInflater = LayoutInflater.from(context);
		
		this.memberList = new ArrayList<User>(userList);
		this.check = checkList;
		this.selectedColor = context.getResources().getColor(R.color.major_dark);
		this.unselectedColor = context.getResources().getColor(R.color.font_major_dark);
	}
	
	public View getView(int position, View convertView, ViewGroup parent)
	{
		if (convertView == null)
		{
			convertView = layoutInflater.inflate(R.layout.list_member, parent, false);
		}
		
		if (check != null)
		{
			int color = check[position] ? R.color.list_item_selected : R.color.list_item_unselected;
			convertView.setBackgroundResource(color);
		}

		ImageView imageView = (ImageView)convertView.findViewById(R.id.imageView);
		TextView nicknameTextView = (TextView)convertView.findViewById(R.id.nicknameTextView);
				
		User user = memberList.get(position);

		imageView.setImageResource(R.drawable.default_avatar);
		if (!user.getAvatarPath().equals(""))
		{
			Bitmap bitmap = BitmapFactory.decodeFile(user.getAvatarPath());
			if (bitmap != null)
			{
				imageView.setImageBitmap(bitmap);				
			}
		}

		if (user.getNickname().equals(""))
		{
			nicknameTextView.setText(R.string.not_available);
		}
		else
		{
			nicknameTextView.setText(user.getNickname());			
		}

		int color = check[position] ? selectedColor : unselectedColor;
		nicknameTextView.setTextColor(color);
		
		return convertView;
	}
	
	public int getCount()
	{
		return memberList.size();
	}

	public User getItem(int position)
	{
		return memberList.get(position);
	}

	public long getItemId(int position)
	{
		return position;
	}
	
	public void setMember(List<User> userList)
	{
		memberList.clear();
		memberList.addAll(userList);
	}
	
	public void setCheck(boolean[] checkList)
	{
		check = checkList;
	}
}
