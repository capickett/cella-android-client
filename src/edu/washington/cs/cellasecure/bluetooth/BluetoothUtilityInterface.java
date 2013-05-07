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

import java.util.List;

import android.bluetooth.BluetoothDevice;

/**
 * 
 * @author CellaSecure
 */
public interface BluetoothUtilityInterface {
	
	/**
	 * Start an asynchronous discovery for in-range Bluetooth devices, 
	 * scanning for at most 12 seconds.  
	 */
	public void scanForDevices();
	
	/**
	 * Accessor for both bonded and discovered devices
	 * @return a list of all discovered and bonded devices
	 */
	public List<BluetoothDevice> getAllDevices();
	
	/**
	 * Accessor for bonded devices
	 * @return a list of all bonded devices
	 */
	public List<BluetoothDevice> getBondedDevices();
	
	/**
	 * Accessor for discovered devices
	 * @return a list of all discovered devices
	 */
	public List<BluetoothDevice> getDiscoveredDevices();
	
	/**
	 * Create a bond with the given device 
	 * 
	 * @param device 	the Bluetooth device to bond with
	 * @return 			true if already paired or on successful pairing, false otherwise
	 */
	public boolean pairDevice(BluetoothDevice device);
	
	/**
	 * Erase the bond with the given device
	 * 
	 * @param device	the Bluetooth device to unpair from
	 */
	public boolean unpairDevice(BluetoothDevice device);
	
	/**
	 * Attempt to establish a connection with the given device
	 * 
	 * @param device	the Bluetooth device to connect to
	 */
	public void connect(BluetoothDevice device);
	
	/**
	 * Gracefully end connection with a Bluetooth device
	 * 
	 * @param socket	the socket to terminate
	 */
	public void disconnect();
	
	/**
	 * Return the configuration of the given Bluetooth device
	 * 
	 * @param device 	the device whose configuration to return
	 * @return 			the configuration for the device if found, else null
	 */
	public DeviceConfiguration getConfiguration(BluetoothDevice device);
	
	/**
	 * Sets the configuration to the given device from a preexisting configuration;
	 * 
	 * @param device    the device whose configuration will be updated
	 * @param config	the new configuration object for the given device
	 * 
	 * @throws IllegalArgumentException		if the device is not found
	 */
	public void setConfiguration(BluetoothDevice device, DeviceConfiguration config);
	
	/**
	 * Sets the given fieldName to the given value for the given device
	 * 
	 * @param device	the device whose configuration is to be set
	 * @param fieldName	the name of the configuration field to be modified
	 * @param value		the new value for fieldName in the configuration
	 */
	public void setConfiguration(BluetoothDevice device, String fieldName, String value);
}