package edu.washington.cs.cellasecure.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import edu.washington.cs.cellasecure.DriveConnectActivity;
import edu.washington.cs.cellasecure.R;

public class DriveNoDevicesFragment extends Fragment implements OnClickListener {

    Button mNewDevicePrompt;
    View mNewDeviceView;

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Fragment#onCreateView(android.view.LayoutInflater,
     * android.view.ViewGroup, android.os.Bundle)
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_drive_manage_no_devices,
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
        mNewDevicePrompt = (Button) activity
                .findViewById(R.id.drive_manage_new_device_hint);
        mNewDeviceView = activity
                .findViewById(R.id.drive_manage_no_devices_fragment);

        mNewDevicePrompt.setOnClickListener(this);
        mNewDeviceView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.equals(mNewDevicePrompt) || v.equals(mNewDeviceView)) {
            startActivity(new Intent(v.getContext(), DriveConnectActivity.class));
        }
    }
}
