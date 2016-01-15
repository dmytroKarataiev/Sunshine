package com.example.android.sunshine.app.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.example.android.sunshine.app.sync.SunshineSyncAdapter;

/**
 * Class for the weather widget, delegates data fetching to the {@link TodayWidgetIntentService} to ensure
 * that data retrieval is done on a background thread
 * Created by karataev on 1/14/16.
 */
public class TodayWidgetProvider extends AppWidgetProvider {

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        context.startService(new Intent(context, TodayWidgetIntentService.class));
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        context.startService(new Intent(context, TodayWidgetIntentService.class));
    }

    @Override
    public void onReceive(@NonNull Context context, @NonNull Intent intent) {
        if (SunshineSyncAdapter.ACTION_DATA_UPDATE.equals(intent.getAction())) {
            context.startService(new Intent(context, TodayWidgetIntentService.class));
        }
        super.onReceive(context, intent);
    }



}
