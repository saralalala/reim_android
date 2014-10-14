package classes.Adapter;

import classes.AppPreference;
import classes.Group;
import classes.User;

import com.rushucloud.reim.R;
import com.rushucloud.reim.start.SignInActivity;
import com.rushucloud.reim.MainActivity;

import database.DBManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

public class MeListViewAdapater extends BaseAdapter
{
	private LayoutInflater layoutInflater;
	private MainActivity activity;
	private AppPreference appPreference;
	
	public MeListViewAdapater(Context context)
	{
		layoutInflater = LayoutInflater.from(context);
		activity = (MainActivity)context;
		appPreference = AppPreference.getAppPreference();
	}
	
	public View getView(int position, View convertView, ViewGroup parent)
	{
		View view = null;
		switch (position)
		{
			case 0:
			{
				DBManager dbManager = DBManager.getDBManager();
				User currentUser = dbManager.getUser(appPreference.getCurrentUserID());
				Group group = dbManager.getGroup(appPreference.getCurrentGroupID());
				
				view = layoutInflater.inflate(R.layout.list_item_profile, null);
				
				ImageView imageView = (ImageView)view.findViewById(R.id.imageView);
				if (currentUser.getAvatarPath().equals(""))
				{
					imageView.setImageResource(R.drawable.default_avatar);
				}
				else
				{
					Bitmap bitmap = BitmapFactory.decodeFile(currentUser.getAvatarPath());
					imageView.setImageBitmap(bitmap);
				}
				
				TextView nicknameTextView = (TextView)view.findViewById(R.id.nicknameTextView);
				nicknameTextView.setText(currentUser.getNickname());
				TextView companyTextView = (TextView)view.findViewById(R.id.companyTextView);
				companyTextView.setText(group.getName());
				break;
			}
			case 1:
			{
				view = layoutInflater.inflate(R.layout.list_item_toggle, null);
				TextView textView = (TextView)view.findViewById(R.id.textView);
				textView.setText(activity.getString(R.string.enablePasswordProtection));
				final ToggleButton toggleButton = (ToggleButton)view.findViewById(R.id.toggleButton);
				toggleButton.setChecked(appPreference.passwordProtectionEnabled());
				toggleButton.setOnClickListener(new View.OnClickListener()
				{
					public void onClick(View v)
					{
						appPreference.setEnablePasswordProtection(toggleButton.isChecked());
						appPreference.saveAppPreference();
					}
				});
				break;
			}
			case 2:
			{
				view = layoutInflater.inflate(R.layout.list_item_toggle, null);
				TextView textView = (TextView)view.findViewById(R.id.textView);
				textView.setText(R.string.syncWithoutWifi);
				final ToggleButton toggleButton = (ToggleButton)view.findViewById(R.id.toggleButton);
				toggleButton.setChecked(appPreference.syncWithoutWifi());
				toggleButton.setOnClickListener(new View.OnClickListener()
				{
					public void onClick(View v)
					{
						appPreference.setSyncWithoutWifi(toggleButton.isChecked());
						appPreference.saveAppPreference();
					}
				});
				break;
			}
			case 3:
			{
				view = layoutInflater.inflate(android.R.layout.simple_list_item_1, null);
				TextView textView = (TextView)view.findViewById(android.R.id.text1);
				textView.setText(activity.getString(R.string.preference));
				break;
			}
			case 4:
			{
				view = layoutInflater.inflate(android.R.layout.simple_list_item_1, null);
				TextView textView = (TextView)view.findViewById(android.R.id.text1);
				textView.setText(activity.getString(R.string.share));
				break;
			}
			case 5:
			{
				view = layoutInflater.inflate(android.R.layout.simple_list_item_1, null);
				TextView textView = (TextView)view.findViewById(android.R.id.text1);
				textView.setText(activity.getString(R.string.feedback));
				break;
			}
			case 6:
			{
				view = layoutInflater.inflate(R.layout.list_item_button, null);
				Button button = (Button)view.findViewById(R.id.button);
				button.setText(activity.getString(R.string.signOut));
				button.setOnClickListener(new View.OnClickListener()
				{
					public void onClick(View v)
					{
						AppPreference appPreference = AppPreference.getAppPreference();
						appPreference.setUsername("");
						appPreference.setPassword("");
						appPreference.setServerToken("");
						appPreference.saveAppPreference();
						activity.startActivity(new Intent(activity.getBaseContext(), SignInActivity.class));
						activity.finish();
					}
				});
				break;
			}
			default:
				break;
		}
		return view;
	}
	
	public int getCount()
	{
		return 7;
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
