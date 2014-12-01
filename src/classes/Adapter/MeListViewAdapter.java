package classes.Adapter;

import classes.AppPreference;
import classes.Group;
import classes.User;
import classes.Widget.CircleImageView;

import com.rushucloud.reim.ImageActivity;
import com.rushucloud.reim.MeFragment;
import com.rushucloud.reim.R;
import database.DBManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class MeListViewAdapter extends BaseAdapter
{
	private LayoutInflater layoutInflater;
	private MeFragment fragment;
	
	public MeListViewAdapter(MeFragment fragment)
	{
		this.layoutInflater = LayoutInflater.from(fragment.getActivity());
		this.fragment = (MeFragment)fragment;
	}
	
	public View getView(int position, View convertView, ViewGroup parent)
	{
		AppPreference appPreference = AppPreference.getAppPreference();
		DBManager dbManager = DBManager.getDBManager();
		
		final User currentUser = appPreference.getCurrentUser();
		Group group = dbManager.getGroup(appPreference.getCurrentGroupID());
		
		convertView = layoutInflater.inflate(R.layout.list_profile, parent, false);
		
		CircleImageView imageView = (CircleImageView)convertView.findViewById(R.id.imageView);	
		TextView nicknameTextView = (TextView)convertView.findViewById(R.id.nicknameTextView);
		TextView companyTextView = (TextView)convertView.findViewById(R.id.companyTextView);		
		
		if (currentUser != null)
		{
			if (!currentUser.getAvatarPath().equals(""))
			{
				Bitmap bitmap = BitmapFactory.decodeFile(currentUser.getAvatarPath());
				if (bitmap != null)
				{
					imageView.setImageBitmap(bitmap);						
				}
			}
			imageView.setOnClickListener(new View.OnClickListener()
			{
				public void onClick(View v)
				{
					if (currentUser != null && !currentUser.getAvatarPath().equals(""))
					{
						Intent intent = new Intent(fragment.getActivity(), ImageActivity.class);
						intent.putExtra("imagePath", currentUser.getAvatarPath());
						fragment.getActivity().startActivity(intent);
					}
				}
			});
			fragment.registerForContextMenu(imageView);
			
			nicknameTextView.setText(currentUser.getNickname());					
		}
		else
		{
			imageView.setImageResource(R.drawable.default_avatar);
			companyTextView.setText(R.string.notAvailable);
		}
		
		if (group != null)
		{
			companyTextView.setText(group.getName());
		}
		else
		{
			companyTextView.setText(R.string.notAvailable);
		}
		
		return convertView;
	}
	
	public int getCount()
	{
		return 1;
	}

	public Object getItem(int position)
	{
		return null;
	}

	public long getItemId(int position)
	{
		return 0;
	}
}
