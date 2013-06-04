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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.bluetooth.BluetoothSocket;
import android.util.Log;
import edu.washington.cs.cellasecure.Drive;

/**
 * Utility for managing and executing operations over a connection to a
 * Bluetooth device
 *
 * @author CellaSecure
 */
public class Connection {

    @Override
    public String toString() {
        return "Connection [mBluetoothSocket=" + mBluetoothSocket + ", " +
                "mInputStream=" + mInputStream + ", mOutputStream=" + mOutputStream + "]";
    }

    private static ExecutorService sPool = Executors.newSingleThreadExecutor();

    private BluetoothSocket mBluetoothSocket;
    private BufferedInputStream mInputStream;
    private BufferedOutputStream mOutputStream;
    private OnResponseListener mOnResponseListener;

    /**
     * Construct a new connection over the given socket The socket must be
     * established.  Calls onConfigurationRead with the devices configuration
     * if successful, otherwise calls with null.
     *
     * @param socket the BluetoothSocket which the device is connected to
     * @throws IllegalArgumentException if socket is not connected
     */
    public Connection(BluetoothSocket socket) {
        if (!socket.isConnected()) {
            throw new IllegalArgumentException("Socket must be connected");
        }
        mBluetoothSocket = socket;

        InputStream is = null;
        OutputStream os = null;
        try {
            is = mBluetoothSocket.getInputStream();
            os = mBluetoothSocket.getOutputStream();
        } catch (Exception e) { /* streams remain null */ }

        mInputStream = new BufferedInputStream(is);
        mOutputStream = new BufferedOutputStream(os);
    }

    public boolean isConnected() {
        return mBluetoothSocket.isConnected();
    }

    public void close() throws IOException {
        mBluetoothSocket.close();
    }


    public void setOnResponseListener(OnResponseListener listener) {
        mOnResponseListener = listener;
    }

    public void send(byte[] message, int maxResponseSize) {
        sPool.execute(new SendMessageTask(mInputStream, mOutputStream, message, maxResponseSize,
                                         mOnResponseListener));
    }

    public interface OnResponseListener {
        /**
         * A callback to notify the client that the BT device has responded with the following
         *
         * @param message the message sent back from the BT device
         * @param e       on error, the exception raised
         */
        public void onResponse(byte[] message, IOException e);
    }

    private static class SendMessageTask implements Runnable {

        private static final String TAG = "SendMessageTask";

//        private static final int MAX_READ_TIME_MILLIS = 5000;

        private final int mMaxResponseSize;
        private final InputStream mInStream;
        private final OutputStream mOutStream;
        private final byte[] mMessage;
        private final OnResponseListener mListener;
//        private final Handler mTimeoutHandler = new Handler();

        public SendMessageTask(InputStream is, OutputStream os, byte[] message,
                               int maxResponseSize, OnResponseListener listener) {
            Log.d(TAG, "Created send message task");
            mInStream = is;
            mOutStream = os;
            mMessage = message;
            mMaxResponseSize = maxResponseSize;
            mListener = listener;
        }

        @Override
        public void run() {
            Log.d(TAG, "Running send message task");
            try {
                mOutStream.write(mMessage);
                mOutStream.flush();
            } catch (IOException e) {
                Log.e(TAG, "Error sending message", e);
                if (mListener != null) {
                    mListener.onResponse(null, e);
                }
            }

//            Runnable delayTimer = new Runnable() {
//                @Override
//                public void run() {
//                    Log.e(TAG, "Read timeout");
//                }
//            };
//            mTimeoutHandler.postDelayed(delayTimer, MAX_READ_TIME_MILLIS);

            byte[] response = new byte[mMaxResponseSize];
            for (int i = mMaxResponseSize; i > 0; --i) {
                try {
                    byte next = (byte) mInStream.read();
                    if (next == Drive.RESPONSE_BAD_BYTE) {
                        break;
                    }
                    response[mMaxResponseSize - i] = next;
                } catch (IOException e) {
                    Log.e(TAG, "Error reading response", e);
                    if (mListener != null) {
                        mListener.onResponse(null, e);
                    }
                }
            }

            // Flush read buffer
            // try {
            //     mInStream.skip(mInStream.available());
            // } catch (IOException ignored) {
            // }

//            mTimeoutHandler.removeCallbacks(delayTimer);
            if (mListener != null) {
                mListener.onResponse(response, null);
            }
        }
    }
}
