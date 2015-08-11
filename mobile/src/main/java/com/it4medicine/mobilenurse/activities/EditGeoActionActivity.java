package com.it4medicine.mobilenurse.activities;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.it4medicine.mobilenurse.R;
import com.it4medicine.mobilenurse.core.dialog.vkPlacePicker;
import com.it4medicine.mobilenurse.core.model.vkReminderAction;
import com.it4medicine.mobilenurse.core.model.vkReminderEvent;
import com.it4medicine.mobilenurse.core.model.vkUserStoredLocation;

public class EditGeoActionActivity extends ActionBarActivity {

    private EditText edtIntervalAmount, edtDoseAmount;
    private TextView txtWhere, txtWatcher;
    private AutoCompleteTextView txtName;
    private Spinner spnIntervalType, spnDoseType;

    private static final int PICK_CONTACT_REQUEST = 1;


    private long selected_place_id = -1;
    private String watcher_name;
    private String watcher_phone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_action);

        // Get a reference to the AutoCompleteTextView in the layout
        txtName = (AutoCompleteTextView) findViewById(R.id.autocomplete_action_name);
        // Get the string array
        String[] autocomplete = getResources().getStringArray(R.array.autocomplete_actions);
        // Create the adapter and set it to the AutoCompleteTextView
        ArrayAdapter<String> adapter =
                new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, autocomplete);
        txtName.setAdapter(adapter);

        edtIntervalAmount = (EditText) findViewById(R.id.edtIntervalAmount);
        edtDoseAmount = (EditText) findViewById(R.id.edtDoseAmount);

        spnIntervalType = (Spinner) findViewById(R.id.spnTimesWhat);
// Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> intervalTypeAdapter = ArrayAdapter.createFromResource(this,
                R.array.interval_type, android.R.layout.simple_spinner_item);
// Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
// Apply the adapter to the spinner
        spnIntervalType.setAdapter(intervalTypeAdapter);

        spnDoseType = (Spinner) findViewById(R.id.spnDoseType);
// Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> doseTypeAdapter = ArrayAdapter.createFromResource(this,
                R.array.dose_type, android.R.layout.simple_spinner_item);
// Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
// Apply the adapter to the spinner
        spnDoseType.setAdapter(doseTypeAdapter);

        txtWhere = (TextView) findViewById(R.id.txtWhere);
        txtWhere.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                vkPlacePicker placePicker = new vkPlacePicker();
                placePicker.setmListener(new vkPlacePicker.onDialogResultListener() {
                    @Override
                    public void onPlaceSelected(long id) {
                        Log.d("selected id: ", String.valueOf(id));
                        vkUserStoredLocation location = vkUserStoredLocation.load(vkUserStoredLocation.class, id);
                        Log.d("selected name: ", location.getName());
                        txtWhere.setText(location.getName());
                        selected_place_id = id;
                    }
                });
                placePicker.show(getSupportFragmentManager(), "placePicker");
            }
        });
        txtWatcher = (TextView) findViewById(R.id.txtWatcher);
        txtWatcher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent pickIntent = new Intent(Intent.ACTION_GET_CONTENT);
                pickIntent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
                startActivityForResult(pickIntent, PICK_CONTACT_REQUEST);
            }
        });

        Button btnSave = (Button) findViewById(R.id.btnEditActionSave);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vkUserStoredLocation location = vkUserStoredLocation.load(vkUserStoredLocation.class,
                                                                            selected_place_id);

//                vkReminderAction reminderAction = new vkReminderAction().setFireOnce(true)
//                                                        .setName(txtName.getText().toString())
//                                                        .setPlace(location)
//                                                        .setWatcherName(watcher_name)
//                                                        .setWatcherPhone(watcher_phone);
//                reminderAction.save();



            }
        });

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == PICK_CONTACT_REQUEST) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                // Get the URI that points to the selected contact
                Uri contactUri = data.getData();
                // We only need the NUMBER column, because there will be only one row in the result
                String[] projection = {ContactsContract.Contacts.DISPLAY_NAME,
                        ContactsContract.CommonDataKinds.Phone.NUMBER};

                // Perform the query on the contact to get the NUMBER column
                // We don't need a selection or sort order (there's only one result for the given URI)
                // CAUTION: The query() method should be called from a separate thread to avoid blocking
                // your app's UI thread. (For simplicity of the sample, this code doesn't do that.)
                // Consider using CursorLoader to perform the query.
                Cursor cursor = getContentResolver()
                        .query(contactUri, projection, null, null, null);
                cursor.moveToFirst();

                // Retrieve the phone number from the NUMBER column
                watcher_phone = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                watcher_name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                txtWatcher.setText(watcher_name+" "+watcher_phone);
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_edit_action, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
