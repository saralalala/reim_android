package classes.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.rushucloud.reim.R;

import java.util.ArrayList;
import java.util.List;

import classes.model.Comment;
import classes.model.User;
import classes.utils.Utils;
import classes.utils.ViewUtils;
import classes.widget.CircleImageView;

public class CommentListViewAdapter extends BaseAdapter
{
    private LayoutInflater layoutInflater;
    private List<Comment> commentList;

    public CommentListViewAdapter(Context context, List<Comment> comments)
    {
        this.layoutInflater = LayoutInflater.from(context);
        this.commentList = new ArrayList<>(comments);
    }

    public View getView(int position, View convertView, ViewGroup parent)
    {
        ViewHolder viewHolder;
        if (convertView == null)
        {
            convertView = layoutInflater.inflate(R.layout.list_comment, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.avatarImageView = (CircleImageView) convertView.findViewById(R.id.avatarImageView);
            viewHolder.reviewerTextView = (TextView) convertView.findViewById(R.id.reviewerTextView);
            viewHolder.commentTextView = (TextView) convertView.findViewById(R.id.commentTextView);
            viewHolder.dateTextView = (TextView) convertView.findViewById(R.id.dateTextView);

            convertView.setTag(viewHolder);
        }
        else
        {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Comment comment = commentList.get(position);

        User user = comment.getReviewer();
        if (user != null)
        {
            ViewUtils.setImageViewBitmap(user, viewHolder.avatarImageView);

            if (user.getNickname().isEmpty())
            {
                viewHolder.reviewerTextView.setText(R.string.not_available);
            }
            else
            {
                viewHolder.reviewerTextView.setText(user.getNickname());
            }
        }

        viewHolder.commentTextView.setText(comment.getContent());
        viewHolder.dateTextView.setText(Utils.secondToStringUpToMinute(comment.getCreatedDate()));

        return convertView;
    }

    public int getCount()
    {
        return commentList.size();
    }

    public Comment getItem(int position)
    {
        return commentList.get(position);
    }

    public long getItemId(int position)
    {
        return position;
    }

    public void setComments(List<Comment> comments)
    {
        commentList.clear();
        commentList.addAll(comments);
    }

    private static class ViewHolder
    {
        CircleImageView avatarImageView;
        TextView reviewerTextView;
        TextView commentTextView;
        TextView dateTextView;
    }
}