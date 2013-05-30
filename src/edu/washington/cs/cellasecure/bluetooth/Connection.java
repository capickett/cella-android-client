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

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Utility for managing and executing operations over a connection to a
 * Bluetooth device
 *
 * @author CellaSecure
 */
public class Connection {

    @Override
    public String toString() {
        return "Connection [mConfig=" + mConfig + ", mBluetoothSocket=" + mBluetoothSocket + ", mInputStream=" + mInputStream + ", mOutputStream=" + mOutputStream + ", mWriteListener=" + mWriteListener + ", mConfigListener=" + mConfigListener + ", mLockListener=" + mLockListener + "]";
    }

    private static final byte LOCK_STATE_QUERY_BYTE = '?';
    private static final byte CONFIG_REQUEST_BYTE = 'g';
    private static final byte CONFIG_SEND_BYTE = 'c';
    private static final byte PASSWD_REQUEST_BYTE = 'p';

    private static final int PASSWD_MAX_LENGTH = 32;

    private static ExecutorService sPool = Executors.newSingleThreadExecutor();

    private DeviceConfiguration mConfig;
    private BluetoothSocket mBluetoothSocket;
    private BufferedInputStream mInputStream;
    private BufferedOutputStream mOutputStream;

    private OnWriteFeedbackListener mWriteListener;
    private OnConfigurationListener mConfigListener;
    private OnLockQueryListener mLockListener;

    /**
     * Construct a new connection over the given socket The socket must be
     * established.  Calls onConfigurationRead with the devices configuration
     * if successful, otherwise calls with null.
     *
     * @param socket the BluetoothSocket which the device is connected to
     * @throws IllegalArgumentException if socket is null
     * @see OnConfigurationListener
     */
    public Connection(BluetoothSocket socket) {
        if (!socket.isConnected()) throw new IllegalArgumentException("Socket must be connected");
        mBluetoothSocket = socket;

        InputStream is = null;
        OutputStream os = null;
        try {
            is = mBluetoothSocket.getInputStream();
            os = mBluetoothSocket.getOutputStream();
        } catch (Exception e) { /* streams remain null */ }

        mInputStream = new BufferedInputStream(is);
        mOutputStream = new BufferedOutputStream(os);

        // getConfiguration();
    }

    public boolean isConnected() {
        return mBluetoothSocket.isConnected();
    }

    /**
     * Gets configuration for Bluetooth device, returning it through
     * the onConfigurationRead callback.
     *
     * @see OnConfigurationListener
     */
    public void getConfiguration() {
        if (mConfig != null) if (mConfigListener != null) mConfigListener.onConfigurationRead(mConfig);
        else sPool.execute(new InitThread(mConfigListener));
    }

    public void getLockStatus() {
        sPool.execute(new WriteThread(new byte[]{LOCK_STATE_QUERY_BYTE}, mBluetoothSocket, mConfigListener, mWriteListener, mLockListener));
    }

    /**
     * Sends the current configuration to the device, updating the device if necessary
     */
    public void sendConfiguration() {
        sPool.execute(new WriteThread(cons(CONFIG_SEND_BYTE, mConfig.getBytes()), mBluetoothSocket, mConfigListener, mWriteListener, mLockListener));
    }

    /**
     * Sends a password to unlock the drive over Bluetooth.  If this fails, will
     * call onWriteError with an appropriate message.
     *
     * @param passwd The user's password which unlocks the drive
     * @see OnWriteFeedbackListener
     */
    public void sendPassword(String passwd) {
        if (mBluetoothSocket != null) {
            if (passwd.length() > PASSWD_MAX_LENGTH) throw new IllegalArgumentException("password is too long");
            byte[] message = new byte[PASSWD_MAX_LENGTH + 1];
            message[0] = PASSWD_REQUEST_BYTE;
            System.arraycopy(passwd.getBytes(), 0, message, 1, passwd.length());
            sPool.execute(new WriteThread(message, mBluetoothSocket, mConfigListener, mWriteListener, mLockListener));
        }
    }

    /**
     * Sets the given fieldName to the given value for this device
     *
     * @param fieldName the name of the configuration field to be modified
     * @param value     the new value for fieldName in the configuration
     */
    public void setConfiguration(String fieldName, String value) {
        mConfig.setOption(fieldName, value);
    }

    /**
     * Sets the configuration to the given device from a pre-existing
     * configuration;
     *
     * @param config the new configuration object for the current device
     */
    public void setConfiguration(DeviceConfiguration config) {
        mConfig = config;
    }

    /**
     * Sets the handler for configuration related callbacks
     *
     * @param cl the listener to handle config callbacks
     */
    public void setOnConfigurationListener(OnConfigurationListener cl) {
        mConfigListener = cl;
    }

