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

    private final String LOG_TAG = ForecastAdapter.class.getSimpleName();

    private static final int VIEW_TYPE_TODAY = 0;
    private static final int VIEW_TYPE_FUTURE_DAY = 1;
    private static final int VIEW_TYPE_COUNT = 2;
    private boolean mUseTodayLayout = true;

    /**
     * Cache of the children views for a forecast list item.
     */
    public static class ViewHolder {
        public final ImageView iconView;
        public final TextView dateView;
        public final TextView descriptionView;
        public final TextView highTempView;
        public final TextView lowTempView;

        public ViewHolder(View view) {
            iconView = (ImageView) view.findViewById(R.id.list_item_icon);
            dateView = (TextView) view.findViewById(R.id.list_item_date_textview);
            descriptionView = (TextView) view.findViewById(R.id.list_item_forecast_textview);
            highTempView = (TextView) view.findViewById(R.id.list_item_high_textview);
            lowTempView = (TextView) view.findViewById(R.id.list_item_low_textview);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return (position == 0 && mUseTodayLayout) ? VIEW_TYPE_TODAY : VIEW_TYPE_FUTURE_DAY;
    }

    public void setUseTodayLayout(boolean useTodayLayout) {
        mUseTodayLayout = useTodayLayout;
    }

    @Override
    public int getViewTypeCount() {
        return VIEW_TYPE_COUNT;
    }



    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Choose the layout type
        int viewType = getItemViewType(cursor.getPosition());

        int layoutId;

        if (viewType == VIEW_TYPE_TODAY) {
            layoutId = R.layout.list_item_forecast_today;
        }
        else {
            layoutId = R.layout.list_item_forecast;
        }


        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        return view;
    }

    /*
        This is where we fill-in the views with the contents of the cursor.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // our view is pretty simple here --- just a text view
        // we'll keep the UI functional with a simple (and slow!) binding.
        ViewHolder viewHolder = (ViewHolder) view.getTag();

        int viewType = getItemViewType(cursor.getPosition());
        // Read weather icon ID from cursor
        int weatherId = cursor.getInt(ForecastFragment.COL_WEATHER_CONDITION_ID);

        if (viewType == VIEW_TYPE_TODAY) {
            // Art image for today
            viewHolder.iconView.setImageResource(Utility.getArtResourceForWeatherCondition(weatherId));
        }
        else {
            // Icon image for other days
            viewHolder.iconView.setImageResource(Utility.getIconResourceForWeatherCondition(weatherId));
        }

        // Description
        String description = Utility.getStringForWeatherCondition(context, weatherId);
        viewHolder.descriptionView.setText(description);
        viewHolder.descriptionView.setContentDescription(context.getString(R.string.a11y_forecast, description));
        viewHolder.iconView.setContentDescription(description);

        // Nicely formatted date
        viewHolder.dateView.setText(Utility.getFriendlyDayString(context, cursor.getLong(ForecastFragment.COL_WEATHER_DATE)));

        // Metric or Imperial boolean value
        boolean isMetric = Utility.isMetric(mContext);

        // High temp +
        double maxTemperature = cursor.getDouble(ForecastFragment.COL_WEATHER_MAX_TEMP);
        viewHolder.highTempView.setText(Utility.formatTemperature(context, maxTemperature, isMetric));
        viewHolder.highTempView.setContentDescription(viewHolder.highTempView.getText());

        double minTemperature = cursor.getDouble(ForecastFragment.COL_WEATHER_MIN_TEMP);
        viewHolder.lowTempView.setText(Utility.formatTemperature(context, minTemperature, isMetric));
        viewHolder.lowTempView.setContentDescription(viewHolder.lowTempView.getText());

    }
}