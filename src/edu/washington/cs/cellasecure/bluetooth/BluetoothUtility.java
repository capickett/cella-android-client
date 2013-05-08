/*
 * Copyright 2013 CellaSecure
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.washington.cs.cellasecure.bluetooth;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

/**
 * TODO: Class comment
 * 
 * requires BLUETOOTH and BLUETOOTH_ADMIN
 * 
 * @author CellaSecure
 */
public class BluetoothUtility implements BluetoothUtilityInterface, ConnectionThread.ConnectionCallbacks {

	private static final UUID mUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	
	private BluetoothAdapter 		mBluetoothAdapter;		// Connection point for Bluetooth devices
	private BroadcastReceiver 		mBroadcastReceiver;		// Broadcast receiver to listen for various callbacks
	private List<BluetoothDevice> 	mBondedDevices; 		// List of bonded devices
	private List<BluetoothDevice>	mDiscoveredDevices;		// List of found devices that have not been paired
	private BluetoothCallbacks		mCallbacks;				//
	
	/*
	 * Constructs a new BluetoothUtility for managing 
	 * communication between Android and Bluetooth Devices
	 */
	public BluetoothUtility(Context context, BluetoothCallbacks callbacks) {
		mCallbacks = callbacks;
		mBluetoothAdapter  = BluetoothAdapter.getDefaultAdapter();
		mDiscoveredDevices = new ArrayList<BluetoothDevice>();
		mBondedDevices 	   = new ArrayList<BluetoothDevice>(mBluetoothAdapter.getBondedDevices());
		
		// register receiver to hear when a device is found
		mBroadcastReceiver = new BroadcastReceiver() {
			public void onReceive(Context context, Intent intent) {
				String action = intent.getAction();
				if (action.equals(BluetoothDevice.ACTION_FOUND)) {
					BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
					mDiscoveredDevices.add(device);
					mCallbacks.onDiscovery(getDiscoveredDevices());
				}
			}
		};
		IntentFilter action_found_filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		context.registerReceiver(mBroadcastReceiver, action_found_filter);
	}
	
	@Override
	public void scanForDevices() {
		if (mBluetoothAdapter.isDiscovering()) 
			mBluetoothAdapter.cancelDiscovery();
		mBluetoothAdapter.startDiscovery();
	}
	
	@Override
	public boolean isScanning() {
		return mBluetoothAdapter.isDiscovering();
	}
	
	@Override
	public List<BluetoothDevice> getAllDevices() {
		List<BluetoothDevice> allDevices = new ArrayList<BluetoothDevice>(mDiscoveredDevices);
		allDevices.addAll(mBondedDevices);
		return allDevices;
	}
	
	@Override
	public List<BluetoothDevice> getBondedDevices() {
		return mBondedDevices;
	}
	
	@Override
	public List<BluetoothDevice> getDiscoveredDevices() {
		return mDiscoveredDevices;
	}

	@Override
	public boolean pairDevice(BluetoothDevice device) {
		switch (device.getBondState()) {
		case (BluetoothDevice.BOND_BONDED):
			return true;
		case (BluetoothDevice.BOND_NONE):
			try {
				return (Boolean) (device.getClass()).getMethod("createBond").invoke(device);
			} catch (Exception e) {
				return false;
			}
		// case (BluetoothDevice.BOND_BONDING): // taken care of by default
		default:
			return false;
		}
	}

	@Override
	public boolean unpairDevice(BluetoothDevice device) {
		switch (device.getBondState()) {
		case (BluetoothDevice.BOND_BONDED):
			try {
				return (Boolean) (device.getClass()).getMethod("removeBond").invoke(device);
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

	@Override
	public void connect(BluetoothDevice device) {
		new Thread(new ConnectionThread(device, mBluetoothAdapter, mUUID, this)).start();
	}

	@Override
	public void onConnected(Connection connection) {
		mCallbacks.onConnected(connection);
	}
}