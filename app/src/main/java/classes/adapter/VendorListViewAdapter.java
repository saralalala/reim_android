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
import java.util.List;

import classes.model.Vendor;

public class VendorListViewAdapter extends BaseAdapter
{
    private LayoutInflater layoutInflater;
    private List<Vendor> defaultList;
    private List<Vendor> vendorList;
    private boolean showDistance;

    public VendorListViewAdapter(Context context)
    {
        this.layoutInflater = LayoutInflater.from(context);
        this.defaultList = new ArrayList<>();
        this.vendorList = new ArrayList<>();

        Vendor vendor = new Vendor(context.getString(R.string.vendor_taxi));
        vendor.setPhotoResID(R.drawable.icon_transport);
        defaultList.add(vendor);

        vendor = new Vendor(context.getString(R.string.vendor_flight));
        vendor.setPhotoResID(R.drawable.icon_flight);
        defaultList.add(vendor);

        vendor = new Vendor(context.getString(R.string.vendor_train));
        vendor.setPhotoResID(R.drawable.icon_train);
        defaultList.add(vendor);

        vendor = new Vendor(context.getString(R.string.vendor_phone));
        vendor.setPhotoResID(R.drawable.icon_phone);
        defaultList.add(vendor);

        vendorList.addAll(defaultList);
    }

    public View getView(int position, View convertView, ViewGroup parent)
    {
        Vendor vendor = vendorList.get(position);

        if (position < 4)
        {
            View view = layoutInflater.inflate(R.layout.list_vendor_default, parent, false);

            ImageView imageView = (ImageView) view.findViewById(R.id.avatarImageView);
            imageView.setImageResource(vendor.getPhotoResID());

            TextView nameTextView = (TextView) view.findViewById(R.id.nameTextView);
            nameTextView.setText(vendor.getName());

            return view;
        }
        else
        {
            ViewHolder viewHolder;
            if(convertView == null || convertView.getTag() == null)
            {
                convertView = layoutInflater.inflate(R.layout.list_vendor, parent, false);

                viewHolder = new ViewHolder();
                viewHolder.imageView = (ImageView) convertView.findViewById(R.id.avatarImageView);
                viewHolder.nameTextView = (TextView) convertView.findViewById(R.id.nameTextView);
                viewHolder.addressTextView = (TextView) convertView.findViewById(R.id.locationTextView);
                viewHolder.distanceTextView = (TextView) convertView.findViewById(R.id.distanceTextView);

                convertView.setTag(viewHolder);
            }
            else
            {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            if (vendor.getPhoto() != null)
            {
                viewHolder.imageView.setImageBitmap(vendor.getPhoto());
            }

            viewHolder.nameTextView.setText(vendor.getName());
            viewHolder.addressTextView.setText(vendor.getAddress());
            viewHolder.distanceTextView.setText(Integer.toString(vendor.getDistance()) + "米");
            int visibility = showDistance ? View.VISIBLE : View.GONE;
            viewHolder.distanceTextView.setVisibility(visibility);

            return convertView;
        }
    }

    public int getCount()
    {
        return vendorList.size();
    }

    public Vendor getItem(int position)
    {
        return vendorList.get(position);
    }

    public long getItemId(int position)
    {
        return position;
    }

    public void setVendorList(List<Vendor> vendors)
    {
        vendorList.clear();
        vendorList.addAll(defaultList);
        vendorList.addAll(vendors);
    }

    public void setShowDistance(boolean showDistance)
    {
        this.showDistance = showDistance;
    }

    static class ViewHolder
    {
        ImageView imageView;
        TextView nameTextView;
        TextView addressTextView;
        TextView distanceTextView;
    }
}