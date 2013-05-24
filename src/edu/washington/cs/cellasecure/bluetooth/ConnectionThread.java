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

import java.io.IOException;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

/**
 * Connection thread for asynchronously establishing a socket with a Bluetooth
 * device
 * 
 * @author CellaSecure
 */
public class ConnectionThread implements Runnable {
    private BluetoothAdapter    mAdapter;      // Bluetooth adapter for managing all communication
    private BluetoothDevice     mDevice;       // Bluetooth device to connect to
    private UUID                mUUID;         // UUID for connecting to Bluetooth device
    private OnConnectListener   mListener;     // listener to return conneciton status to client
    /*
     * Initialize necessary information for a new connection thread
     */
    public ConnectionThread(BluetoothDevice device,
                            BluetoothAdapter adapter,
                            UUID uuid){
        mDevice   = device;
        mAdapter  = adapter;
        mUUID     = uuid;
        mListener = null;
    }
    
    public void setOnConnectListener(OnConnectListener listener) {
        mListener = listener;
    }

    @Override
    public void run() {
        mAdapter.cancelDiscovery();
        try {
            BluetoothSocket socket = mDevice.createRfcommSocketToServiceRecord(mUUID);
            socket.connect();
            if (mListener != null) mListener.onConnected(mDevice, new Connection(socket));
        } catch (IOException e) {
            // e.printStackTrace();
            return;
        }
    }
    
    public interface OnConnectListener {
        /**
         * Callback to return an established connection
         * 
         * @param connection
         *            the connection to the Bluetooth device
         */
        public void onConnected(BluetoothDevice device, Connection connection);
    }
}
