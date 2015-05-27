package classes.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
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
import classes.utils.ViewUtils;
import classes.widget.CircleImageView;
import classes.widget.PinnedSectionListView;

public class MemberListViewAdapter extends BaseAdapter implements PinnedSectionListView.PinnedSectionListAdapter
{
    private LayoutInflater layoutInflater;
    private List<User> memberList;
    private List<User> chosenList;
    private HashMap<String, Integer> selector = new HashMap<>();
    private ArrayList<Integer> indexList = new ArrayList<>();
    private int selectedColor;
    private int unselectedColor;

    public MemberListViewAdapter(Context context, List<User> userList, List<User> chosenList)
    {
        this.layoutInflater = LayoutInflater.from(context);
        this.memberList = new ArrayList<>(userList);
        this.chosenList = chosenList == null || chosenList.isEmpty() ? new ArrayList<User>() : new ArrayList<>(chosenList);
        this.selectedColor = ViewUtils.getColor(R.color.major_dark);
        this.unselectedColor = ViewUtils.getColor(R.color.font_major_dark);
        initData();
    }

    public View getView(int position, View convertView, ViewGroup parent)
    {
        User user = memberList.get(position);

        if (indexList.contains(position))
        {
            View view = layoutInflater.inflate(R.layout.list_header, parent, false);
            TextView headerTextView = (TextView) view.findViewById(R.id.headerTextView);
            headerTextView.setText(user.getNickname());
            return view;
        }
        else
        {
            View view = layoutInflater.inflate(R.layout.list_member, parent, false);

            boolean isChosen = chosenList.contains(user);

            int color = isChosen ? R.color.list_item_selected : R.color.list_item_unselected;
            view.setBackgroundResource(color);

            CircleImageView imageView = (CircleImageView) view.findViewById(R.id.avatarImageView);
            TextView nicknameTextView = (TextView) view.findViewById(R.id.nicknameTextView);

            ViewUtils.setImageViewBitmap(user, imageView);

            if (user.getNickname().isEmpty())
            {
                nicknameTextView.setText(R.string.not_available);
            }
            else
            {
                nicknameTextView.setText(user.getNickname());
            }

            color = isChosen ? selectedColor : unselectedColor;
            nicknameTextView.setTextColor(color);
            return view;
        }
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

    private void initData()
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

        for (User user : memberList)
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
        memberList.clear();
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
            memberList.add(header);
            memberList.addAll(values);
            count += values.size() + 1;
        }
    }

    public void setMemberList(List<User> userList)
    {
        memberList.clear();
        memberList.addAll(userList);
        initData();
    }

    public void setChosenList(List<User> userList)
    {
        chosenList.clear();
        chosenList.addAll(userList);
    }

    public void setCheck(int position)
    {
        if (!indexList.contains(position))
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
    }

    public List<User> getChosenList()
    {
        return chosenList;
    }

    public HashMap<String, Integer> getSelector()
    {
        return selector;
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