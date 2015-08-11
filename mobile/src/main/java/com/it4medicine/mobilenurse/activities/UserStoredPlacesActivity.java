package com.it4medicine.mobilenurse.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.internal.widget.AdapterViewCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.activeandroid.query.From;
import com.activeandroid.query.Select;
import com.dexafree.materialList.cards.SmallImageCard;
import com.dexafree.materialList.controller.OnDismissCallback;
import com.dexafree.materialList.controller.RecyclerItemClickListener;
import com.dexafree.materialList.model.Card;
import com.dexafree.materialList.model.CardItemView;
import com.dexafree.materialList.view.MaterialListView;
import com.it4medicine.mobilenurse.R;
import com.it4medicine.mobilenurse.core.model.vkUserStoredLocation;

import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.recyclerview.animators.SlideInLeftAnimator;

public class UserStoredPlacesActivity extends ActionBarActivity {

    private Activity activity;
    private int selected_pos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_stored_places);

        activity = this;
        List<vkUserStoredLocation> locations = new Select().from(vkUserStoredLocation.class).execute();

        if (locations != null && locations.size() > 0 ) {
            // data is inserted before
        } else {
            vkUserStoredLocation location = new vkUserStoredLocation();
            location.setName("Home").setAddress("г. Киев, ул. Челябинская 5б").save();

            location = new vkUserStoredLocation();
            location.setName("Olga's parents").setAddress("г. Киев, ул. Героев Днепра 19").save();

            location = new vkUserStoredLocation();
            location.setName("Vladimir's parents").setAddress("г. Киев, ул. Флоренции 9").save();
        }

        locations = new Select().from(vkUserStoredLocation.class).execute();
        
        MaterialListView mListView = (MaterialListView) findViewById(R.id.lstPlaces);


        for (vkUserStoredLocation location : locations) {
            SmallImageCard card = new SmallImageCard(this);
            card.setTitle(location.getName());
            card.setDescription(location.getAddress());
            card.setTag(location);
            card.setDismissible(true);
            mListView.add(card);
        }

        mListView.setItemAnimator(new SlideInLeftAnimator());
        mListView.getItemAnimator().setAddDuration(1000);
        mListView.getItemAnimator().setRemoveDuration(1000);
        mListView.getItemAnimator().setMoveDuration(1000);
        mListView.getItemAnimator().setChangeDuration(1000);


        mListView.addOnItemTouchListener(new RecyclerItemClickListener.OnItemClickListener() {

            @Override
            public void onItemClick(CardItemView view, int position) {
                Log.d("CARD_TYPE", "single click "+ position);
                selected_pos = position;

                int[] attrs = new int[]{R.attr.selectableItemBackground};
                TypedArray typedArray = activity.obtainStyledAttributes(attrs);
                int backgroundResource = typedArray.getResourceId(0, 0);
                view.setBackgroundResource(backgroundResource);
            }

            @Override
            public void onItemLongClick(CardItemView view, int position) {
                //Log.d("LONG_CLICK", view.getTag().toString());
                Intent intent = new Intent(activity, UserStoredPlaceEditActivity.class);
                vkUserStoredLocation location = (vkUserStoredLocation) view.getTag();

                intent.putExtra("location_id", location.getId());
                startActivity(intent);
            }
        });

        mListView.setOnDismissCallback(new OnDismissCallback() {
            @Override
            public void onDismiss(Card card, int position) {
                Log.d("Dismiss", String.valueOf(position));
                vkUserStoredLocation location = (vkUserStoredLocation) card.getTag();
                location.delete();
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fabPlaceAddNew);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(activity, UserStoredPlacesActivity.class);
                startActivity(intent);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_user_stored_places, menu);
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
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
