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

package edu.washington.cs.cellasecure;

import java.io.IOException;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import edu.washington.cs.cellasecure.bluetooth.Connection;

public class Drive implements Parcelable {
    private static final UUID   mUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    public static final String KEY_BUNDLE_DRIVE = "drive";
    
    public static final Parcelable.Creator<Drive> CREATOR
        = new Parcelable.Creator<Drive>() {
        public Drive createFromParcel(Parcel in) {
            return new Drive(in);
        }
        
        public Drive[] newArray(int size) {
            return new Drive[size];
        }
    };

    private String mName;
    private BluetoothDevice mDevice;
    private Connection mConnection;
    private boolean mLockStatus;

    
    private Drive(Parcel in) {
        mName = in.readString();
        mDevice = in.readParcelable(null);
        mLockStatus = (Boolean) in.readValue(null);
    }
    
    public Drive(BluetoothDevice bt) {
        mName = bt.getName();
        mDevice = bt;
        mLockStatus = true;
    }
    
    public Drive(String name, BluetoothDevice bt) {
        mName = name;
        mDevice = bt;
        mLockStatus = true;
    }

    public Drive(String name, String address) {
        this(name, BluetoothAdapter.getDefaultAdapter().getRemoteDevice(address));
    }

    @Override
    public int describeContents() { return 0; }

    @Override
    public String toString() { return mName + " " + mDevice.getAddress(); }
    
    public String getAddress() { return mDevice.getAddress(); }
    public String getName() { return mName; }
    public BluetoothDevice getDevice() { return mDevice; }
    public boolean isLocked() { return mLockStatus; }
    public void setLockStatus(boolean status) { mLockStatus = status; } 
    public void setConnection(Connection c) { mConnection = c; }
    
    public void connect(OnConnectedListener cl) {
        new ConnectThread(mDevice, cl).run();
    }
    
    public void sendPassword(String passwd) {
        if (mConnection.isConnected()) 
            mConnection.sendPassword(passwd);
        else
            throw new IllegalArgumentException("Connection must be established");
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((mDevice == null) ? 0 : mDevice.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        Drive other = ( Drive ) obj;
        if (mDevice == null) {
            if (other.mDevice != null) return false;
        } else if (!mDevice.equals(other.mDevice)) return false;
        return true;
    }
    

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mName);
        dest.writeParcelable(mDevice, flags);
        dest.writeValue((Boolean) mLockStatus);
    }
    
    interface OnConnectedListener {
        public void onConnected(Connection connection);
    }
    
    private class ConnectThread implements Runnable {
        private BluetoothDevice mDevice;
        private OnConnectedListener mListener;
        
        public ConnectThread (BluetoothDevice device, OnConnectedListener cl) {
            mDevice = device;
            mListener = cl;
        }
        public void run() {
            Connection c;
            try {
                BluetoothSocket socket = mDevice.createRfcommSocketToServiceRecord(mUUID);
                socket.connect();
                c = new Connection(socket);
            } catch (IOException e) {
                Log.e("Foo", "Failed to connect " + e.getMessage());
                c = null;
            }
            mListener.onConnected(c); // FIXME: Use Handler to send message to UI thread
        }
    }

}
