package edu.washington.cs.cellasecure.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import edu.washington.cs.cellasecure.R;

public class PasswordInputDialogFragment extends DialogFragment {
    
    private int mEncryptionLevel;

    public PasswordInputDialogFragment() {
        // empty constructor required by DialogFragment
    }
    
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.dialog_password_input)
               .setPositiveButton(R.string.dialog_password_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // clicked OK!
                    }
                })
                .setNegativeButton(R.string.dialog_password_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // clicked Cancel!
                    }
                });
        return builder.create();
    }
}
