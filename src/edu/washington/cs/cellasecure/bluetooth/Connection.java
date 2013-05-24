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
import android.util.Log;

/**
 * Utility for managing and executing operations over a connection to a
 * Bluetooth device
 * 
 * @author CellaSecure
 */
public class Connection {
    
    private static final byte LOCK_STATE_QUERY_BYTE = '?';
    private static final byte CONFIG_REQUEST_BYTE   = 'c'; 
    private static final byte PASSWD_REQUEST_BYTE   = 'p';
    private static final int  PASSWD_MAX_LENGTH     = 32;
    
    private static ExecutorService sPool = Executors.newSingleThreadExecutor();

    private DeviceConfiguration mConfig;
    private BluetoothSocket     mBluetoothSocket;
    private InputStream         mInputStream;
    private OutputStream        mOutputStream;
    
    private OnWriteFeedbackListener     mWriteListener;
    private OnConfigurationListener     mConfigListener;
    
    /**
     * Construct a new connection over the given socket The socket must be
     * established.  Calls onConfigurationRead with the devices configuration
     * if successful, otherwise calls with null.
     * @see BluetoothListener
     * 
     * @param socket
     *            the BluetoothSocket which the device is connected to
     * @throws IllegalArgumentException
     *             if socket is null
     */
    public Connection(BluetoothSocket socket) {
        if (socket == null) {
            throw new IllegalArgumentException("Socket must be established");
        }
        mBluetoothSocket = socket;
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
        
        mWriteListener  = null;
        mConfigListener = null;
        
        getConfiguration();
    }

    /**
     * Gracefully end connection with a Bluetooth device
     */
    public void disconnect() {
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
            mConfigListener.onConfigurationRead(mConfig);
        else
            if (sPool.isShutdown()) {
                Log.e("MainActivity", "sPool is shutdown");
            } 
            sPool.execute(new InitThread(mConfigListener));
    }
    
    /**
     * Sends the current configuration to the device, updating the device if necessary
     */
    public void sendConfiguration() {
        ByteBuffer message = ByteBuffer.wrap(new byte[]{CONFIG_REQUEST_BYTE});
        message.put(mConfig.configBytes());
        sPool.execute(new WriteThread(message.array(), mWriteListener));
    }
    
    /**
     * Sends a password to unlock the drive over Bluetooth.  If this fails, will
     * call onWriteError with an appropriate message.
     * @see BluetoothListener.onWriteError(String message)
     * 
     * @param passwd  The user's password which unlocks the drive
     * TODO: callback on correct/incorrect password
     *
     */
    public void sendPassword(String passwd) {
        if (passwd.length() > PASSWD_MAX_LENGTH)
            throw new IllegalArgumentException("password is too long");
        byte[] message = new byte[PASSWD_MAX_LENGTH + 1];
        message[0] = PASSWD_REQUEST_BYTE;
        System.arraycopy(passwd.getBytes(), 0, message, 1, passwd.length());
        sPool.execute(new WriteThread(message, mWriteListener));
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
     */
    public void setConfiguration(DeviceConfiguration config) {
        mConfig = config;
    }
    
    /**
     * Sets the handler for configuration related callbacks
     * @param cl    the listener to handle config callbacks
     */
    public void setOnConfigurationListener (OnConfigurationListener cl) {
        mConfigListener = cl;
    }
    
    /**
     * Sets the handler for write response related callbacks
     * @param wl    the listener to handle write related callbacks
     */
    public void setOnWriteFeedbackListener (OnWriteFeedbackListener wl) {
        mWriteListener = wl;
    }
    
    // Asynchronous Helpers //

    private class InitThread implements Runnable {
        private final String CONFIG_REQUEST_STRING   = "C";
        private final byte[] CONFIG_REQUEST_BYTES    = CONFIG_REQUEST_STRING.getBytes();
        private final int    CONFIG_RESPONSE_LENGTH  = 1;
        
        private OnConfigurationListener mListener;
        
        public InitThread(OnConfigurationListener cl) {
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
                if (mListener != null) mListener.onConfigurationRead(mConfig);
            }
        }
    }
    
    private class WriteThread implements Runnable {
        private final String WRITE_RESPONSE_OKAY     = "K";
        private final int    WRITE_RESPONSE_LENGTH   = 1;
        private final int    CONNECT_RETRY_TIMES     = 2;

        private byte[]            mMessage;      
        private OnWriteFeedbackListener     mListener;
        
        public WriteThread(byte[] message, 
                OnWriteFeedbackListener listener) {
            mWriteListener = listener;
            mMessage  = message;
        }
        public void run() {
            int attempt = 0;
            while (attempt < CONNECT_RETRY_TIMES) {
                try {
                    mOutputStream.write(mMessage);
                    byte[] response = new byte[WRITE_RESPONSE_LENGTH];
                    mInputStream.read(response);
                    
                    if (!(new String(response)).equals(WRITE_RESPONSE_OKAY)) {
                        throw new IOException("Bad response");
                    }
                    if (response[0] == LOCK_STATE_QUERY_BYTE) {
                        // LOCK STATE LISTENER
                    } else {
                        if (mListener != null)
                            mListener.onWriteResponse(new String(response));
                    }
                    break;
                } catch (IOException e) {
                    if (attempt < CONNECT_RETRY_TIMES) {
                        ++attempt;
                    } else {
                        String error = 
                                (attempt >= CONNECT_RETRY_TIMES) ? "Bad Response" : "Failed to write";
                        if (mListener != null) mListener.onWriteError(error);
                    }
                }
            }
        }
    }
    
    public interface OnConfigurationListener {
        /**
         * Callback to notify a client when configuration is received
         * @param config
         *            the configuration received from Bluetooth device, null if failure
         */
        public void onConfigurationRead(DeviceConfiguration config);
    }
    
    public interface OnWriteFeedbackListener {
        /**
         * Callback to notify a client that socket failed to write to Bluetooth device
         * @param error
         *            a string description of the failure
         */
        public void onWriteError(String error);  
        
        public void onWriteResponse(String response);
    }
    
    public interface OnLockQueryListener {
        public boolean isLocked();
    }
}
