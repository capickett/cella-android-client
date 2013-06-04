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
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import edu.washington.cs.cellasecure.R;

public class PasswordInputDialogFragment extends DialogFragment {
    private static final String TAG = "PasswordInputDialogFragment";
    private PasswordInputDialogListener mListener;
    
    public interface PasswordInputDialogListener {
        public void onDialogPositiveClick (DialogFragment df, String password);
        public void onDialogNegativeClick (DialogFragment df);
    }

    public PasswordInputDialogFragment() {
        // empty constructor required by DialogFragment
    }
    
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View passwordInputView = inflater.inflate(R.layout.dialog_password_input, null);
        builder.setView(passwordInputView)
               .setPositiveButton(R.string.dialog_password_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        EditText passwordEditText = (EditText) passwordInputView.findViewById(R.id.passText);
                        Editable passwordEditable = passwordEditText.getText();
                        String password = (passwordEditable == null) ? "" : passwordEditable.toString(); 
                        mListener.onDialogPositiveClick(PasswordInputDialogFragment.this, password);
                    }
                })
                .setNegativeButton(R.string.dialog_password_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mListener.onDialogNegativeClick(PasswordInputDialogFragment.this);
                    }
                });
        return builder.create();
    }
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (PasswordInputDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement PasswordInputDialogListener");
        }

    }
}
