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
import classes.utils.PhoneUtils;
import classes.utils.ViewUtils;
import classes.widget.PinnedSectionListView;

public class ContactListViewAdapter extends BaseAdapter implements PinnedSectionListView.PinnedSectionListAdapter
{
    private Context context;
    private LayoutInflater layoutInflater;
    private List<User> contactList = new ArrayList<>();
    private List<User> contactChosenList = new ArrayList<>();
    private HashMap<String, Integer> selector = new HashMap<>();
    private ArrayList<Integer> indexList = new ArrayList<>();
    private boolean noPermission = false;

    public ContactListViewAdapter(Context context)
    {
        this.context = context;
        this.layoutInflater = LayoutInflater.from(context);
    }

    public View getView(int position, View convertView, ViewGroup parent)
    {
        if (noPermission)
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
        else if (indexList.contains(position))
        {
            User user = contactList.get(position);

            View view = layoutInflater.inflate(R.layout.list_header, parent, false);

            TextView headerTextView = (TextView) view.findViewById(R.id.headerTextView);
            headerTextView.setText(user.getNickname());

            return view;
        }
        else
        {
            User user = contactList.get(position);

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
    }

    public int getCount()
    {
        return noPermission ? 1 : contactList.size();
    }

    public User getItem(int position)
    {
        return contactList.get(position);
    }

    public long getItemId(int position)
    {
        return position;
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

        int count = 0;
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

    public int getViewTypeCount()
    {
        return 2;
    }

    public int getItemViewType(int position)
    {
        return indexList.contains(position) ? 1 : 0;
    }

    public boolean isItemViewTypePinned(int viewType)
    {
        return viewType == 1;
    }
}