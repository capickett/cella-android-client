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

package edu.washington.cs.cellasecure.fragments;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.app.Activity;
import android.app.Fragment;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import edu.washington.cs.cellasecure.Drive;
import edu.washington.cs.cellasecure.R;
import edu.washington.cs.cellasecure.bluetooth.BluetoothUtility;
import edu.washington.cs.cellasecure.bluetooth.BluetoothUtility.OnDiscoveryListener;

public class DriveConnectFragment extends Fragment implements
        OnItemClickListener {

    private ListView mDeviceList;
    private ArrayAdapter<Drive> mDeviceListAdapter;
    private Set<Drive> mDeviceListItems = new HashSet<Drive>();
    private BluetoothUtility mBT;

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Fragment#onCreate(android.os.Bundle)
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Fragment#onCreateView(android.view.LayoutInflater,
     * android.view.ViewGroup, android.os.Bundle)
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_drive_connect_list,
                container, false);
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Fragment#onCreateOptionsMenu(android.view.Menu,
     * android.view.MenuInflater)
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.device_list, menu);
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Fragment#onActivityCreated(android.os.Bundle)
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Activity activity = getActivity();
        mDeviceListAdapter = new ArrayAdapter<Drive>(activity,
                android.R.layout.simple_list_item_1);
        startBluetoothScan();

        mDeviceList = (ListView) activity.findViewById(R.id.drive_connect_list);
        mDeviceList.setAdapter(mDeviceListAdapter);
        mDeviceList.setOnItemClickListener(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Fragment#onOptionsItemSelected(android.view.MenuItem)
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.menu_refresh_devices:
            startBluetoothScan();
            return true;
        default:
            return false;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case BluetoothUtility.BLUETOOTH_REQUEST_ID:
                if (resultCode == Activity.RESULT_OK)
                    startBluetoothScan();
        }
    }

    private void startBluetoothScan() {
        mBT = new BluetoothUtility(getActivity());
        
        mBT.setOnDiscoveryListener(new OnDiscoveryListener() {
            
            @Override
            public void onDiscovery(List<BluetoothDevice> bluetoothDevices) {
                for (BluetoothDevice dev : bluetoothDevices)
                    if (!mBT.getBondedDevices().contains(dev))
                        mDeviceListItems.add(new Drive(dev.getName(), dev));
                mDeviceListAdapter.clear();
                mDeviceListAdapter.addAll(mDeviceListItems);
                mDeviceListAdapter.notifyDataSetChanged();
            }
        });

        if (!mBT.isEnabled())
            mBT.enableBluetooth();
        else
            mBT.scanForDevices();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * android.widget.AdapterView.OnItemClickListener#onItemClick(android.widget
     * .AdapterView, android.view.View, int, long)
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
            long id) {
        Drive selectedDrive = (Drive) parent.getItemAtPosition(position);
        
        // TODO: Add device to stored list
        getActivity().finish();
    }

    @Override
    public void onPause() {
        super.onPause();
        mBT.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        mBT.onResume();
    }
}
