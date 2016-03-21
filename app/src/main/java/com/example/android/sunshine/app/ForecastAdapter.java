package com.example.android.sunshine.app;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.android.sunshine.app.data.WeatherContract;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * {@link ForecastAdapter} exposes a list of weather forecasts
 * from a {@link android.database.Cursor} to a {@link android.widget.ListView}.
 */
public class ForecastAdapter extends RecyclerView.Adapter<ForecastAdapter.ViewHolder> {

    public ForecastAdapter(Context context, ForecastAdapterOnClickHandler dh, View emptyView, int choiceMode) {
        mContext = context;
        mClickHandler = dh;
        mEptyView = emptyView;
        mICM = new ItemChoiceManager(this);
        mICM.setChoiceMode(choiceMode);
    }

    private Context mContext;
    private Cursor mCursor;
    private ForecastAdapterOnClickHandler mClickHandler;
    private View mEptyView;

    // ItemChoiceManager to save position on rotation
    private ItemChoiceManager mICM;

    private final String LOG_TAG = ForecastAdapter.class.getSimpleName();

    private static final int VIEW_TYPE_TODAY = 0;
    private static final int VIEW_TYPE_FUTURE_DAY = 1;
    private static final int VIEW_TYPE_COUNT = 2;
    private boolean mUseTodayLayout = true;
    boolean useLongToday;

    private static final String[] FORECAST_COLUMNS = {
            // In this case the id needs to be fully qualified with a table name, since
            // the content provider joins the location & weather tables in the background
            // (both have an _id column)
            // On the one hand, that's annoying.  On the other, you can search the weather table
            // using the location set by the user, which is only in the Location table.
            // So the convenience is worth it.
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.LocationEntry.COLUMN_COORD_LAT,
            WeatherContract.LocationEntry.COLUMN_COORD_LONG
    };

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (parent instanceof RecyclerView) {
            int layoutId = -1;
            switch (viewType) {
                case VIEW_TYPE_TODAY:
                    layoutId = R.layout.list_item_forecast_today;
                    useLongToday = true;
                    break;
                case VIEW_TYPE_FUTURE_DAY:
                    layoutId = R.layout.list_item_forecast;
                    useLongToday = false;
                    break;
            }

            View view = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
            view.setFocusable(true);
            return new ViewHolder(view);
        } else {
            throw new RuntimeException("Not bound to RecyclerView");
        }

    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {

        // our view is pretty simple here --- just a text view
        // we'll keep the UI functional with a simple (and slow!) binding.
        mCursor.moveToPosition(position);

        // Read weather icon ID from cursor
        int weatherId = mCursor.getInt(ForecastFragment.COL_WEATHER_CONDITION_ID);
        int defaultImage;

        switch (getItemViewType(position)) {
            case VIEW_TYPE_TODAY:
                // Art image for today
                defaultImage = Utility.getArtResourceForWeatherCondition(weatherId);
                break;
            default:
                // Icon image for other days
                defaultImage = Utility.getIconResourceForWeatherCondition(weatherId);
                break;
        }

        Glide.with(mContext)
                .load(Utility.getArtUrlForWeatherCondition(mContext, weatherId))
                .error(defaultImage)
                .crossFade()
                .into(viewHolder.iconView);

        // better animation even after rotation of a device to find a view
        ViewCompat.setTransitionName(viewHolder.iconView, "iconView" + position);

        // Description
        String description = Utility.getStringForWeatherCondition(mContext, weatherId);
        viewHolder.descriptionView.setText(description);
        viewHolder.descriptionView.setContentDescription(mContext.getString(R.string.a11y_forecast, description));
        viewHolder.iconView.setContentDescription(description);

        // Nicely formatted date
        viewHolder.dateView.setText(Utility.getFriendlyDayString(mContext, mCursor.getLong(ForecastFragment.COL_WEATHER_DATE), useLongToday));

        // Metric or Imperial boolean value
        boolean isMetric = Utility.isMetric(mContext);

        // High temp +
        double maxTemperature = mCursor.getDouble(ForecastFragment.COL_WEATHER_MAX_TEMP);
        viewHolder.highTempView.setText(Utility.formatTemperature(mContext, maxTemperature, isMetric));
        viewHolder.highTempView.setContentDescription(viewHolder.highTempView.getText());

        double minTemperature = mCursor.getDouble(ForecastFragment.COL_WEATHER_MIN_TEMP);
        viewHolder.lowTempView.setText(Utility.formatTemperature(mContext, minTemperature, isMetric));
        viewHolder.lowTempView.setContentDescription(viewHolder.lowTempView.getText());

        mICM.onBindViewHolder(viewHolder, position);
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        mICM.onRestoreInstanceState(savedInstanceState);
    }

    public void onSaveInstanceState(Bundle outState) {
        mICM.onSaveInstanceState(outState);
    }

    public int getSelectedItemPosition() {
        return mICM.getSelectedItemPosition();
    }

    public void selectView(RecyclerView.ViewHolder viewHolder) {
        if ( viewHolder instanceof ViewHolder ) {
            ViewHolder vfh = (ViewHolder)viewHolder;
            vfh.onClick(vfh.itemView);
        }
    }

    @Override
    public int getItemCount() {

        if (mCursor == null) {
            return 0;
        } else {
            return mCursor.getCount();
        }
    }

    /**
     * Cache of the children views for a forecast list item.
     */
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @Bind(R.id.list_item_icon) ImageView iconView;
        @Bind(R.id.list_item_date_textview) TextView dateView;
        @Bind(R.id.list_item_forecast_textview) TextView descriptionView;
        @Bind(R.id.list_item_high_textview) TextView highTempView;
        @Bind(R.id.list_item_low_textview) TextView lowTempView;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            mCursor.moveToPosition(adapterPosition);
            int dateColumnIndex = mCursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_DATE);
            mClickHandler.onClick(mCursor.getLong(dateColumnIndex), this);
            mICM.onClick(this);
        }
    }

    public static interface ForecastAdapterOnClickHandler {
        void onClick(Long date, ViewHolder vh);
    }

    @Override
    public int getItemViewType(int position) {
        return (position == 0 && mUseTodayLayout) ? VIEW_TYPE_TODAY : VIEW_TYPE_FUTURE_DAY;
    }

    public void setUseTodayLayout(boolean useTodayLayout) {
        mUseTodayLayout = useTodayLayout;
    }

    public Cursor getCursor() {
        return mCursor;
    }

    public void swapCursor(Cursor newCursor) {
        mCursor = newCursor;
        notifyDataSetChanged();
        mEptyView.setVisibility(getItemCount() == 0 ? View.VISIBLE : View.GONE);
    }


}