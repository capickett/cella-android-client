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

public class Drive implements Parcelable {

    public static final String KEY_BUNDLE_DRIVE = "drive";

    private String mName;
    private BluetoothDevice mDevice;

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
        // TODO: Complete me
        return true;
    }

    @Override
    public String toString() { return mName; }
    public String getName() { return mName; }
    public BluetoothDevice getDevice() { return mDevice; }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mName);
    }

}
