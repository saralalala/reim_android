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

import classes.User;
import classes.utils.ViewUtils;

public class MemberListViewAdapter extends BaseAdapter
{
	private LayoutInflater layoutInflater;
	private List<User> memberList;
	private List<User> chosenList;
    private int selectedColor;
    private int unselectedColor;
	
	public MemberListViewAdapter(Context context, List<User> userList, List<User> chosenList)
	{
		this.layoutInflater = LayoutInflater.from(context);
		this.memberList = new ArrayList<User>(userList);
        this.chosenList = chosenList == null || chosenList.isEmpty()? new ArrayList<User>() : new ArrayList<User>(chosenList);
        this.selectedColor = ViewUtils.getColor(R.color.major_dark);
        this.unselectedColor = ViewUtils.getColor(R.color.font_major_dark);
	}
	
	public View getView(int position, View convertView, ViewGroup parent)
	{
		if (convertView == null)
		{
			convertView = layoutInflater.inflate(R.layout.list_member, parent, false);
		}

        User user = memberList.get(position);
        boolean isChosen = chosenList.contains(user);

        int color = isChosen ? R.color.list_item_selected : R.color.list_item_unselected;
        convertView.setBackgroundResource(color);

		ImageView imageView = (ImageView) convertView.findViewById(R.id.avatarImageView);
		TextView nicknameTextView = (TextView) convertView.findViewById(R.id.nicknameTextView);

        ViewUtils.setImageViewBitmap(user, imageView);

		if (user.getNickname().isEmpty())
		{
			nicknameTextView.setText(R.string.not_available);
		}
		else
		{
			nicknameTextView.setText(user.getNickname());			
		}

		color = isChosen? selectedColor : unselectedColor;
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

    public void setMemberList(List<User> userList)
    {
        memberList.clear();
        memberList.addAll(userList);
    }

    public void setChosenList(List<User> userList)
    {
        chosenList.clear();
        chosenList.addAll(userList);
    }

	public void setCheck(int position)
	{
        User user = memberList.get(position);
        if (chosenList.contains(user))
        {
            chosenList.remove(user);
        }
        else
        {
            chosenList.add(user);
        }
	}

    public List<User> getChosenList()
    {
        return chosenList;
    }
}