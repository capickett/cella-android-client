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

import java.util.List;

import android.bluetooth.BluetoothDevice;

/**
 * Interface for Bluetooth Utility used to establish and manage interactions
 * between Android mobile devices and Bluetooth devices
 * 
 * @author CellaSecure
 */
public interface BluetoothUtilityInterface {

    /**
     * Callback interface to be implemented by the client. Used to deliver
     * results of asynchronous methods
     * 
     * @author CellaSecure
     */
    public interface BluetoothListener {
        /**
         * Callback to return an established connection
         * 
         * @param connection
         *            the connection to the Bluetooth device
         */
        public void onConnected(Connection connection);

        /**
         * Callback to notify a client when a device is found
         * 
         * @param bluetoothDevices
         *            the list of discovered Bluetooth devices
         */
        public void onDiscovery(List<BluetoothDevice> bluetoothDevices);
    }

    /**
     * Start an asynchronous discovery for in-range Bluetooth devices, scanning
     * for at most 12 seconds.
     */
    public void scanForDevices();

    /**
     * @return true if bluetooth adapter is discovering, else false
     */
    public boolean isScanning();

    /**
     * Accessor for both bonded and discovered devices
     * 
     * @return a list of all discovered and bonded devices
     */
    public List<BluetoothDevice> getAllDevices();

    /**
     * Accessor for bonded devices
     * 
     * @return a list of all bonded devices
     */
    public List<BluetoothDevice> getBondedDevices();

    /**
     * Accessor for discovered devices
     * 
     * @return a list of all discovered devices
     */
    public List<BluetoothDevice> getDiscoveredDevices();

    /**
     * Create a bond with the given device
     * 
     * @param device
     *            the Bluetooth device to bond with
     * @return true if already paired or on successful pairing, false otherwise
     */
    public boolean pairDevice(BluetoothDevice device);

    /**
     * Erase the bond with the given device
     * 
     * @param device
     *            the Bluetooth device to unpair from
     */
    public boolean unpairDevice(BluetoothDevice device);

    /**
     * Attempt to establish a connection with the given device
     * 
     * @param device
     *            the Bluetooth device to connect to
     */
    public void connect(BluetoothDevice device);
}