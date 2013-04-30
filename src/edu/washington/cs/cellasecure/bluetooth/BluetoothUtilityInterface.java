package edu.washington.cs.cellasecure.bluetooth;

import java.util.List;

import android.bluetooth.BluetoothDevice;

public interface BluetoothUtilityInterface {
	
	/**
	 * Discover in-range Bluetooth devices, scanning for at most
	 * 12 seconds.
	 *  
	 * @return a list of discovered Bluetooth devices
	 */
	public List<BluetoothDevice> scanForDevices();
	
	/**
	 * Create a bond with the given device 
	 * 
	 * @param device 	the Bluetooth device to bond with
	 */
	public void pairDevice(BluetoothDevice device);
	
	/**
	 * Erase the bond with the given device
	 * 
	 * @param device	the Bluetooth device to unpair from
	 */
	public void unpairDevice(BluetoothDevice device);
	
	/**
	 * Attempt to establish a connection with the given device
	 * 
	 * @param device	the Bluetooth device to connect to
	 */
	public void connect(BluetoothDevice device);
	
	/**
	 * Gracefully end connection with a Bluetooth device
	 */
	public void disconnect();
	
	/**
	 * Return the configuration of the given Bluetooth device
	 * 
	 * @param device 	the device whose configuration to return
	 * @return 			the configuration for the device
	 */
	public DeviceConfiguration getConfiguration(BluetoothDevice device);
	
	/**
	 * 
	 * @param device	the device whose configuration is to be set
	 * @param fieldName	the name of the configuration field to be modified
	 * @param value		the new value for fieldName in the configuration
	 */
	public void setConfiguration(BluetoothDevice device, String fieldName, String value);
}