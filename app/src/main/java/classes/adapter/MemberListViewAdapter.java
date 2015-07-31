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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import classes.model.User;
import classes.utils.CharacterParser;
import classes.utils.Constant;
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
            HeaderViewHolder headerViewHolder;

            if(convertView == null)
            {
                convertView= layoutInflater.inflate(R.layout.list_header, parent, false);

                headerViewHolder = new HeaderViewHolder();
                headerViewHolder.headerTextView = (TextView) convertView.findViewById(R.id.headerTextView);

                convertView.setTag(headerViewHolder);
            }
            else
            {
                headerViewHolder = (HeaderViewHolder) convertView.getTag();
            }

            headerViewHolder.headerTextView.setText(user.getNickname());
        }
        else
        {
            MemberViewHolder memberViewHolder;
            if(convertView == null)
            {
                convertView = layoutInflater.inflate(R.layout.list_member, parent, false);

                memberViewHolder = new MemberViewHolder();
                memberViewHolder.imageView = (CircleImageView) convertView.findViewById(R.id.avatarImageView);
                memberViewHolder.nicknameTextView = (TextView) convertView.findViewById(R.id.nicknameTextView);
                memberViewHolder.departmentTextView = (TextView) convertView.findViewById(R.id.departmentTextView);

                convertView.setTag(memberViewHolder);
            }
            else
            {
                memberViewHolder = (MemberViewHolder) convertView.getTag();
            }

            boolean isChosen = chosenList.contains(user);
            int color = isChosen ? R.color.list_item_pressed : R.color.list_item_unpressed;
            convertView.setBackgroundResource(color);

            ViewUtils.setImageViewBitmap(user, memberViewHolder.imageView);

            if (user.getNickname().isEmpty())
            {
                memberViewHolder.nicknameTextView.setText(R.string.not_available);
            }
            else
            {
                memberViewHolder.nicknameTextView.setText(user.getNickname());
            }

            int visibility = user.getDepartment().isEmpty() ? View.GONE : View.VISIBLE;
            memberViewHolder.departmentTextView.setVisibility(visibility);
            memberViewHolder.departmentTextView.setText(user.getDepartment());

            color = isChosen ? selectedColor : unselectedColor;
            memberViewHolder.nicknameTextView.setTextColor(color);

        }
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

    public int getViewTypeCount()
    {
        return 2;
    }

    public int getItemViewType(int position)
    {
        return indexList.contains(position) ? Constant.TYPE_HEADER : Constant.TYPE_CONTENT;
    }

    public boolean isItemViewTypePinned(int viewType)
    {
        return viewType == 1;
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
        indexList.clear();
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

    static class HeaderViewHolder
    {
        TextView headerTextView;
    }

    static class MemberViewHolder
    {
        CircleImageView imageView;
        TextView nicknameTextView;
        TextView departmentTextView;
    }
}