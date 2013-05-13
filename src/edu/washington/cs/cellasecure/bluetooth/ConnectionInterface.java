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

/**
 * Interface for connection utility that manages and executes operations over a
 * connection to a Bluetooth device
 * 
 * @author CellaSecure
 */
public interface ConnectionInterface {
    /**
     * Gracefully end connection with a Bluetooth device
     * 
     * @param thread
     *            the thread to terminate
     */
    public void disconnect();

    /**
     * Read information from the socket
     * 
     * @param buf
     *            the buffer to read into
     */
    public void read(byte[] buf);

    /**
     * Write information to the socket
     * 
     * @param msg
     *            the message to write over the socket
     */
    public void write(byte[] msg);

    /**
     * Return the configuration of the given Bluetooth device
     * 
     * @param device
     *            the device whose configuration to return
     * @return the configuration for the device if found, else null
     */
    public DeviceConfiguration getConfiguration();

    /**
     * Sets the configuration to the given device from a preexisting
     * configuration;
     * 
     * @param device
     *            the device whose configuration will be updated
     * @param config
     *            the new configuration object for the given device
     * 
     * @throws IllegalArgumentException
     *             if the device is not found
     */
    public void setConfiguration(DeviceConfiguration config);

    /**
     * Sets the given fieldName to the given value for the given device
     * 
     * @param device
     *            the device whose configuration is to be set
     * @param fieldName
     *            the name of the configuration field to be modified
     * @param value
     *            the new value for fieldName in the configuration
     */
    public void setConfiguration(String fieldName, String value);
}
