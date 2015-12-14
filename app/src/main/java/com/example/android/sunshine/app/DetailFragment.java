package com.example.android.sunshine.app;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.sunshine.app.data.WeatherContract;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private String LOG_TAG = DetailFragment.class.getSimpleName();
    private ShareActionProvider mShareActionProvider;
    private String mForecast;

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
        public final TextView friednlyDateView;
        public final TextView dateView;
        public final TextView descriptionView;
        public final TextView highTempView;
        public final TextView lowTempView;
        public final TextView humidityView;
        public final TextView windView;
        public final TextView pressureView;

        public ViewHolder(View view) {
            iconView = (ImageView) view.findViewById(R.id.detail_icon);
            friednlyDateView = (TextView) view.findViewById(R.id.detail_day_textview);
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

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        viewHolder = new ViewHolder(rootView);

        return rootView;


    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.detail_fragment, menu);

        // Retrieve the share menu item
        MenuItem item = menu.findItem(R.id.share);

        // Get the provider and hold onto it to set/change the share intent.
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);


        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(weatherIntent());
        }
        else
        {
            Log.v(LOG_TAG, "fail");
        }
    }

    private Intent weatherIntent() {
        Intent sendIntent = new Intent(Intent.ACTION_SEND);
        sendIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
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

        Intent intent = getActivity().getIntent();

        if (intent == null) {
            return null;
        }

        // Now create and return a CursorLoader that will take care of
        // creating a Cursor for the data being displayed.
        return new CursorLoader(
                getActivity(),
                intent.getData(),
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

        // Use placeholder image for now
        int weatherId = data.getInt(COL_WEATHER_CONDITION_ID);
        viewHolder.iconView.setImageResource(Utility.getArtResourceForWeatherCondition(weatherId));

        // Description
        String description = data.getString(COL_WEATHER_DESC);
        viewHolder.descriptionView.setText(description);

        // Nicely formatted date
        long date = data.getLong(COL_WEATHER_DATE);
        String friendlyDateText = Utility.getFriendlyDayString(getActivity(), date);
        String dateText = Utility.getFormattedMonthDay(getActivity(), date);
        viewHolder.friednlyDateView.setText(friendlyDateText);
        viewHolder.dateView.setText(dateText);

        // High temp + min temp
        boolean isMetric = Utility.isMetric(getActivity());

        double maxTemperature = data.getDouble(COL_WEATHER_MAX_TEMP);
        String high = Utility.formatTemperature(getActivity(), maxTemperature, isMetric);
        viewHolder.highTempView.setText(high);

        double minTemperature = data.getDouble(COL_WEATHER_MIN_TEMP);
        String low = Utility.formatTemperature(getActivity(), minTemperature, isMetric);
        viewHolder.lowTempView.setText(low);

        // Humidity
        float humidity = data.getFloat(COL_WEATHER_HUMIDITY);
        viewHolder.humidityView.setText(getActivity().getString(R.string.format_humidity, humidity));

        // Wind speed and direction
        float windSpeed = data.getFloat(COL_WEATHER_WIND);
        float degrees = data.getFloat(COL_WEATHER_DEGREES);
        viewHolder.windView.setText(Utility.getFormattedWind(getActivity(), windSpeed, degrees));

        // Pressure
        float pressure = data.getFloat(COL_WEATHER_PRESSURE);
        viewHolder.pressureView.setText(getActivity().getString(R.string.format_pressure, pressure));

        // Share Intent
        mForecast = String.format("%s - %s - %s/%s", dateText, description, high, low);

        // If onCreateOptionsMenu has already happened, we need to update the share intent now.
        if ( mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(weatherIntent());
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
        Toast.makeText(getActivity(), "onLoaderReset", Toast.LENGTH_SHORT).show();
    }



}