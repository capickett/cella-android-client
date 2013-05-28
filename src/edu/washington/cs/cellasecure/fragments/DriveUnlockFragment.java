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

package edu.washington.cs.cellasecure.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import edu.washington.cs.cellasecure.Drive;
import edu.washington.cs.cellasecure.R;
import edu.washington.cs.cellasecure.bluetooth.Connection;

public class DriveUnlockFragment extends Fragment implements View.OnClickListener {

    private Connection mConnection; 
    private Button mLockStatus;
    private View mDriveUnlockView;
    private Drive mDrive;
    
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
        activity.findViewById(R.id.drive_loading_progress).setVisibility(View.GONE);
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
    }

    @Override
    public void onClick(View v) {
        Log.e("Foo", "onClick");
        if (v.equals(mLockStatus) || v.equals(mDriveUnlockView)) {
            mConnection.sendPassword("12345678");
        }
    }
    
    public void isLocked(BluetoothDevice device, boolean status) {
        Activity parent = getActivity();
        TextView lsi = (TextView) parent.findViewById(R.id.drive_manage_lock_status);
        if (status) {
            mLockStatus.setText(R.string.device_manage_lock_status_locked);
            lsi.setBackgroundResource(android.R.color.holo_red_dark);
            lsi.setText(R.string.device_manage_lock_status_unlocked);
        } else {
            mLockStatus.setText(R.string.device_manage_lock_status_unlocked);
            lsi.setBackgroundResource(android.R.color.holo_green_dark);
            lsi.setText(R.string.device_manage_lock_status_unlocked);
        }
    }
}
