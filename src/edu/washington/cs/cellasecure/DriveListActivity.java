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

import android.app.Activity;
import android.app.ListActivity;
import android.bluetooth.BluetoothDevice;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import edu.washington.cs.cellasecure.bluetooth.BluetoothUtility;
import edu.washington.cs.cellasecure.storage.DeviceUtils;

import java.util.*;

public class DriveListActivity extends ListActivity {

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_drive_list);

        ListAdapter adapter = new DriveListAdapter();

        setListAdapter(adapter);
    }

    private class DriveListAdapter extends BaseAdapter implements ListAdapter, BluetoothUtility.OnDiscoveryListener {

        // load paired devices and add to list
        // then, scan over bluetooth and add pairable devices
        //
        // [In range + paired] > [Pairable] > [Out of range + paired]
        // In range + paired: text is black, in an "enabled" state
        //                    lock status is shown
        // Pairable:          text is black, in an "enabled" state
        //                    "+" icon is shown instead of lock status
        // Out of range + paired: text is grayed out, in a "disabled" state


        private Activity mActivity = DriveListActivity.this;
        private Set<Drive> mPairedInRangeDrives = new HashSet<Drive>();
        private Set<Drive> mInRangeDrives = new HashSet<Drive>();
        private Set<Drive> mPairedOutOfRangeDrives = new HashSet<Drive>();

        public DriveListAdapter() {
            new PairedDrivesLoadTask().execute();
        }

        private Set<Drive> chooseSet(int position) {

            if (position > threshold2)
                return mPairedOutOfRangeDrives;
            else if (position > threshold1)
                return mInRangeDrives;
            else
                return mPairedInRangeDrives;
        }

        @Override
        public int getCount() {
            return mPairedInRangeDrives.size() + mInRangeDrives.size() + mPairedOutOfRangeDrives.size();
        }

        @Override
        public Object getItem(int position) {
            int threshold1 = mPairedInRangeDrives.size();
            int threshold2 = threshold1 + mInRangeDrives.size();
            if (position >= threshold2)
                return mPairedOutOfRangeDrives.toArray()[position - threshold2];
            else if (position >= threshold1)
                return mInRangeDrives.toArray()[position - threshold1];
            else
                return mPairedInRangeDrives.toArray()[position];
        }

        @Override
        public long getItemId(int position) {
            return 0; // TODO: Implement me
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return null; // TODO: Implement me
        }

        @Override
        public void onDiscovery(List<BluetoothDevice> bluetoothDevices) {
            for (BluetoothDevice dev : bluetoothDevices) {
                boolean added = false;
                for (Drive drive : mPairedOutOfRangeDrives) {
                    if (drive.getDevice().equals(dev)) {
                        mPairedOutOfRangeDrives.remove(drive);
                        mPairedInRangeDrives.add(drive);
                        added = true;
                        break;
                    }
                }
                if (!added)
                    mInRangeDrives.add(new Drive(dev.getName(), dev));
            }

            notifyDataSetChanged();
        }

        private class PairedDrivesLoadTask extends AsyncTask<Void, Void, Map<String, String>> {

            private DriveListAdapter mAdapter = DriveListAdapter.this;

            @Override
            protected Map<String, String> doInBackground(Void... args) {
                return DeviceUtils.fileToMap(mActivity);
            }

            @Override
            protected void onPostExecute(Map<String, String> pairedDevices) {
                for (Map.Entry<String, String> e : pairedDevices.entrySet())
                    mPairedOutOfRangeDrives.add(new Drive(e.getValue(), e.getKey()));
                mAdapter.notifyDataSetChanged();
                BluetoothUtility bt = new BluetoothUtility(mActivity);
                bt.setOnDiscoveryListener(mAdapter);
                bt.scanForDevices();
            }
        }
    }

}