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

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Parcel;
import android.os.Parcelable;
import edu.washington.cs.cellasecure.bluetooth.BluetoothUtility;
import edu.washington.cs.cellasecure.bluetooth.Connection;

public class Drive implements Parcelable {

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

    
    private Drive(Parcel in) {
        mName = in.readString();
        mDevice = in.readParcelable(null);
    }
    
    public Drive(BluetoothDevice bt) {
        mName = bt.getName();
        mDevice = bt;
    }
    
    public Drive(String name, BluetoothDevice bt) {
        mName = name;
        mDevice = bt;
    }

    public Drive(String name, String address) {
        
        this(name, BluetoothAdapter.getDefaultAdapter().getRemoteDevice(address));
    }

    @Override
    public int describeContents() { return 0; }
    
    public boolean isLocked() {
        // connect
        // onConnected (Connection)
        return true;
    }

    @Override
    public String toString() { return mName; }
    public String getName() { return mName; }
    public String getAddress() { return mDevice.getAddress(); }
    public BluetoothDevice getDevice() { return mDevice; }

    
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
    }

}
