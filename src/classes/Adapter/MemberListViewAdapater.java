package classes.Adapter;

import java.util.ArrayList;
import java.util.List;

import classes.AppPreference;
import classes.User;

import com.rushucloud.reim.R;
import database.DBManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class MemberListViewAdapater extends BaseAdapter
{
	private LayoutInflater layoutInflater;
	private List<User> memberList;
	private boolean[] check;
	
	public MemberListViewAdapater(Context context, List<User> userList, boolean[] checkList)
	{
		layoutInflater = LayoutInflater.from(context);
		AppPreference.getAppPreference();
		DBManager.getDBManager();
		memberList = new ArrayList<User>(userList);
		check = checkList;
	}
	
	public View getView(int position, View convertView, ViewGroup parent)
	{
		if (convertView == null)
		{
			convertView = layoutInflater.inflate(R.layout.list_member, parent, false);
		}

		ImageView imageView = (ImageView)convertView.findViewById(R.id.imageView);
		TextView nicknameTextView = (TextView)convertView.findViewById(R.id.nicknameTextView);
		
		User user = memberList.get(position);
		if (!user.getAvatarPath().equals(""))
		{
			Bitmap bitmap = BitmapFactory.decodeFile(user.getAvatarPath());
			imageView.setImageBitmap(bitmap);
		}
		else
		{
			imageView.setImageResource(R.drawable.default_avatar);
		}
		
		if (user.getNickname().equals(""))
		{
			nicknameTextView.setText(R.string.notAvailable);
		}
		else
		{
			nicknameTextView.setText(user.getNickname());			
		}

		int color = check[position] ? R.color.list_item_selected : R.color.list_item_not_selected;
		convertView.setBackgroundResource(color);
		
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
		memberList = new ArrayList<User>(userList);
	}
	
	public void setCheck(boolean[] checkList)
	{
		check = checkList;
	}
}
