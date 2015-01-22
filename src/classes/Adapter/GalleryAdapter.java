package classes.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Toast;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.rushucloud.reim.ImageActivity;
import com.rushucloud.reim.R;

public class GalleryAdapter extends BaseAdapter
{
	private Context context;
	private LayoutInflater layoutInflater;
	private ImageLoader imageLoader;
	private ArrayList<String> pathList = new ArrayList<String>();
	private boolean[] checkList;
	private int maxChosenCount;

	public GalleryAdapter(Context context, ImageLoader imageLoader, int maxCount)
	{
		this.context = context;
		this.imageLoader = imageLoader;
		this.layoutInflater = LayoutInflater.from(context);
		this.maxChosenCount = maxCount;
	}

	public View getView(final int position, View convertView, ViewGroup parent)
	{
		final ViewHolder holder;
		if (convertView == null)
		{
			convertView = layoutInflater.inflate(R.layout.gallery_item, parent, false);
			holder = new ViewHolder();
			holder.imageView = (ImageView) convertView.findViewById(R.id.imageView);			
			holder.checkImageView = (ImageView) convertView.findViewById(R.id.checkImageView);
			convertView.setTag(holder);
		}
		else
		{
			holder = (ViewHolder) convertView.getTag();
		}
		
		holder.imageView.setTag(position);
		holder.imageView.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				Intent intent = new Intent(context, ImageActivity.class);
				intent.putExtra("imagePath", pathList.get(position));
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(intent);
			}
		});
		holder.checkImageView.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				if (maxChosenCount != -1 && getSelectedCount() == maxChosenCount && !checkList[position])
				{
					Toast.makeText(context, "最多只能选择" + maxChosenCount + "张照片", Toast.LENGTH_SHORT).show();
					holder.checkImageView.setSelected(false);
				}
				else
				{
					checkList[position] = !checkList[position];
					holder.checkImageView.setSelected(checkList[position]);			
				}
			}
		});

		try
		{
			imageLoader.displayImage("file://" + pathList.get(position), holder.imageView, new SimpleImageLoadingListener()
			{
				public void onLoadingStarted(String imageUri, View view)
				{
					holder.imageView.setImageResource(R.drawable.no_media);
					super.onLoadingStarted(imageUri, view);
				}
			});

			holder.checkImageView.setSelected(checkList[position]);

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return convertView;
	}
	
	public int getCount()
	{
		return pathList.size();
	}

	public String getItem(int position)
	{
		return pathList.get(position);
	}

	public long getItemId(int position)
	{
		return position;
	}

	public void selectAll(boolean selection)
	{
		for (int i = 0; i < pathList.size(); i++)
		{
			checkList[i] = selection;

		}
		notifyDataSetChanged();
	}

	public ArrayList<String> getSelectedList()
	{
		ArrayList<String> selectedList = new ArrayList<String>();

		for (int i = 0; i < pathList.size(); i++)
		{
			if (checkList[i])
			{
				selectedList.add(pathList.get(i));
			}
		}

		return selectedList;
	}

	public int getSelectedCount()
	{
		int count = 0;
		if (checkList != null)
		{
			for (boolean b : checkList)
			{
				if (b)
				{
					count++;
				}
			}
		}

		return count;
	}
	
	public void setImageList(ArrayList<String> paths)
	{
		pathList.clear();
		pathList.addAll(paths);
		
		checkList = new boolean[paths.size()];
		for (int i = 0; i < checkList.length; i++)
		{
			checkList[i] = false;
		}
	}

	public void clearCache()
	{
		imageLoader.clearDiscCache();
		imageLoader.clearMemoryCache();
	}

	public void clear()
	{
		pathList.clear();
	}

	public class ViewHolder
	{
		public ImageView imageView;
		public ImageView checkImageView;
	}
}