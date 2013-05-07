package edu.washington.cs.cellasecure.bluetooth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;


/**
 * 
 * requires BLUETOOTH and BLUETOOTH_ADMIN
 * 
 * @author palarson
 *
 */
public class BluetoothUtility implements BluetoothUtilityInterface {

	private static final UUID mUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	
	private BluetoothAdapter 		mBluetoothAdapter;		// Connection point for bluetooth devices
	private BroadcastReceiver 		mBroadcastReceiver;		// Broadcast receiver to listen for various callbacks
	private List<BluetoothDevice> 	mBondedDevices; 		// List of bonded devices
	private List<BluetoothDevice>	mDiscoveredDevices;		// List of found devices that have not been paired	
	private Map<BluetoothDevice, DeviceConfiguration> mConfigMap; // Mappings from each connected bluetooth 
													 			  // device to its configuration
	
	public BluetoothUtility(Context context) {
		mBluetoothAdapter  = BluetoothAdapter.getDefaultAdapter();
		mDiscoveredDevices = new ArrayList<BluetoothDevice>();
		mBondedDevices 	   = new ArrayList<BluetoothDevice>(mBluetoothAdapter.getBondedDevices());
		mConfigMap		   = new HashMap<BluetoothDevice, DeviceConfiguration>();
		
		// register receiver to hear when a device is found
		mBroadcastReceiver = new BroadcastReceiver() {
			public void onReceive(Context context, Intent intent) {
				String action = intent.getAction();
				if (action.equals(BluetoothDevice.ACTION_FOUND)) {
					BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
					mDiscoveredDevices.add(device);
				}
			}
		};
		IntentFilter action_found_filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		context.registerReceiver(mBroadcastReceiver, action_found_filter);
	}
	
	@Override
	public void scanForDevices() {
		mBluetoothAdapter.startDiscovery();
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
			// already bonded
			return true;
//		case (BluetoothDevice.BOND_BONDING):
//			// currently bonding
//		 	return false;
		case (BluetoothDevice.BOND_NONE):
			try {
				return (Boolean) (device.getClass()).getMethod("createBond").invoke(device);
			} catch (Exception e) {
				return false;
			}
		default:
			// Unrecognized state
			return false;
		}
	}

	@Override
	public boolean unpairDevice(BluetoothDevice device) {
		switch (device.getBondState()) {
		case (BluetoothDevice.BOND_BONDED):
			// already bonded
			try {
				return (Boolean) (device.getClass()).getMethod("removeBond").invoke(device);
			} catch (Exception e) {
				return false;
			}
//		case (BluetoothDevice.BOND_BONDING):
//			// currently bonding
//		 	return false;
		case (BluetoothDevice.BOND_NONE):
			return true;
		default:
			// Unrecognized state
			return false;
		}		
	}

	@Override
	public void connect(BluetoothDevice device) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void disconnect() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public DeviceConfiguration getConfiguration(BluetoothDevice device) {
		return mConfigMap.get(device); 
	}

	@Override
	public void setConfiguration(BluetoothDevice device, String fieldName,
			String value) {
		DeviceConfiguration config = mConfigMap.get(device);
		if (config == null) {
			throw new IllegalArgumentException("Device not found");
		}
		config.setOption(fieldName, value);
		setConfiguration(device, config);
	}
	
	@Override
	public void setConfiguration(BluetoothDevice device, DeviceConfiguration config) {
		if (config == null) config = new DeviceConfiguration();
		mConfigMap.put(device, config);
	}
}
