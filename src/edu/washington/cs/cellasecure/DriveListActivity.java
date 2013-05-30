/*
 * Copyright 2013 CellaSecure
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package edu.washington.cs.cellasecure;

import android.app.ListActivity;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import edu.washington.cs.cellasecure.bluetooth.BluetoothUtility;
import edu.washington.cs.cellasecure.storage.DeviceUtils;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DriveListActivity extends ListActivity implements OnItemClickListener {

    private BluetoothUtility mBT;
    private MenuItem mMenuRefresh;
    private ProgressBar mDriveScanIndicator;
    private LinearLayout mDriveListContainer;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_drive_list);

        getListView().setOnItemClickListener(this);

        ListAdapter adapter = new DriveListAdapter();
        setListAdapter(adapter);

        mDriveScanIndicator = (ProgressBar) findViewById(android.R.id.progress);
        mDriveListContainer = (LinearLayout ) findViewById(R.id.list_drive_container);
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflator = getMenuInflater();
        inflator.inflate(R.menu.device_list, menu);

        mMenuRefresh = menu.findItem(R.id.menu_refresh_devices);
        assert mMenuRefresh != null;
        mMenuRefresh.setActionView(R.layout.actionbar_indeterminate_progress);
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_refresh_devices:
                setListAdapter(new DriveListAdapter());
                mMenuRefresh.setActionView(R.layout.actionbar_indeterminate_progress);
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case BluetoothUtility.BLUETOOTH_REQUEST_ID:
                if (mBT != null) mBT.scanForDevices();
                break;
            default:
                // no-op
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
        mBT.cancelDiscovery();
        Drive d = (Drive) getListAdapter().getItem(position);
        Intent i = new Intent(this, DriveManageActivity.class);
        i.putExtra(Drive.KEY_BUNDLE_DRIVE, d);
        startActivity(i);
    }

    private class DriveListAdapter extends BaseAdapter implements ListAdapter, BluetoothUtility.OnDiscoveryListener, BluetoothUtility.OnDiscoveryFinishedListener {
        private static final int VIEW_TYPE_PAIRED_INRANGE = 0;
        private static final int VIEW_TYPE_INRANGE = 1;
        private static final int VIEW_TYPE_PAIRED_OORANGE = 2;
        private static final int VIEW_TYPE_COUNT = 3;

        // load paired devices and add to list
        // then, scan over bluetooth and add pairable devices
        //
        // [In range + paired] > [Pairable] > [Out of range + paired]
        // In range + paired: text is black, in an "enabled" state
        // lock status is shown
        // Pairable: text is black, in an "enabled" state
        // "+" icon is shown instead of lock status
        // Out of range + paired: text is grayed out, in a "disabled" state

        private final DriveListActivity mActivity = DriveListActivity.this;
        private final Set<Drive> mPairedInRangeDrives = new HashSet<Drive>();
        private final Set<Drive> mInRangeDrives = new HashSet<Drive>();
        private final Set<Drive> mPairedOutOfRangeDrives = new HashSet<Drive>();

        public DriveListAdapter() {
            new PairedDrivesLoadTask().execute();
        }

        @Override
        public int getCount() {
            return mPairedInRangeDrives.size() + mInRangeDrives.size() + mPairedOutOfRangeDrives.size();
        }

        @Override
        public Object getItem(int position) {
            int threshold1 = mPairedInRangeDrives.size();
            int threshold2 = threshold1 + mInRangeDrives.size();
            if (position >= threshold2) return mPairedOutOfRangeDrives.toArray()[position - threshold2];
            else if (position >= threshold1) return mInRangeDrives.toArray()[position - threshold1];
            else return mPairedInRangeDrives.toArray()[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater li = LayoutInflater.from(mActivity);
            View v = (convertView != null) ? convertView : li.inflate(R.layout.list_drive, null);
            assert v != null;
            Drive d = (Drive) getItem(position);
            int viewType = getItemViewType(position);

            switch (viewType) {
                case VIEW_TYPE_PAIRED_INRANGE:
                    ((TextView) v.findViewById(R.id.list_drive_name)).setText(d.getName());
                    ((TextView) v.findViewById(R.id.list_drive_address)).setText(d.getAddress());
                    break;
                case VIEW_TYPE_INRANGE:
                    ((TextView) v.findViewById(R.id.list_drive_name)).setText(d.getName());
                    ((TextView) v.findViewById(R.id.list_drive_address)).setText(d.getAddress());
                    break;
                case VIEW_TYPE_PAIRED_OORANGE:
                    TextView tvDriveName = (TextView) v.findViewById(R.id.list_drive_name);
                    tvDriveName.setTextColor(Color.LTGRAY);
                    tvDriveName.setText(d.getName());
                    TextView tvDriveAddress = (TextView) v.findViewById(R.id.list_drive_address);
                    tvDriveAddress.setTextColor(Color.LTGRAY);
                    tvDriveAddress.setText(d.getAddress());
                    break;
            }
            return v;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public boolean isEmpty() {
            return mPairedInRangeDrives.isEmpty() && mInRangeDrives.isEmpty() && mPairedOutOfRangeDrives.isEmpty();
        }

        @Override
        public int getItemViewType(int position) {
            int threshold1 = mPairedInRangeDrives.size();
            int threshold2 = threshold1 + mInRangeDrives.size();
            if (position >= threshold2) return VIEW_TYPE_PAIRED_OORANGE;
            else if (position >= threshold1) return VIEW_TYPE_INRANGE;
            else return VIEW_TYPE_PAIRED_INRANGE;
        }

        @Override
        public int getViewTypeCount() {
            return VIEW_TYPE_COUNT;
        }

        @Override
        public void onDiscovery(BluetoothDevice device) {
            Drive drive = new Drive(device);
            if (mPairedOutOfRangeDrives.remove(drive)) mPairedInRangeDrives.add(drive);
            else mInRangeDrives.add(drive);

            mDriveScanIndicator.setVisibility(View.GONE);
            mDriveListContainer.setVisibility(View.VISIBLE);
            notifyDataSetChanged();
        }

        @Override
        public void onDiscoveryFinished() {
            mMenuRefresh.setActionView(null);
        }

        private class PairedDrivesLoadTask extends AsyncTask<Void, Void, Map<String, String>> {

            private final DriveListAdapter mAdapter = DriveListAdapter.this;

            @Override
            protected Map<String, String> doInBackground(Void... args) {
                return DeviceUtils.fileToMap(mActivity);
            }

            @Override
            protected void onPostExecute(Map<String, String> pairedDevices) {
                for (Map.Entry<String, String> e : pairedDevices.entrySet())
                    mPairedOutOfRangeDrives.add(new Drive(e.getValue(), e.getKey()));
                if (!pairedDevices.isEmpty()) {
                    mDriveScanIndicator.setVisibility(View.GONE);
                    mDriveListContainer.setVisibility(View.VISIBLE);
                    mAdapter.notifyDataSetChanged();
                }
                mBT = new BluetoothUtility(mActivity);
                mBT.setOnDiscoveryListener(mAdapter);
                mBT.setOnDiscoveryFinishedListener(mAdapter);
                if (!mBT.isEnabled()) mBT.enableBluetooth();
                else mBT.scanForDevices();
            }
        }
    }
}