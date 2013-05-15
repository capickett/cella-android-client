package edu.washington.cs.cellasecure.fragments;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.app.Activity;
import android.app.Fragment;
import android.bluetooth.BluetoothDevice;
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
import edu.washington.cs.cellasecure.bluetooth.BluetoothUtilityInterface.BluetoothListener;
import edu.washington.cs.cellasecure.bluetooth.Connection;

public class DriveConnectFragment extends Fragment implements
        OnItemClickListener, BluetoothListener {

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

    private void startBluetoothScan() {
        mBT = new BluetoothUtility(getActivity(), this);
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
    public void onConnected(Connection connection) {
        /* We will not connect in this fragment, only pair */
    }

    @Override
    public void onDiscovery(List<BluetoothDevice> bluetoothDevices) {
        for (BluetoothDevice dev : bluetoothDevices)
            if (!mBT.getBondedDevices().contains(dev))
                mDeviceListItems.add(new Drive(dev.getName(), dev));
        mDeviceListAdapter.clear();
        mDeviceListAdapter.addAll(mDeviceListItems);
        mDeviceListAdapter.notifyDataSetChanged();
    }
    
    // FIXME: Needs to unregisterReceiver onPause

}
