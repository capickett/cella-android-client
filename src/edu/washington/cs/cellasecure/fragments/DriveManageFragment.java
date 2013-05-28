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
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import edu.washington.cs.cellasecure.Drive;
import edu.washington.cs.cellasecure.R;

public class DriveManageFragment extends Fragment {

    /**
     * They key to store and lookup the currently displayed drive into/from the
     * instance state.
     */
    public static final String STATE_DRIVE_KEY = "drive";

    private Drive mCurrentDrive;

    private TextView mLockStatusIndicator;
    
    /*
     * (non-Javadoc)
     * 
     * @see android.app.Fragment#onCreateView(android.view.LayoutInflater,
     * android.view.ViewGroup, android.os.Bundle)
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.e("Foo", "Before inflater");
        View v = inflater.inflate(R.layout.fragment_drive_manage, container,
                false);
        Log.e("Foo", "After inflater");
        return v;
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Fragment#onSaveInstanceState(android.os.Bundle)
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save the drive which details are being displayed
        outState.putParcelable(STATE_DRIVE_KEY, mCurrentDrive);
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Fragment#onActivityCreated(android.os.Bundle)
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Load drive if saved in instance state
        if (savedInstanceState != null) {
            mCurrentDrive = savedInstanceState.getParcelable(STATE_DRIVE_KEY);
        } else {
            Bundle args = getArguments();
            mCurrentDrive = args.getParcelable(Drive.KEY_BUNDLE_DRIVE);

            Fragment frag = new DriveUnlockFragment();
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            frag.setArguments(args);
            transaction.replace(R.id.drive_unlock_fragment_container, frag);
            transaction.commit();

            // Fill in details
            Activity parent = getActivity();
            mLockStatusIndicator = (TextView) parent.findViewById(R.id.drive_manage_lock_status);

            // mLockStatusIndicator
            if (!mCurrentDrive.isLocked()) {
                mLockStatusIndicator.setBackgroundResource(android.R.color.holo_green_dark);
                mLockStatusIndicator.setText(R.string.device_manage_lock_status_unlocked);
            }
        }
    }

}
