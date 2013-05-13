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

import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.bluetooth.BluetoothSocket;

/**
 * Utility for managing and executing operations over a connection to a
 * Bluetooth device
 * 
 * @author CellaSecure
 */
public class Connection implements ConnectionInterface {

    private DeviceConfiguration mConfig;
    private BluetoothSocket     mBluetoothSocket;
    private InputStream         mInputStream;
    private OutputStream        mOutputStream;
    private ExecutorService     mPool;

    /**
     * Construct a new connection over the given socket The socket must be
     * established
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
        mConfig = initConfiguration();
        mPool = Executors.newSingleThreadExecutor();

        InputStream is = null;
        OutputStream os = null;
        if (mBluetoothSocket != null) {
            try {
                is = mBluetoothSocket.getInputStream();
                os = mBluetoothSocket.getOutputStream();
            } catch (Exception e) { /* streams remain null */
            }
        }

        mInputStream = is;
        mOutputStream = os;
    }

    /*
     * Initialize the configuration by requesting information over the Bluetooth
     * socket
     */
    private DeviceConfiguration initConfiguration() {
        // TODO:
        DeviceConfiguration config = new DeviceConfiguration();

        // Write config request to bluetooth
        // Receive response
        // Parse into DeviceConfig
        // return
        return config;
    }

    @Override
    public void disconnect() {
        mPool.shutdown();
        try {
            mBluetoothSocket.close();
            mBluetoothSocket = null;
        } catch (Exception e) {
        }
    }

    @Override
    public void read(byte[] buf) {
        mPool.execute(new ReadRunnable(buf));
    }

    @Override
    public void write(byte[] msg) {
        mPool.execute(new WriteRunnable(msg));
    }

    @Override
    public DeviceConfiguration getConfiguration() {
        return mConfig;
    }

    @Override
    public void setConfiguration(String fieldName, String value) {
        mConfig.setOption(fieldName, value);
    }

    @Override
    public void setConfiguration(DeviceConfiguration config) {
        mConfig = config;
    }

    /*
     * Runnable thread for asynchronously reading over the socket
     */
    private class ReadRunnable implements Runnable {
        private byte[] mBuffer;

        public ReadRunnable(byte[] buf) {
            mBuffer = buf;
        }

        @Override
        public void run() {
            try {
                mInputStream.read(mBuffer);
            } catch (Exception e) { /* read failed */
            }
        }
    }

    /*
     * Runnable thread for asynchronously writing over the socket
     */
    private class WriteRunnable implements Runnable {
        private byte[] mMessage;

        public WriteRunnable(byte[] msg) {
            mMessage = msg;
        }

        @Override
        public void run() {
            try {
                mOutputStream.write(mMessage);
            } catch (Exception e) { /* write failed */
            }
        }
    }
}