    /**
     * Sets the handler for write response related callbacks
     *
     * @param wl the listener to handle write related callbacks
     */
    public void setOnWriteFeedbackListener(OnWriteFeedbackListener wl) {
        mWriteListener = wl;
    }

    /**
     * Sets the handler for the lock response callback
     *
     * @param ll the listener to handle lock status callbacks
     */
    public void setOnLockQueryListener(OnLockQueryListener ll) {
        mLockListener = ll;
    }

    /**
     * Combine header and body into one byte array
     *
     * @param header byte to be front of list
     * @param body   rest of message
     * @return header::body
     */
    private byte[] cons(byte header, byte[] body) {
        byte[] packet = new byte[body.length + 1];
        packet[0] = header;
        System.arraycopy(body, 0, packet, 1, body.length);
        return packet;
    }

    // Asynchronous Helpers //

    private class InitThread implements Runnable {
        private final int CONFIG_RESPONSE_LENGTH = 2;

        private OnConfigurationListener mListener;

        public InitThread(OnConfigurationListener cl) {
            mListener = cl;
        }

        public void run() {
            try {
                mOutputStream.write(CONFIG_REQUEST_BYTE);
                byte[] buf = new byte[CONFIG_RESPONSE_LENGTH];
                mInputStream.read(buf);
                if (buf[0] == 'K') mConfig = new DeviceConfiguration(new String(buf));
                else throw new IOException("Bad configuration");
            } catch (IOException e) {
                mConfig = null;
            } finally {
                if (mListener != null) mListener.onConfigurationRead(mConfig);
            }
        }
    }

    private class WriteThread implements Runnable {
        private final int MAX_WRITE_RESPONSE_LENGTH = 2;
        private final int CONNECT_RETRY_TIMES = 2;

        private final byte WRITE_RESPONSE_OKAY = 'K';
        private final byte LOCK_RESPONSE_OKAY = '?';
        private final byte CONFIG_RESPONSE_OKAY = 'C';
        private final byte DEVICE_LOCKED_RESPONSE = 'L';

        private byte[] mMessage;
        private OnConfigurationListener mConfigListener;
        private OnWriteFeedbackListener mWriteListener;
        private OnLockQueryListener mLockListener;

        public WriteThread(byte[] message, BluetoothSocket socket, OnConfigurationListener clistener, OnWriteFeedbackListener wlistener, OnLockQueryListener llistener) {
            mWriteListener = wlistener;
            mLockListener = llistener;
            mMessage = message;
        }

        public void run() {
            int attempt = 0;
            while (attempt < CONNECT_RETRY_TIMES) {
                try {
                    Log.e("Foo", "Connection: " + new String(mMessage));
                    mOutputStream.write(mMessage);
                    byte[] response = new byte[MAX_WRITE_RESPONSE_LENGTH];
                    mInputStream.read(response);
                    Log.e("Foo", "response: " + new String(response));
                    if (response[0] == LOCK_RESPONSE_OKAY) {
                        if (mLockListener != null) mLockListener.isLocked(response[1] == DEVICE_LOCKED_RESPONSE);
                    } else if (response[0] == WRITE_RESPONSE_OKAY) {
                        if (mWriteListener != null) mWriteListener.onWriteResponse(new String(response));
                    } else if (response[0] == CONFIG_RESPONSE_OKAY) {
                        if (mConfigListener != null) {
                            mConfigListener.onConfigurationRead(new DeviceConfiguration((new String(response)).substring(1)));
                        }
                    }
                    break;
                } catch (IOException e) {
                    if (attempt < CONNECT_RETRY_TIMES) {
                        ++attempt;
                    } else {
                        String error = (attempt >= CONNECT_RETRY_TIMES) ? "Bad Response" : "Failed to write";
                        if (mWriteListener != null) mWriteListener.onWriteError(error);
                    }
                }
            }
        }
    }

    public interface OnConfigurationListener {
        /**
         * Callback to notify a client when configuration is received
         *
         * @param config the configuration received from Bluetooth device, null if failure
         */
        public void onConfigurationRead(DeviceConfiguration config);
    }

    public interface OnWriteFeedbackListener {
        /**
         * Callback to notify a client that socket failed to write to Bluetooth device
         *
         * @param error a string description of the failure
         */
        public void onWriteError(String error);

        /**
         * Callback to notify a client that writing was successful, and responded with
         * the given message
         *
         * @param response the response from the device written to
         */
        public void onWriteResponse(String response);
    }

    public interface OnLockQueryListener {
        /**
         * Callback to notify a client whether a device is unlocked or not
         *
         * @param status true is device is locked, false otherwise
         * @return true if device is locked, false otherwise
         */
        public void isLocked(boolean status);
    }
}
