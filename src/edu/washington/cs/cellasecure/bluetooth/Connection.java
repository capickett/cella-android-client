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
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.bluetooth.BluetoothSocket;
import edu.washington.cs.cellasecure.bluetooth.BluetoothUtility.BluetoothListener;

/**
 * Utility for managing and executing operations over a connection to a
 * Bluetooth device
 * 
 * @author CellaSecure
 */
public class Connection {
    private static final String  WRITE_REQUEST_STRING = "W";
    private static final byte[]  WRITE_REQUEST_BYTES  = WRITE_REQUEST_STRING.getBytes();
    private static final String PASSWD_REQUEST_STRING = "P";
    private static final byte[] PASSWD_REQUEST_BYTES  = PASSWD_REQUEST_STRING.getBytes();
    
    private static ExecutorService     sPool = Executors.newSingleThreadExecutor();

    private DeviceConfiguration mConfig;
    private BluetoothSocket     mBluetoothSocket;
    private InputStream         mInputStream;
    private OutputStream        mOutputStream;
    private BluetoothListener   mListener;

    /**
     * Construct a new connection over the given socket The socket must be
     * established
     * 
     * @param socket
     *            the BluetoothSocket which the device is connected to
     * @throws IllegalArgumentException
     *             if socket is null
     *         IOException
     *             if device configuration failed to initialize
     */
    public Connection(BluetoothSocket socket, BluetoothListener listener) {
        if (socket == null) {
            throw new IllegalArgumentException("Socket must be established");
        }
        mBluetoothSocket = socket;
        mListener        = listener;
        mConfig          = null;
        
        InputStream is = null;
        OutputStream os = null;
        if (mBluetoothSocket != null) {
            try {
                is = mBluetoothSocket.getInputStream();
                os = mBluetoothSocket.getOutputStream();
            } catch (Exception e) { /* streams remain null */ }
        }
        mInputStream = is;
        mOutputStream = os;

        getConfiguration();
    }

    /**
     * Gracefully end connection with a Bluetooth device
     */
    public void disconnect() {
        sPool.shutdown();
        try {
            mBluetoothSocket.close();
            mBluetoothSocket = null;
        } catch (Exception e) { /* continue */ }
    }

    /**
     * Gets configuration for Bluetooth device, returning it through
     * the onConfigurationRead callback.
     * @see BluetoothListener
     */
    public void getConfiguration() {
        if (mConfig != null)
            mListener.onConfigurationRead(mConfig);
        else
            sPool.execute(new InitThread(mListener));
    }
    
    /**
     * Sends the current configuration to the device, updating the device if necessary
     */
    public void sendConfiguration() {
        sPool.execute(new WriteThread(mConfig.configBytes(), WRITE_REQUEST_BYTES, mListener));
    }
    
    /**
     * Sends a password to unlock the drive over Bluetooth.  If this fails, will
     * call onWriteError with an appropriate message.
     * @see BluetoothListener.onWriteError(String message)
     * 
     * @param passwd  The user's password which unlocks the drive
     */
    public void sendPassword(String passwd) {
        sPool.execute(new WriteThread(passwd.getBytes(), PASSWD_REQUEST_BYTES, mListener));
    }

    /**
     * Sets the given fieldName to the given value for this device
     * 
     * @param fieldName
     *            the name of the configuration field to be modified
     * @param value
     *            the new value for fieldName in the configuration
     */
    public void setConfiguration(String fieldName, String value) {
        mConfig.setOption(fieldName, value);
    }

    /**
     * Sets the configuration to the given device from a pre-existing
     * configuration;
     * 
     * @param config
     *            the new configuration object for the current device
     * TODO: callback on correct/incorrect password
     */
    public void setConfiguration(DeviceConfiguration config) {
        mConfig = config;
    }
    
    // Asynchronous Helpers //

    private class InitThread implements Runnable {
        private final String CONFIG_REQUEST_STRING   = "C";
        private final byte[] CONFIG_REQUEST_BYTES    = CONFIG_REQUEST_STRING.getBytes();
        private final int    CONFIG_RESPONSE_LENGTH  = 1;
        
        private BluetoothListener mListener;
        public InitThread(BluetoothListener cl) {
            mListener = cl;
        }
        public void run() {
            try {
                mOutputStream.write(CONFIG_REQUEST_BYTES);
                byte[] buf = new byte[CONFIG_RESPONSE_LENGTH];
                mInputStream.read(buf);
                mConfig = new DeviceConfiguration(new String(buf));
            } catch (IOException e) {
                mConfig = null;
            } finally {
                mListener.onConfigurationRead(mConfig);
            }
        }
    }
    
    private class WriteThread implements Runnable {
        private final String WRITE_RESPONSE_OKAY     = "K";
        private final int    WRITE_RESPONSE_LENGTH   = 1;
        private final int    CONNECT_RETRY_TIMES     = 2;

        private byte[]            mHeader;
        private byte[]            mMessage;
        private BluetoothListener mListener;
        
        public WriteThread(byte[] message, byte[] header, BluetoothListener cl) {
            mListener = cl;
            mHeader   = header;
            mMessage  = message;
        }
        public void run() {
            int attempt = 0;
            while (attempt < CONNECT_RETRY_TIMES) {
                try {
                    ByteBuffer buf = ByteBuffer.wrap(mHeader);
                    buf.put(mMessage);
                    mOutputStream.write(buf.array());
                    byte[] response = new byte[WRITE_RESPONSE_LENGTH];
                    mInputStream.read(response);
                    if (!(new String(response)).equals(WRITE_RESPONSE_OKAY)) {
                        throw new IOException("Bad response");
                    }
                } catch (IOException e) {
                    attempt++;
                }
            }
            String error = (attempt >= CONNECT_RETRY_TIMES) ? "Bad Response" : "Failed to write";
            mListener.onWriteError(error);
        }
    }
}
