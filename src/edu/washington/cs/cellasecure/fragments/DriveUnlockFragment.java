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

import java.io.IOException;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import edu.washington.cs.cellasecure.Drive;
import edu.washington.cs.cellasecure.DriveManageActivity;
import edu.washington.cs.cellasecure.R;
import edu.washington.cs.cellasecure.fragments.PasswordInputDialogFragment.PasswordInputDialogListener;

public class DriveUnlockFragment extends Fragment implements View.OnClickListener,
        PasswordInputDialogListener, Drive.OnLockStateChangeListener {
    private final String TAG = "DriveUnlockFragment";
    public static final String KEY_BUNDLE_DRIVE_UNLOCK_FRAGMENT = "drive_unlock_fragment";

    private int mEncryptionLevel;
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
        return inflater.inflate(R.layout.fragment_drive_unlock, container, false);
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
        mEncryptionLevel = args.getInt(DriveManageActivity.KEY_BUNDLE_ENCRYPTION_LEVEL);
        if (mEncryptionLevel == 0) {
            mDrive.unlock("", "");
        } else {
            PasswordInputDialogFragment pidFragment = new PasswordInputDialogFragment();
            pidFragment.show(getFragmentManager(), "fragment_password_input");
        }

        mLockStatus.setText(R.string.device_manage_lock_status_locked);
    }

    @Override
    public void onClick(View v) {
        if (v.equals(mLockStatus) || v.equals(mDriveUnlockView)) {
        }
    }

    @Override
    public void onDialogPositiveClick(DialogFragment df, String password) {
        if (mEncryptionLevel == 1) {
            mDrive.unlock(password, "");
        } else {
            TelephonyManager tManager = 
                    (TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE);
            String uuid = tManager.getDeviceId();
            mDrive.unlock(password, uuid);
        }
    }


    @Override
    public void onDialogNegativeClick(DialogFragment df) {
        Log.d(TAG, "User clicked cancel");
        getActivity().finish();
        return;
    }


    @Override
    public void onLockStateChanged(boolean status, IOException lockStateException) {
        if (lockStateException != null) {
          if (status) {
              mLockStatus.setText(R.string.device_manage_lock_status_locked);
          } else {
              mLockStatus.setText(R.string.device_manage_lock_status_unlocked);
          }
        } else {
            Log.e(TAG, "Locking failed", lockStateException);
            // failed to unlock, do something fail-y
        }
    }
}
