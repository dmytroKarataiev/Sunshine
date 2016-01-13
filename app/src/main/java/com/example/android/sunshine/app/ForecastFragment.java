package com.example.android.sunshine.app;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.TextView;

import com.example.android.sunshine.app.data.WeatherContract;
import com.example.android.sunshine.app.sync.SunshineSyncAdapter;

/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, SharedPreferences.OnSharedPreferenceChangeListener {

    // These indices are tied to FORECAST_COLUMNS.  If FORECAST_COLUMNS changes, these
    // must change.
    static final int COL_WEATHER_ID = 0;
    static final int COL_WEATHER_DATE = 1;
    static final int COL_WEATHER_DESC = 2;
    static final int COL_WEATHER_MAX_TEMP = 3;
    static final int COL_WEATHER_MIN_TEMP = 4;
    static final int COL_LOCATION_SETTING = 5;
    static final int COL_WEATHER_CONDITION_ID = 6;
    static final int COL_COORD_LAT = 7;
    static final int COL_COORD_LONG = 8;

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

    private final String LOG_TAG = ForecastFragment.class.getSimpleName();

    private ForecastAdapter forecastAdapter;
    private int FORECAST_LOADER = 0;
    private boolean mUseTodayLayout, mAutoSelectView;

    private int mChoiceMode;

    private int mPosition = RecyclerView.NO_POSITION;
    private String SELECTED_POSITION = "position";
    private RecyclerView recyclerView;

    // Show weather on first launch of the app
    private int MSG_UPDATE = 1;

    // Handler was used in a first version of the app to load detail view on start in tablet mode,
    // called from onLoadFinished
//    private android.os.Handler handler = new android.os.Handler() {
//        @Override
//        public void handleMessage(Message msg) {
//            if (msg.what == MSG_UPDATE) {
//                if (forecastAdapter.getCursor() != null) {
//                    Cursor cursor = forecastAdapter.getCursor();
//
//                    if (cursor.getCount() > 0) {
//                        cursor.moveToFirst();
//                        String locationSetting = Utility.getPreferredLocation(getActivity());
//                        ((ForecastFragment.Callback) getActivity()).onLoaded(WeatherContract.WeatherEntry.buildWeatherLocationWithDate(locationSetting, cursor.getLong(COL_WEATHER_DATE)));
//                    }
//                }
//
//            }
//        }
//    };

    public ForecastFragment() {
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.pref_location_status_key))) {
            updateEmptyView();
        }
    }

    public void onLocationChanged() {
        //updateWeather();
        getLoaderManager().restartLoader(FORECAST_LOADER, null, this);
    }

    @Override
    public void onInflate(Activity activity, AttributeSet attrs, Bundle savedInstanceState) {
        super.onInflate(activity, attrs, savedInstanceState);
        TypedArray a = activity.obtainStyledAttributes(attrs, R.styleable.ForecastFragment,
                0, 0);
        mChoiceMode = a.getInt(R.styleable.ForecastFragment_android_choiceMode, AbsListView.CHOICE_MODE_NONE);
        mAutoSelectView = a.getBoolean(R.styleable.ForecastFragment_autoSelectView, false);
        a.recycle();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Add this line in order for this fragment to handle menu events.
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.forecast_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_refresh) {

            updateWeather();

            return true;
        }

        if (id == R.id.action_settings) {
            startActivity(new Intent(getActivity(), SettingsActivity.class));
            return true;
        }


        if (id == R.id.map_location) {
            openPreferredLocation();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Main layout for the view
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        // Reference to the RecyclerView
        recyclerView = (RecyclerView) rootView.findViewById(R.id.listview_forecast);

        // Layout manager which sets the "design" of the View
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(recyclerView.getContext());
        recyclerView.setLayoutManager(linearLayoutManager);

        View emptyView = rootView.findViewById(R.id.listview_forecast_empty);

        recyclerView.setHasFixedSize(true);

        // Adapter to populate RecyclerView
        forecastAdapter = new ForecastAdapter(getActivity(), new ForecastAdapter.ForecastAdapterOnClickHandler() {
            @Override
            public void onClick(Long date, ForecastAdapter.ViewHolder vh) {
                String locationSetting = Utility.getPreferredLocation(getActivity());
                ((Callback) getActivity()).onItemSelected(WeatherContract.WeatherEntry.buildWeatherLocationWithDate(locationSetting, date)
                );
                mPosition = vh.getAdapterPosition();
            }
        }, emptyView, mChoiceMode);

        recyclerView.setAdapter(forecastAdapter);

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(SELECTED_POSITION)) {
                mPosition = savedInstanceState.getInt(SELECTED_POSITION);
            }
            forecastAdapter.onRestoreInstanceState(savedInstanceState);
        }

        forecastAdapter.setUseTodayLayout(mUseTodayLayout);

        final View parallaxView = rootView.findViewById(R.id.parallax_bar);
        if (parallaxView != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
                    @Override
                    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                        super.onScrolled(recyclerView, dx, dy);
                        int max = parallaxView.getHeight();
                        if (dy > 0) {
                            parallaxView.setTranslationY(Math.max(-max, parallaxView.getTranslationY() - dy / 2));
                        } else {
                            parallaxView.setTranslationY(Math.min(0, parallaxView.getTranslationY() - dy / 2));
                        }

                    }
                });
            }
        }


        return rootView;
    }

    /**
     * Method to update the weather
     */
    public void updateWeather() {
        // Sync immediately after calling
        SunshineSyncAdapter.syncImmediately(getActivity());
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(FORECAST_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String locationSetting = Utility.getPreferredLocation(getActivity());
        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";

        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(locationSetting, System.currentTimeMillis());

        return new CursorLoader(getActivity(), weatherForLocationUri, FORECAST_COLUMNS, null, null, sortOrder);
    }

    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Swap the new cursor in.  (The framework will take care of closing the
        // old cursor once we return.)
        forecastAdapter.swapCursor(data);

        if (mPosition != RecyclerView.NO_POSITION) {
            recyclerView.smoothScrollToPosition(mPosition);
        }

        //handler.sendEmptyMessage(MSG_UPDATE);

        updateEmptyView();

        if ( data.getCount() > 0 ) {
            recyclerView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    // Since we know we're going to get items, we keep the listener around until
                    // we see Children.
                    if (recyclerView.getChildCount() > 0) {
                        recyclerView.getViewTreeObserver().removeOnPreDrawListener(this);
                        int itemPosition = forecastAdapter.getSelectedItemPosition();
                        if ( RecyclerView.NO_POSITION == itemPosition ) itemPosition = 0;
                        RecyclerView.ViewHolder vh = recyclerView.findViewHolderForAdapterPosition(itemPosition);
                        if ( null != vh && mAutoSelectView ) {
                            forecastAdapter.selectView( vh );
                        }
                        return true;
                    }
                    return false;
                }
            });
        }
    }

    public void onLoaderReset(Loader<Cursor> loader) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
        forecastAdapter.swapCursor(null);
    }

    @Override
    public void onSaveInstanceState(Bundle outstate) {

        if (mPosition != RecyclerView.NO_POSITION) {
            outstate.putInt(SELECTED_POSITION, mPosition);
        }
        forecastAdapter.onSaveInstanceState(outstate);
        super.onSaveInstanceState(outstate);

    }

    /**
     * Method to check if layout is two pane
     *
     * @param useTodayLayout true or false depending on the device
     */
    public void setUseTodayLayout(boolean useTodayLayout) {
        mUseTodayLayout = useTodayLayout;
        if (forecastAdapter != null) {
            forecastAdapter.setUseTodayLayout(mUseTodayLayout);
        }
    }

    private void openPreferredLocation() {

        if (forecastAdapter != null) {
            Cursor cursor = forecastAdapter.getCursor();
            if (cursor != null) {
                cursor.moveToPosition(0);
                String posLat = cursor.getString(COL_COORD_LAT);
                String posLong = cursor.getString(COL_COORD_LONG);
                Uri geoLocation = Uri.parse("geo:" + posLat + "," + posLong);

                // starts geo intent
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(geoLocation);

                // if there is a way to show the map - starts activity
                if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                    startActivity(intent);
                } else {
                    Log.d(LOG_TAG, "Couldn't open the map, no receiving apps installed!");
                }
            }
        }
    }

    /**
     * If adapter has no data - update Empty view, based on Internet connectivity
     */
    private void updateEmptyView() {

        if (forecastAdapter.getItemCount() == 0) {
            TextView empty = (TextView) getView().findViewById(R.id.listview_forecast_empty);

            if (null != empty) {
                int message = R.string.empty_forecast_list;
                @SunshineSyncAdapter.LocationStatus int location = Utility.getLocationStatus(getActivity());

                switch (location) {
                    case SunshineSyncAdapter.LOCATION_STATUS_SERVER_DOWN:
                        message = R.string.empty_forecast_list_server_down;
                        break;
                    case SunshineSyncAdapter.LOCATION_STATUS_SERVER_INVALID:
                        message = R.string.empty_forecast_list_server_error;
                        break;
                    case SunshineSyncAdapter.LOCATION_STATUS_INVALID:
                        message = R.string.empty_forecast_list_invalid_location;
                        break;
                    default:
                        if (!Utility.isConnected(getActivity())) {
                            message = R.string.empty_forecast_list_no_network;
                        }
                        break;
                }
                empty.setText(message);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        prefs.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        prefs.unregisterOnSharedPreferenceChangeListener(this);
    }

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        public void onItemSelected(Uri dateUri);

        public void onLoaded(Uri dateUri);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (recyclerView != null) {
            recyclerView.clearOnScrollListeners();
        }
    }
}


