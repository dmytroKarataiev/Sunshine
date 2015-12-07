package com.example.android.sunshine.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;

import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;


public class MainActivity extends ActionBarActivity {

    private final String LOG_TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new ForecastFragment())
                    .commit();
        }
        Log.v("Create", "success");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

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
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        if (id == R.id.map_location)
        {
            openPreferredLocation();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void openPreferredLocation() {

        // gets sharedpreferences parameter and converts it into Uri link
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String zip = prefs.getString(getString(R.string.pref_location_key), getString(R.string.pref_location_default));
        Uri geoLocation = Uri.parse("geo:0,0?").buildUpon()
                .appendQueryParameter("q", zip)
                .build();

        // starts geo intent
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(geoLocation);

        // if there is a way to show the map - starts activity
        if (intent.resolveActivity(this.getPackageManager()) != null) {
            startActivity(intent);
        }
        else {
            Log.d(LOG_TAG, "Couldn't call " + zip + ", no receiving apps installed!");
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.v("Start", "success");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.v("Resume", "success");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.v("Pause", "success");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.v("Stop", "success");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.v("Destroy", "success");
    }



}
