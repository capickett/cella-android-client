package edu.washington.cs.cellasecure.bluetooth;

import java.io.IOException;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

/**
 * Connection thread for asynchronously establishing
 * a socket with a Bluetooth device
 * 
 * @author CellaSecure
 */
public class ConnectionThread implements Runnable {
	
	interface Callbacks {
		public void onConnected(BluetoothSocket socket);
	}	
	
	private BluetoothAdapter	mAdapter;	// Bluetooth adapter for managing all communication
	private BluetoothDevice 	mDevice;	// Bluetooth device to connect to
	private Callbacks			mCallback;	// Callback to return connected socket
	private UUID				mUUID;		// UUID for connecting to Bluetooth device

	public ConnectionThread(BluetoothDevice device, 
							BluetoothAdapter adapter, 
							UUID uuid,
							Callbacks callback) {
		mDevice   = device;
		mAdapter  = adapter;
		mCallback = callback;
		mUUID     = uuid;
	}
	
	@Override
	public void run() {
		mAdapter.cancelDiscovery();
		try {
			BluetoothSocket socket = mDevice.createRfcommSocketToServiceRecord(mUUID);
			socket.connect();
			mCallback.onConnected(socket);
		} catch (IOException e) {
			return;
		}
	}
}

