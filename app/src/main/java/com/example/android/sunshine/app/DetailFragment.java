package com.example.android.sunshine.app;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.android.sunshine.app.data.WeatherContract;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private String LOG_TAG = DetailFragment.class.getSimpleName();
    static final String DETAIL_URI = "URI";

    private ShareActionProvider mShareActionProvider;
    private String mForecast;
    private Uri mUri;

    private int DETAIL_LOADER = 1;
    private ViewHolder viewHolder;

    private static final String[] FORECAST_COLUMNS = {
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.WeatherEntry.COLUMN_HUMIDITY,
            WeatherContract.WeatherEntry.COLUMN_WIND_SPEED,
            WeatherContract.WeatherEntry.COLUMN_PRESSURE,
            WeatherContract.WeatherEntry.COLUMN_DEGREES,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID
    };

    // these constants correspond to the projection defined above, and must change if the
    // projection changes
    private static final int COL_WEATHER_ID = 0;
    private static final int COL_WEATHER_DATE = 1;
    private static final int COL_WEATHER_DESC = 2;
    private static final int COL_WEATHER_MAX_TEMP = 3;
    private static final int COL_WEATHER_MIN_TEMP = 4;
    private static final int COL_WEATHER_HUMIDITY = 5;
    private static final int COL_WEATHER_WIND = 6;
    private static final int COL_WEATHER_PRESSURE = 7;
    private static final int COL_WEATHER_DEGREES = 8;
    private static final int COL_WEATHER_CONDITION_ID = 9;

    /**
     * Cache of the children views for a forecast list item.
     */
    public static class ViewHolder {

        public final ImageView iconView;
        public final TextView dateView;
        public final TextView descriptionView;
        public final TextView highTempView;
        public final TextView lowTempView;
        public final TextView humidityView;
        public final TextView windView;
        public final TextView pressureView;

        public ViewHolder(View view) {
            iconView = (ImageView) view.findViewById(R.id.detail_icon);
            dateView = (TextView) view.findViewById(R.id.detail_date_textview);
            descriptionView = (TextView) view.findViewById(R.id.detail_forecast_textview);
            highTempView = (TextView) view.findViewById(R.id.detail_high_textview);
            lowTempView = (TextView) view.findViewById(R.id.detail_low_textview);
            humidityView = (TextView) view.findViewById(R.id.detail_humidity_textview);
            windView = (TextView) view.findViewById(R.id.detail_wind_textview);
            pressureView = (TextView) view.findViewById(R.id.detail_pressure_textview);
        }
    }

    public DetailFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Bundle arguments = getArguments();
        if (arguments != null) {
            mUri = arguments.getParcelable(DetailFragment.DETAIL_URI);
        }

        View rootView = inflater.inflate(R.layout.fragment_detail_start, container, false);

        viewHolder = new ViewHolder(rootView);

        return rootView;


    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if ( getActivity() instanceof DetailActivity ){
            // Inflate the menu; this adds items to the action bar if it is present.
            inflater.inflate(R.menu.detail_fragment, menu);
            finishCreatingMenu(menu);
        }
    }

    private Intent weatherIntent() {
        Intent sendIntent = new Intent(Intent.ACTION_SEND);
        sendIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        sendIntent.setType("text/plain");
        sendIntent.putExtra(Intent.EXTRA_TEXT, mForecast + " #Sunshine");

        return sendIntent;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        Log.v(LOG_TAG, "In onCreateLoader");

        if (mUri == null) {
            return null;
        }

        // Now create and return a CursorLoader that will take care of
        // creating a Cursor for the data being displayed.
        return new CursorLoader(
                getActivity(),
                mUri,
                FORECAST_COLUMNS,
                null,
                null,
                null
        );

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        Log.v(LOG_TAG, "In onLoadFinished");
        if (!data.moveToFirst()) { return; }

        int weatherId = data.getInt(COL_WEATHER_CONDITION_ID);


        // Description
        String description = Utility.getStringForWeatherCondition(getActivity(), weatherId);;
        viewHolder.descriptionView.setText(description);
        viewHolder.descriptionView.setContentDescription(getString(R.string.a11y_forecast, description));

        // Use placeholder image for now
        //viewHolder.iconView.setImageResource(Utility.getArtResourceForWeatherCondition(weatherId));
        viewHolder.iconView.setContentDescription(getString(R.string.a11y_forecast_icon, description));

        // Use weather art image
        Glide.with(this)
                .load(Utility.getArtUrlForWeatherCondition(getActivity(), weatherId))
                .error(Utility.getArtResourceForWeatherCondition(weatherId))
                .crossFade()
                .into(viewHolder.iconView);

        // Nicely formatted date
        long date = data.getLong(COL_WEATHER_DATE);
        String dateText = Utility.getFullFriendlyDayString(getActivity(), date);
        viewHolder.dateView.setText(dateText);

        // High temp + min temp
        boolean isMetric = Utility.isMetric(getActivity());

        double maxTemperature = data.getDouble(COL_WEATHER_MAX_TEMP);
        String high = Utility.formatTemperature(getActivity(), maxTemperature, isMetric);
        viewHolder.highTempView.setText(high);
        viewHolder.highTempView.setContentDescription(getString(R.string.a11y_high_temp, high));

        double minTemperature = data.getDouble(COL_WEATHER_MIN_TEMP);
        String low = Utility.formatTemperature(getActivity(), minTemperature, isMetric);
        viewHolder.lowTempView.setText(low);
        viewHolder.highTempView.setContentDescription(getString(R.string.a11y_low_temp, low));

        // Humidity
        float humidity = data.getFloat(COL_WEATHER_HUMIDITY);
        viewHolder.humidityView.setText(getActivity().getString(R.string.format_humidity, humidity));
        viewHolder.humidityView.setContentDescription(viewHolder.humidityView.getText());

        // Wind speed and direction
        float windSpeed = data.getFloat(COL_WEATHER_WIND);
        float degrees = data.getFloat(COL_WEATHER_DEGREES);
        viewHolder.windView.setText(Utility.getFormattedWind(getActivity(), windSpeed, degrees));
        viewHolder.windView.setContentDescription(viewHolder.windView.getText());

        // Pressure
        float pressure = data.getFloat(COL_WEATHER_PRESSURE);
        viewHolder.pressureView.setText(getActivity().getString(R.string.format_pressure, pressure));
        viewHolder.pressureView.setContentDescription(viewHolder.pressureView.getText());

        // Share Intent
        mForecast = String.format("%s - %s - %s/%s", dateText, description, high, low);

        // If onCreateOptionsMenu has already happened, we need to update the share intent now.
        if ( mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(weatherIntent());
        }

        AppCompatActivity activity = (AppCompatActivity)getActivity();
        Toolbar toolbarView = (Toolbar) getView().findViewById(R.id.toolbar);

        // We need to start the enter transition after the data has loaded
        if (activity instanceof DetailActivity) {
            activity.supportStartPostponedEnterTransition();

            if ( null != toolbarView ) {
                activity.setSupportActionBar(toolbarView);

                activity.getSupportActionBar().setDisplayShowTitleEnabled(false);
                activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
        } else {
            if ( null != toolbarView ) {
                Menu menu = toolbarView.getMenu();
                if ( null != menu ) menu.clear();
                toolbarView.inflateMenu(R.menu.detail_fragment);
                finishCreatingMenu(toolbarView.getMenu());
            }
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
        //Toast.makeText(getActivity(), "onLoaderReset", Toast.LENGTH_SHORT).show();
    }

    void onLocationChanged( String newLocation ) {
        // replace the uri, since the location has changed
        Uri uri = mUri;
        if (null != uri) {
            long date = WeatherContract.WeatherEntry.getDateFromUri(uri);
            mUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(newLocation, date);
            getLoaderManager().restartLoader(DETAIL_LOADER, null, this);
        }
    }

    private void finishCreatingMenu(Menu menu) {

        // Retrieve the share menu item
        MenuItem item = menu.findItem(R.id.share);

        // Get the provider and hold onto it to set/change the share intent.
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);

        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(weatherIntent());
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {

            startActivity(new Intent(getActivity(), SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}