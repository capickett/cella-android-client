package edu.washington.cs.cellasecure.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import edu.washington.cs.cellasecure.Drive;
import edu.washington.cs.cellasecure.R;

public class DriveSetupFragment extends Fragment {

    private Drive mDrive;    
    
    /* (non-Javadoc)
     * @see android.app.Fragment#onCreate(android.os.Bundle)
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDrive = getArguments().getParcelable(Drive.KEY_BUNDLE_DRIVE);
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Fragment#onCreateView(android.view.LayoutInflater,
     * android.view.ViewGroup, android.os.Bundle)
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater
                .inflate(R.layout.fragment_drive_setup, container, false);
    }

    /* (non-Javadoc)
     * @see android.app.Fragment#onActivityCreated(android.os.Bundle)
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        
        getActivity().getActionBar().setTitle(mDrive.toString());
    }

}
