package edu.washington.cs.cellasecure;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.SpinnerAdapter;

public class DriveDropDownAdapter extends BaseAdapter implements SpinnerAdapter {

    private final DriveCollection mDriveCollection = DriveCollection.getCollection();
    private static final Drive NEW_DRIVE = new Drive("NEW DRIVE"); // FIXME: use res string
    
    @Override
    public int getCount() {
        return mDriveCollection.getList().size()
                + 1; // Add new device
    }

    @Override
    public Object getItem(int position) {
        if (position == getCount()-1)
            return NEW_DRIVE;
        
        return mDriveCollection.getList().get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
//        if (convertView != null) {
//            TextView tv = (TextView) convertView;
//            tv.setText(getItem(position).toString());
//            return tv;
//        }
        return null;
    }

    /* (non-Javadoc)
     * @see android.widget.BaseAdapter#getDropDownView(int, android.view.View, android.view.ViewGroup)
     */
    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        return super.getDropDownView(position, convertView, parent);
    }

}
