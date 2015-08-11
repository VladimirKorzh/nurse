package com.it4medicine.mobilenurse.core.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.activeandroid.query.Select;
import com.it4medicine.mobilenurse.R;
import com.it4medicine.mobilenurse.activities.UserStoredPlacesActivity;
import com.it4medicine.mobilenurse.core.model.vkUserStoredLocation;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by root on 09.08.15.
 */
public class vkPlacePicker extends DialogFragment {

    private long selected = -1;

    public interface onDialogResultListener {
        public void onPlaceSelected(long id);
    }

    public void onPlaceSelected(long id) {
        mListener.onPlaceSelected(id);
    }
    onDialogResultListener mListener;

    public void setmListener(onDialogResultListener mListener) {
        this.mListener = mListener;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final List<vkUserStoredLocation> locations = new Select().from(vkUserStoredLocation.class).execute();

        List<String> arr = new ArrayList<String>();

        for(vkUserStoredLocation location : locations){
            arr.add(location.getName());
            Log.d("add", location.getName()+" "+location.getId());
        }

        CharSequence[] array = arr.toArray(new CharSequence[arr.size()]);
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.title_dialog_select_place)
                .setSingleChoiceItems(array, (int) selected, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        selected = locations.get(which).getId();
                    }
                })
                .setPositiveButton(R.string.dialog_select_place_select, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onPlaceSelected(selected);
                        dismiss();
                    }
                })
                .setNegativeButton(R.string.dialog_select_place_edit, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                        Intent intent = new Intent(getActivity(), UserStoredPlacesActivity.class);
                        startActivity(intent);
                        dismiss();
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}