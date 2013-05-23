package edu.washington.cs.cellasecure.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import edu.washington.cs.cellasecure.Drive;
import edu.washington.cs.cellasecure.DriveConnectActivity;
import edu.washington.cs.cellasecure.R;
import edu.washington.cs.cellasecure.bluetooth.BluetoothUtility;
import edu.washington.cs.cellasecure.bluetooth.Connection;
import edu.washington.cs.cellasecure.bluetooth.DeviceConfiguration;

import java.util.List;

public class DriveUnlockFragment extends Fragment implements View.OnClickListener, BluetoothUtility.BluetoothListener {

    private BluetoothUtility mBT;
    private Button mLockStatus;
    private View mDriveUnlockView;
    private Drive mDrive;

    private Connection mConnection = null;

    /*
     * (non-Javadoc)
     *
     * @see android.app.Fragment#onCreateView(android.view.LayoutInflater,
     * android.view.ViewGroup, android.os.Bundle)
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_drive_unlock,
                container, false);
    }

    /*
     * (non-Javadoc)
     *
     * @see android.app.Fragment#onActivityCreated(android.os.Bundle)
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Activity activity = getActivity();
        mLockStatus = (Button) activity.findViewById(R.id.fragment_drive_unlock_status);
        mLockStatus.setOnClickListener(this);
        mDriveUnlockView = (View) activity.findViewById(R.id.drive_manage_fragment_container);
        mDriveUnlockView.setOnClickListener(this);

        Bundle args = getArguments();
        mDrive = args.getParcelable(Drive.KEY_BUNDLE_DRIVE);

        if (mDrive.isLocked()) {
            mLockStatus.setText(R.string.device_manage_lock_status_locked);
        } else {
            mLockStatus.setText(R.string.device_manage_lock_status_unlocked);
        }
        mBT = new BluetoothUtility(activity, this);
        mBT.connect(mDrive.getDevice());
    }

    @Override
    public void onClick(View v) {
        if (v.equals(mLockStatus) || v.equals(mDriveUnlockView)) {
            if (mConnection != null) {
                mConnection.sendPassword("12345678");
            }
        }
    }

    @Override
    public void onConnected(BluetoothDevice device, Connection connection) { mConnection = connection; }

    @Override
    public void onDiscovery(List<BluetoothDevice> bluetoothDevices) { /* Do nothing */ }

    @Override
    public void onConfigurationRead(DeviceConfiguration config) {

    }

    @Override
    public void onWriteError(String error) {

    }
}
