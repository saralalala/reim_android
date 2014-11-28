package classes.Adapter;

import com.rushucloud.reim.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class OperationListViewAdapter extends BaseAdapter
{
	private LayoutInflater layoutInflater;
	private int[] operationList;
	private boolean[] checkList;
	
	public OperationListViewAdapter(Context context, int[] operations, boolean[] checks)
	{
		layoutInflater = LayoutInflater.from(context);
		operationList = operations;
		checkList = checks;
	}
	
	public View getView(int position, View convertView, ViewGroup parent)
	{
		if (convertView == null)
		{
			convertView = layoutInflater.inflate(R.layout.list_text, parent, false);
		}

		TextView textView = (TextView)convertView.findViewById(R.id.textView);
		textView.setText(operationList[position]);
		
		ImageView nextImageView = (ImageView)convertView.findViewById(R.id.nextImageView);
		nextImageView.setVisibility(checkList[position] ? View.VISIBLE : View.GONE);
		
		return convertView;
	}
	
	public int getCount()
	{
		return operationList.length;
	}

	public String getItem(int position)
	{
		return null;
	}

	public long getItemId(int position)
	{
		return position;
	}
}
