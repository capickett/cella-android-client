package edu.washington.cs.cellasecure.fragments;

import java.io.IOException;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import edu.washington.cs.cellasecure.Drive;
import edu.washington.cs.cellasecure.Drive.OnLockStateChangeListener;
import edu.washington.cs.cellasecure.R;
import edu.washington.cs.cellasecure.bluetooth.DeviceConfiguration;

public class DriveConfigureFragment extends Fragment implements View.OnClickListener {
    private final static String TAG = "DriveConfigureFragment";
    
    private Drive mDrive;
    private Button mLockDriveButton;
    private Button mSetConfigButton;
    private Button mChangePasswordButton;
    
    private RadioGroup mRadioGroup;
    private EditText mPasswordBox;
    
    /*
     * (non-Javadoc)
     *
     * @see android.app.Fragment#onCreateView(android.view.LayoutInflater,
     * android.view.ViewGroup, android.os.Bundle)
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_drive_configure, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Activity activity = getActivity();
        activity.findViewById(R.id.drive_loading_progress).setVisibility(View.GONE);

        Bundle args = getArguments();
        mDrive = (Drive) args.getParcelable(Drive.KEY_BUNDLE_DRIVE);
        
        mRadioGroup = (RadioGroup) activity.findViewById(R.id.encryption_settings_group);
        mPasswordBox = (EditText) activity.findViewById(R.id.password_box);
        mLockDriveButton = (Button) activity.findViewById(R.id.configure_lock_drive);
        mSetConfigButton = (Button) activity.findViewById(R.id.configure_set_configuration);
        mChangePasswordButton = (Button) activity.findViewById(R.id.configure_change_password);
        mLockDriveButton.setOnClickListener(this);
        mSetConfigButton.setOnClickListener(this);
        mChangePasswordButton.setOnClickListener(this);

        mDrive.setOnLockStateChangeListener(new OnLockStateChangeListener() {
            @Override
            public void onLockStateChanged(boolean status, IOException lockStateException) {
                if (!status) {
                    Log.e(TAG, "Locking failed");
                }
            }
        });
    }
    
    @Override
    public void onClick(View v) {
        Log.d(TAG, "Button clicked");
        if (v.equals(mLockDriveButton)) {
            Log.d(TAG, "Lock drive button pressed");
            mDrive.lock();
        } else if (v.equals(mSetConfigButton)) {
            Log.d(TAG, "Set config button pressed");
            int encryptionId = mRadioGroup.getCheckedRadioButtonId();
            int encryptionLevel;
            if (encryptionId == R.id.encryption_level_0)
                encryptionLevel = 0;
            else if (encryptionId == R.id.encryption_level_1)
                encryptionLevel = 1;
            else
                encryptionLevel = 2;
            Log.e(TAG, "encryptionLevel: " + encryptionLevel);
            DeviceConfiguration config = new DeviceConfiguration();
            config.setOption("encryption_level", "" + encryptionLevel);
            //mDrive.sendConfiguration(config);
        } else if (v.equals(mChangePasswordButton)) {
            Log.d(TAG, "Change password button pressed");
            // query user for password
            String password = mPasswordBox.getText().toString();
            Log.d(TAG, "Password: " + password);
//            mDrive.setPassword(password);
        }
    }
}
