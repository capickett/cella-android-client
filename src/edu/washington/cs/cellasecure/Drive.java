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
    public String toString() {
        return mName;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public boolean isLocked() {
        // TODO: Complete me
        return true;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mName);
    }

}
