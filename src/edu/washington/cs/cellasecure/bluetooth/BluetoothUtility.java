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

package edu.washington.cs.cellasecure.bluetooth;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

/**
 * Bluetooth Utility library for connecting Android mobile applications with
 * Bluetooth devices.
 * 
 * requires BLUETOOTH and BLUETOOTH_ADMIN
 * 
 * @author CellaSecure
 */
public class BluetoothUtility {

    public static final int BLUETOOTH_REQUEST_ID = 1337;

    private static final UUID     mUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private BluetoothAdapter      mBluetoothAdapter;    // Connection point for Bluetooth devices
    private BroadcastReceiver     mBroadcastReceiver;   // Broadcast receiver to listen for various callbacks
    private Activity              mActivity;            // Parent activity of this instance
    private List<BluetoothDevice> mDiscoveredDevices;   // List of found devices that have not been paired

    private OnDiscoveryListener   mDiscoveryListener;   // Listener to handle device discovery
    private OnConnectListener     mConnectListener;     // Listener to handle connections
    
    /**
     * Constructs a new Bluetooth utility to manage devices
     * 
     * @param activity
     *            Context for mobile application
     * @param callbacks
     *            Client callbacks for receiving notifications
     */
    public BluetoothUtility(Activity activity) {
        mActivity = activity;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null)
            throw new IllegalStateException("Bluetooth not supported");
        mDiscoveredDevices = new ArrayList<BluetoothDevice>();

        // register receiver to hear when a device is found
        mBroadcastReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action.equals(BluetoothDevice.ACTION_FOUND)) {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    mDiscoveredDevices.add(device);
                    if (mDiscoveryListener != null)
                        mDiscoveryListener.onDiscovery(getDiscoveredDevices());
                }
            }
        };
        IntentFilter action_found_filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        mActivity.registerReceiver(mBroadcastReceiver, action_found_filter);
    }

    /**
     * Start a discovery for in-range Bluetooth devices, scanning
     * for at most 12 seconds.  When a device is found will call
     * onDiscovery with an updated list of found devices.
     * @see BluetoothListener
     */
    public void scanForDevices() {
        if (!mBluetoothAdapter.isEnabled())
            throw new IllegalStateException("Bluetooth must be enabled");
        if (mBluetoothAdapter.isDiscovering()) mBluetoothAdapter.cancelDiscovery();
        mBluetoothAdapter.startDiscovery();
    }

    /**
     * @return true if Bluetooth is enabled on the mobile device, else false
     */
    public boolean isEnabled() {
        return mBluetoothAdapter.isEnabled();
    }
    
    /**
     * Returns an intent to be used by the Activity using this library
     * Ex.
     *      BluetoothUtility util = new BluetoothUtility(this);
     *      if (!util.isEnabled()) 
     *          startActivityForResult(enableBluetooth(), REQUEST_ENABLE_BT);
     *
     * @return an Intent to be used in "startActivityForResult" to request
     * Bluetooth to be enabled
     */
    public void enableBluetooth() {
        Intent i = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        mActivity.startActivityForResult(i, BLUETOOTH_REQUEST_ID);
    }
    
    /**
     * @return true if Bluetooth adapter is discovering, else false
     */
    public boolean isScanning() {
        return mBluetoothAdapter.isDiscovering();
    }
    
    /**
     * Cancel device discovery
     */
    public void cancelDiscovery() {
        mBluetoothAdapter.cancelDiscovery();
    }

    /**
     * Accessor for both bonded and discovered devices
     * 
     * @return a list of all discovered and bonded devices
     * @throws IllegalStateException if Bluetooth is not enabled
     */
    public List<BluetoothDevice> getAllDevices() {
        if (!mBluetoothAdapter.isEnabled())
            throw new IllegalStateException("Bluetooth must be enabled");
        List<BluetoothDevice> allDevices = new ArrayList<BluetoothDevice>(mDiscoveredDevices);
        allDevices.addAll(mBluetoothAdapter.getBondedDevices());
        return allDevices;
    }

    /**
     * Accessor for bonded devices
     * 
     * @return a list of all bonded devices
     * @throws IllegalStateException if Bluetooth is not enabled
     */
    public List<BluetoothDevice> getBondedDevices() {
        if (!mBluetoothAdapter.isEnabled())
            throw new IllegalStateException("Bluetooth must be enabled");
        return new ArrayList<BluetoothDevice>(mBluetoothAdapter.getBondedDevices());
    }

    /**
     * Accessor for discovered devices
     * 
     * @return a list of all discovered devices
     * @throws IllegalStateException if Bluetooth is not enabled
     */
    public List<BluetoothDevice> getDiscoveredDevices() {
        return mDiscoveredDevices;
    }

    /**
     * Create a bond with the given device
     * 
     * @param device
     *            the Bluetooth device to bond with
     * @return true if already paired or on successful pairing, false otherwise
     */
    public boolean pairDevice(BluetoothDevice device) {
        switch (device.getBondState()) {
        case (BluetoothDevice.BOND_BONDED):
            return true;
        case (BluetoothDevice.BOND_NONE):
            try {
                return ( Boolean ) (device.getClass()).getMethod("createBond").invoke(device);
            } catch (Exception e) {
                return false;
            }
            // case (BluetoothDevice.BOND_BONDING): // taken care of by default
        default:
            return false;
        }
    }

    /**
     * Erase the bond with the given device
     * 
     * @param device
     *            the Bluetooth device to unpair from
     * @returns true if already unpaired or on successful unpairing, else false
     */
    public boolean unpairDevice(BluetoothDevice device) {
        switch (device.getBondState()) {
        case (BluetoothDevice.BOND_BONDED):
            try {
                return ( Boolean ) (device.getClass()).getMethod("removeBond").invoke(device);
            } catch (Exception e) {
                return false;
            }
        case (BluetoothDevice.BOND_NONE):
            return true;
            // case (BluetoothDevice.BOND_BONDING): // taken care of by default
        default:
            return false;
        }
    }

    /**
     * Attempt to establish a connection with the given device
     * 
     * @param device
     *            the Bluetooth device to connect to
     * @throws IllegalStateException if Bluetooth is not enabled
     */
    public void connect(BluetoothDevice device) {
        if (!mBluetoothAdapter.isEnabled()) 
            throw new IllegalStateException("Bluetooth must be enabled");
        new Thread(new ConnectionThread(
                device, mBluetoothAdapter, mUUID, mConnectListener)).start();
    }
    
    /* For Android */
    
    public void onPause() {
        mActivity.unregisterReceiver(mBroadcastReceiver);
    }
    
    public void onResume() {
        IntentFilter action_found_filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        mActivity.registerReceiver(mBroadcastReceiver, action_found_filter);
    }
    
    /* Callback setters */
    
    public void setOnDiscoveryListener (OnDiscoveryListener dl) {
        mDiscoveryListener = dl;
    }
    
    public void setOnConnectListener (OnConnectListener cl) {
        mConnectListener = cl;
    }

    /* Listener interfaces */

    public interface OnDiscoveryListener {
        /**
         * Callback to notify a client when a device is found
         * 
         * @param bluetoothDevices
         *            the list of discovered Bluetooth devices
         */
        public void onDiscovery(List<BluetoothDevice> bluetoothDevices);
    }
    
    public interface OnConnectListener {
        /**
         * Callback to return an established connection
         * 
         * @param connection
         *            the connection to the Bluetooth device
         */
        public void onConnected(Connection connection);
    }
}