package com.example.android.sunshine.app;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * {@link ForecastAdapter} exposes a list of weather forecasts
 * from a {@link android.database.Cursor} to a {@link android.widget.ListView}.
 */
public class ForecastAdapter extends CursorAdapter {
    public ForecastAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    /**
     * Prepare the weather high/lows for presentation.
     */
    private String formatHighLows(double high, double low) {
        boolean isMetric = Utility.isMetric(mContext);

        String highLowStr = Utility.formatTemperature(high, isMetric) + "/" + Utility.formatTemperature(low, isMetric);
        return highLowStr;
    }

    /*
        This is ported from FetchWeatherTask --- but now we go straight from the cursor to the
        string.
     */
    private String convertCursorRowToUXFormat(Cursor cursor) {
        // get row indices for our cursor
        String highAndLow = formatHighLows(
                cursor.getDouble(ForecastFragment.COL_WEATHER_MAX_TEMP),
                cursor.getDouble(ForecastFragment.COL_WEATHER_MIN_TEMP));

        return Utility.formatDate(cursor.getLong(ForecastFragment.COL_WEATHER_DATE)) +
                " - " + cursor.getString(ForecastFragment.COL_WEATHER_DESC) +
                " - " + highAndLow;
    }

    /*
        Remember that these views are reused as needed.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_forecast, parent, false);

        return view;
    }

    /*
        This is where we fill-in the views with the contents of the cursor.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // our view is pretty simple here --- just a text view
        // we'll keep the UI functional with a simple (and slow!) binding.

        // Read weather icon ID from cursor
        int weatherId = cursor.getInt(ForecastFragment.COL_WEATHER_ID);

        // Use placeholder image for now
        ImageView iconView = (ImageView) view.findViewById(R.id.list_item_icon);
        iconView.setImageResource(R.drawable.ic_launcher);

        TextView weather = (TextView) view.findViewById(R.id.list_item_forecast_textview);
        TextView day = (TextView) view.findViewById(R.id.list_item_day_textview);
        TextView max = (TextView) view.findViewById(R.id.list_item_high_textview);
        TextView min = (TextView) view.findViewById(R.id.list_item_low_textview);

        weather.setText(cursor.getString(ForecastFragment.COL_WEATHER_DESC));
        day.setText(Utility.getFriendlyDayString(view.getContext(), cursor.getLong(ForecastFragment.COL_WEATHER_DATE)));

        boolean isMetric = Utility.isMetric(mContext);
        double maxTemperature = cursor.getDouble(ForecastFragment.COL_WEATHER_MAX_TEMP);
        double minTemperature = cursor.getDouble(ForecastFragment.COL_WEATHER_MIN_TEMP);

        String maxTemp = Utility.formatTemperature(maxTemperature, isMetric);
        String minTemp = Utility.formatTemperature(minTemperature, isMetric);


        max.setText(maxTemp);
        min.setText(minTemp);
    }
}