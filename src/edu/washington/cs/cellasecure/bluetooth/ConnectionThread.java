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

    /**
     * Interface for callbacks to be implemented by the client
     * 
     * @author CellaSecure
     */
    interface ConnectionListener {
        /**
         * Callback to return an established connection to a Bluetooth device
         * 
         * @param connection
         *            the established connection
         */
        public void onConnected(Connection connection);
    }

    private BluetoothAdapter    mAdapter;      // Bluetooth adapter for managing all communication
    private BluetoothDevice     mDevice;       // Bluetooth device to connect to
    private ConnectionListener  mLocalCallback; // Callback to return connected socket to Bluetooth utility
    private UUID                mUUID;         // UUID for connecting to Bluetooth device

    /*
     * Initialize necessary information for a new connection thread
     */
    public ConnectionThread(BluetoothDevice device,
                            BluetoothAdapter adapter,
                            UUID uuid, 
                            ConnectionListener localCallback){
        mDevice = device;
        mAdapter = adapter;
        mLocalCallback = localCallback;
        mUUID = uuid;
    }

    @Override
    public void run() {
        mAdapter.cancelDiscovery();
        try {
            BluetoothSocket socket = mDevice.createRfcommSocketToServiceRecord(mUUID);
            socket.connect();
            mLocalCallback.onConnected(new Connection(socket));
        } catch (IOException e) {
            // e.printStackTrace();
            return;
        }
    }
}
