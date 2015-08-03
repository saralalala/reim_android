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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import classes.model.User;
import classes.utils.CharacterParser;
import classes.utils.Constant;
import classes.utils.PhoneUtils;
import classes.utils.ViewUtils;
import classes.widget.PinnedSectionListView;

public class InviteListViewAdapter extends BaseAdapter implements PinnedSectionListView.PinnedSectionListAdapter
{
    private Context context;
    private LayoutInflater layoutInflater;
    private ArrayList<String> inputList = new ArrayList<>();
    private ArrayList<String> inputChosenList = new ArrayList<>();
    private List<User> contactList = new ArrayList<>();
    private List<User> contactChosenList = new ArrayList<>();
    private HashMap<String, Integer> selector = new HashMap<>();
    private ArrayList<Integer> indexList = new ArrayList<>();
    private boolean noPermission = false;

    public InviteListViewAdapter(Context context)
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

            int visibility = inputChosenList.contains(contact) ? View.VISIBLE : View.INVISIBLE;
            ImageView checkImageView = (ImageView) view.findViewById(R.id.checkImageView);
            checkImageView.setVisibility(visibility);

            TextView contactTextView = (TextView) view.findViewById(R.id.contactTextView);
            contactTextView.setText(contact);

            return view;
        }
        else if (!contactList.isEmpty() && indexList.contains(position))
        {
            User user = contactList.get(position - inputList.size() - 1);

            View view = layoutInflater.inflate(R.layout.list_header, parent, false);

            TextView headerTextView = (TextView) view.findViewById(R.id.headerTextView);
            headerTextView.setText(user.getNickname());

            return view;
        }
        else if (!contactList.isEmpty())
        {
            User user = contactList.get(position - inputList.size() - 1);

            View view = layoutInflater.inflate(R.layout.list_contact, parent, false);

            int visibility = User.indexOfContactList(contactChosenList, user) > -1 ? View.VISIBLE : View.INVISIBLE;
            ImageView checkImageView = (ImageView) view.findViewById(R.id.checkImageView);
            checkImageView.setVisibility(visibility);

            TextView nameTextView = (TextView) view.findViewById(R.id.nameTextView);
            nameTextView.setText(user.getNickname());

            String contact = user.getPhone().isEmpty() ? user.getEmail() : user.getPhone();
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
                    if (PhoneUtils.isMIUIV6())
                    {
                        try
                        {
                            Intent intent = new Intent("miui.intent.action.APP_PERM_EDITOR");
                            intent.addCategory(Intent.CATEGORY_DEFAULT);
                            intent.putExtra("extra_pkgname", context.getPackageName());
                            context.startActivity(intent);
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                    else
                    {
                        context.startActivity(new Intent(Settings.ACTION_APPLICATION_SETTINGS));
                    }
                }
            });

            return view;
        }
    }

    public int getCount()
    {
        if (contactList.isEmpty() && noPermission)
        {
            return inputList.size() + 2;
        }
        else if (contactList.isEmpty())
        {
            return inputList.size() + 1;
        }
        else
        {
            return inputList.size() + contactList.size() + 1;
        }
    }

    public User getItem(int position)
    {
        return contactList.get(position - inputList.size() - 1);
    }

    public long getItemId(int position)
    {
        return position;
    }

    public int getViewTypeCount()
    {
        return 2;
    }

    public int getItemViewType(int position)
    {
        return indexList.contains(position) ? Constant.VIEW_TYPE_HEADER : Constant.VIEW_TYPE_CONTENT;
    }

    public boolean isItemViewTypePinned(int viewType)
    {
        return viewType == 1;
    }

    public void initIndex()
    {
        TreeMap<String, ArrayList<User>> indexMap = new TreeMap<>(new Comparator<String>()
        {
            public int compare(String s, String s2)
            {
                if (s.equals(s2))
                {
                    return 0;
                }
                else if (s.equals("#"))
                {
                    return 1;
                }
                else if (s2.equals("#"))
                {
                    return -1;
                }
                else
                {
                    return s.compareTo(s2);
                }
            }
        });

        for (User user : contactList)
        {
            String initLetter = CharacterParser.getInitLetter(user.getNickname());
            ArrayList<User> letterUserList = indexMap.get(initLetter);
            if (letterUserList == null)
            {
                letterUserList = new ArrayList<>();
            }
            letterUserList.add(user);
            indexMap.put(initLetter, letterUserList);
        }

        int count = inputList.size() + 1;
        selector.clear();
        selector.put(ViewUtils.getString(R.string.manual), 0);
        contactList.clear();
        indexList.clear();
        for (Map.Entry<String, ArrayList<User>> entry : indexMap.entrySet())
        {
            String key = entry.getKey();
            ArrayList<User> values = entry.getValue();
            if (key.equals("#"))
            {
                Collections.sort(values, new Comparator<User>()
                {
                    public int compare(User user, User user2)
                    {
                        return user.getNickname().compareTo(user2.getNickname());
                    }
                });
            }
            selector.put(key, count);
            indexList.add(count);

            User header = new User();
            header.setNickname(key);
            contactList.add(header);
            contactList.addAll(values);
            count += values.size() + 1;
        }
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

    public HashMap<String, Integer> getSelector()
    {
        return selector;
    }

    public boolean isContact(int position)
    {
        return !indexList.contains(position);
    }
}