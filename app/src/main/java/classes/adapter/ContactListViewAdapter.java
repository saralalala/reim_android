package classes.adapter;

import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.rushucloud.reim.R;

import java.util.ArrayList;
import java.util.List;

import classes.base.User;

public class ContactListViewAdapter extends BaseAdapter
{
	private Context context;
	private LayoutInflater layoutInflater;
    private ArrayList<String> inputList = new ArrayList<>();
    private ArrayList<String> inputChosenList = new ArrayList<>();
    private List<User> contactList = new ArrayList<>();
    private List<User> contactChosenList = new ArrayList<>();
    private boolean noPermission = false;

	public ContactListViewAdapter(Context context)
	{
        this.context = context;
		this.layoutInflater = LayoutInflater.from(context);
	}

	public View getView(int position, View convertView, ViewGroup parent)
	{
        if (position == 0)
        {
            return layoutInflater.inflate(R.layout.list_input_name, parent, false);
        }
        else if (position > 0 && position < inputList.size() + 1)
        {
            String contact = inputList.get(position - 1);

            View view = layoutInflater.inflate(R.layout.list_contact_input, parent, false);

            int visibility = inputChosenList.contains(contact)? View.VISIBLE : View.INVISIBLE;
            ImageView checkImageView = (ImageView) view.findViewById(R.id.checkImageView);
            checkImageView.setVisibility(visibility);

            TextView contactTextView = (TextView) view.findViewById(R.id.contactTextView);
            contactTextView.setText(contact);

            return view;
        }
        else if (position == inputList.size() + 1)
        {
            View view = layoutInflater.inflate(R.layout.list_header, parent, false);

            TextView headerTextView = (TextView) view.findViewById(R.id.headerTextView);
            headerTextView.setText(R.string.contact);

            return view;
        }
        else if (!contactList.isEmpty())
        {
            User user = contactList.get(position - inputList.size() - 2);

            View view = layoutInflater.inflate(R.layout.list_contact, parent, false);

            int visibility = User.indexOfContactList(contactChosenList, user) > -1? View.VISIBLE : View.INVISIBLE;
            ImageView checkImageView = (ImageView) view.findViewById(R.id.checkImageView);
            checkImageView.setVisibility(visibility);

            TextView nameTextView = (TextView) view.findViewById(R.id.nameTextView);
            nameTextView.setText(user.getNickname());

            String contact = user.getPhone().isEmpty()? user.getEmail() : user.getPhone();
            TextView contactTextView = (TextView) view.findViewById(R.id.contactTextView);
            contactTextView.setText(contact);

            return view;
        }
        else
        {
            View view = layoutInflater.inflate(R.layout.list_contact_no_permission, parent, false);

            TextView settingsButton = (TextView) view.findViewById(R.id.settingsButton);
            settingsButton.setOnClickListener(new View.OnClickListener()
            {
                public void onClick(View v)
                {
                    context.startActivity(new Intent(Settings.ACTION_APPLICATION_SETTINGS));
                }
            });

            return view;
        }
	}
	
	public int getCount()
	{
        if (contactList.isEmpty() && noPermission)
        {
            return inputList.size() + 3;
        }
        else if (contactList.isEmpty())
        {
            return inputList.size() + 2;
        }
        else
        {
            return inputList.size() + contactList.size() + 2;
        }
	}

	public Object getItem(int position)
	{
		return null;
	}

	public long getItemId(int position)
	{
		return position;
	}

    public void setInputList(ArrayList<String> inputs)
    {
        inputList.clear();
        inputList.addAll(inputs);
    }

    public void setInputChosenList(ArrayList<String> inputs)
    {
        inputChosenList.clear();
        inputChosenList.addAll(inputs);
    }

    public void setContactList(List<User> contacts)
    {
        contactList.clear();
        contactList.addAll(contacts);
    }

    public void setContactChosenList(List<User> contacts)
    {
        contactChosenList.clear();
        contactChosenList.addAll(contacts);
    }

    public void setNoPermission(boolean noPermission)
    {
        this.noPermission = noPermission;
    }
}