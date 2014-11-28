package classes.Adapter;

import netUtils.HttpConnectionCallback;
import netUtils.Request.User.SignOutRequest;
import netUtils.Response.User.SignOutResponse;
import classes.AppPreference;
import classes.ReimApplication;
import classes.Utils;
import com.rushucloud.reim.R;
import com.rushucloud.reim.me.SettingsActivity;
import com.rushucloud.reim.start.SignInActivity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class SettingsListViewAdapter extends BaseAdapter
{
	private LayoutInflater layoutInflater;
	private SettingsActivity activity;
	
	public SettingsListViewAdapter(SettingsActivity activity)
	{
		this.layoutInflater = LayoutInflater.from(activity);
		this.activity = activity;
	}
	
	public View getView(int position, View convertView, ViewGroup parent)
	{
		View view = null;
		switch (position)
		{
			case 0:
			{
				view = layoutInflater.inflate(R.layout.list_text, parent, false);

				TextView textView = (TextView)view.findViewById(R.id.textView);
				textView.setText(R.string.aboutUs);
				
				ImageView nextImageView = (ImageView)view.findViewById(R.id.nextImageView);
				nextImageView.setVisibility(View.GONE);
				break;
			}
			case 1:
			{
				view = layoutInflater.inflate(R.layout.list_text, parent, false);

				TextView textView = (TextView)view.findViewById(R.id.textView);
				textView.setText(R.string.feedback);
				
				ImageView nextImageView = (ImageView)view.findViewById(R.id.nextImageView);
				nextImageView.setVisibility(View.GONE);
				break;
			}
			case 2:
			{
				view = layoutInflater.inflate(R.layout.list_button, parent, false);
				Button button = (Button)view.findViewById(R.id.button);
				button.setText(activity.getString(R.string.signOut));
				button.setOnClickListener(new View.OnClickListener()
				{
					public void onClick(View v)
					{
						if (Utils.isNetworkConnected())
						{
							sendSignOutRequest();							
						}
						else
						{
							Utils.showToast(activity, "网络未连接，无法登出");							
						}
					}
				});
				break;
			}
//			case 3:
//			{
//				view = layoutInflater.inflate(R.layout.list_toggle, parent, false);
//				TextView textView = (TextView)view.findViewById(R.id.textView);
//				textView.setText(R.string.syncWithoutWifi);
//				final ToggleButton toggleButton = (ToggleButton)view.findViewById(R.id.toggleButton);
//				toggleButton.setChecked(appPreference.syncOnlyWithWifi());
//				toggleButton.setOnClickListener(new View.OnClickListener()
//				{
//					public void onClick(View v)
//					{
//						if (toggleButton.isChecked())
//						{
//							MobclickAgent.onEvent(activity, "UMENG_WIFI_UPLOAD");
//						}
//						appPreference.setSyncOnlyWithWifi(toggleButton.isChecked());
//						appPreference.saveAppPreference();
//					}
//				});
//				break;
//			}
//			case 4:
//			{
//				view = layoutInflater.inflate(R.layout.list_item_toggle, null);
//				TextView textView = (TextView)view.findViewById(R.id.textView);
//				textView.setText(fragment.getString(R.string.enablePasswordProtection));
//				final ToggleButton toggleButton = (ToggleButton)view.findViewById(R.id.toggleButton);
//				toggleButton.setChecked(appPreference.passwordProtectionEnabled());
//				toggleButton.setOnClickListener(new View.OnClickListener()
//				{
//					public void onClick(View v)
//					{
//						appPreference.setEnablePasswordProtection(toggleButton.isChecked());
//						appPreference.saveAppPreference();
//					}
//				});
//				break;
//			}
			default:
				break;
		}
		return view;
	}
	
	public int getCount()
	{
		return 3;
	}

	public Object getItem(int position)
	{
		return null;
	}

	public long getItemId(int position)
	{
		return 0;
	}
	
	private void sendSignOutRequest()
	{
		ReimApplication.showProgressDialog();
		SignOutRequest request = new SignOutRequest();
		request.sendRequest(new HttpConnectionCallback()
		{
			public void execute(Object httpResponse)
			{
				SignOutResponse response = new SignOutResponse(httpResponse);
				if (response.getStatus())
				{
					AppPreference appPreference = AppPreference.getAppPreference();
					appPreference.setCurrentUserID(-1);
					appPreference.setCurrentGroupID(-1);
					appPreference.setUsername("");
					appPreference.setPassword("");
					appPreference.setServerToken("");
					appPreference.setLastSyncTime(0);
					appPreference.saveAppPreference();
					
					ReimApplication.setTabIndex(0);
					ReimApplication.setReportTabIndex(0);
					
					activity.runOnUiThread(new Runnable()
					{
						public void run()
						{						
							ReimApplication.dismissProgressDialog();	
							activity.startActivity(new Intent(activity, SignInActivity.class));
							activity.finish();
						}
					});
				}
				else 
				{
					activity.runOnUiThread(new Runnable()
					{
						public void run()	
						{
							ReimApplication.dismissProgressDialog();
							Utils.showToast(activity, "登出失败");
						}
					});
				}
			}
		});
	}
}
