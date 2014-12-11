package classes.Adapter;

import java.util.ArrayList;
import java.util.List;

import classes.Comment;
import classes.User;
import classes.Utils;

import com.rushucloud.reim.R;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class CommentListViewAdapter extends BaseAdapter
{
	private LayoutInflater layoutInflater;
	private List<Comment> commentList;
	
	public CommentListViewAdapter(Context context, List<Comment> comments)
	{
		this.layoutInflater = LayoutInflater.from(context);
		this.commentList = new ArrayList<Comment>(comments);
	}
	
	public View getView(int position, View convertView, ViewGroup parent)
	{
		if (convertView == null)
		{
			convertView = layoutInflater.inflate(R.layout.list_comment, parent, false);
		}

		ImageView avatarImageView = (ImageView)convertView.findViewById(R.id.avatarImageView);
		TextView reviewerTextView = (TextView)convertView.findViewById(R.id.reviewerTextView);
		TextView commentTextView = (TextView)convertView.findViewById(R.id.commentTextView);
		TextView dateTextView = (TextView)convertView.findViewById(R.id.dateTextView);
		
		Comment comment = commentList.get(position);
		User user = comment.getReviewer();
		if (!user.getAvatarPath().equals(""))
		{
			Bitmap bitmap = BitmapFactory.decodeFile(user.getAvatarPath());
			if (bitmap != null)
			{
				avatarImageView.setImageBitmap(bitmap);				
			}
		}
		
		if (user.getNickname().equals(""))
		{
			reviewerTextView.setText(R.string.not_available);
		}
		else
		{
			reviewerTextView.setText(user.getNickname());			
		}

		commentTextView.setText(comment.getContent());
		dateTextView.setText(Utils.secondToStringUpToMinute(comment.getCreatedDate()));
		
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
}
