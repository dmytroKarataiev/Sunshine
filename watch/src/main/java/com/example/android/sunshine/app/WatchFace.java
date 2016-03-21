/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.sunshine.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.text.format.Time;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.wearable.DataMap;
import com.patloew.rxwear.RxWear;
import com.patloew.rxwear.transformers.MessageEventGetDataMap;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import rx.functions.Action1;

/**
 * Digital watch face with seconds. In ambient mode, the seconds aren't displayed. On devices with
 * low-bit ambient mode, the text is drawn without anti-aliasing in ambient mode.
 */
public class WatchFace extends CanvasWatchFaceService {

    /**
     * Update rate in milliseconds for interactive mode. We update once a second since seconds are
     * displayed in interactive mode.
     */
    private static final long INTERACTIVE_UPDATE_RATE_MS = TimeUnit.SECONDS.toMillis(1);

    /**
     * Handler message id for updating the time periodically in interactive mode.
     */
    private static final int MSG_UPDATE_TIME = 0;

    @Override
    public Engine onCreateEngine() {
        RxWear.init(this);
        return new Engine();
    }

    private static class EngineHandler extends Handler {
        private final WeakReference<WatchFace.Engine> mWeakReference;

        public EngineHandler(WatchFace.Engine reference) {
            mWeakReference = new WeakReference<>(reference);
        }

        @Override
        public void handleMessage(Message msg) {
            WatchFace.Engine engine = mWeakReference.get();
            if (engine != null) {
                switch (msg.what) {
                    case MSG_UPDATE_TIME:
                        engine.handleUpdateTimeMessage();
                        break;
                }
            }
        }
    }

    private class Engine extends CanvasWatchFaceService.Engine {

        final int BACKGROUND_COLOR_AMBIENT = Color.BLACK;
        final int BACKGROUND_COLOR = getColor(R.color.primary);
        String mHigh_temp = "", mLow_temp = "", mDate = "", mWeatherDesc = "";
        int mWeather_id = -1;

        private int specW, specH;
        private View mLayout, face_divider;
        private TextView face_time, face_date, face_high_temp, face_low_temp, face_weather_desc;
        private ImageView face_weather_image;
        private final Point displaySize = new Point();

        final Handler mUpdateTimeHandler = new EngineHandler(this);
        boolean mRegisteredTimeZoneReceiver = false;

        boolean mAmbient;
        Time mTime;

        final BroadcastReceiver mTimeZoneReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mTime.clear(intent.getStringExtra("time-zone"));
                mTime.setToNow();
            }
        };

        float mXOffset;
        float mYOffset;

        /**
         * Whether the display supports fewer bits for each color in ambient mode. When true, we
         * disable anti-aliasing in ambient mode.
         */
        boolean mLowBitAmbient;

        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);

            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mLayout = inflater.inflate(R.layout.weather_face, null);

            Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
            display.getSize(displaySize);

            specW = View.MeasureSpec.makeMeasureSpec(displaySize.x, View.MeasureSpec.EXACTLY);
            specH = View.MeasureSpec.makeMeasureSpec(displaySize.y, View.MeasureSpec.EXACTLY);

            // Find all the views from the layout
            face_time = (TextView) mLayout.findViewById(R.id.current_time);
            face_date = (TextView) mLayout.findViewById(R.id.current_date);
            face_high_temp = (TextView) mLayout.findViewById(R.id.high_temp);
            face_low_temp = (TextView) mLayout.findViewById(R.id.low_temp);
            face_weather_image = (ImageView) mLayout.findViewById(R.id.weather_id);
            face_weather_desc = (TextView) mLayout.findViewById(R.id.weather_desc);
            face_divider = mLayout.findViewById(R.id.divider);

            setWatchFaceStyle(new WatchFaceStyle.Builder(WatchFace.this)
                    .setCardPeekMode(WatchFaceStyle.PEEK_MODE_VARIABLE)
                    .setBackgroundVisibility(WatchFaceStyle.BACKGROUND_VISIBILITY_INTERRUPTIVE)
                    .setShowSystemUiTime(false)
                    .setAcceptsTapEvents(true)
                    .build());
            Resources resources = WatchFace.this.getResources();
            mYOffset = resources.getDimension(R.dimen.digital_y_offset);

            mTime = new Time();

            // Receive info from mobile app
            RxWear.Message.listen()
                    .compose(MessageEventGetDataMap.filterByPath("/sunshine-weather-data"))
                    .subscribe(new Action1<DataMap>() {
                        @Override
                        public void call(DataMap dataMap) {
                            mHigh_temp = dataMap.getString("weather-high");
                            mLow_temp = dataMap.getString("weather-low");
                            mDate = dataMap.getString("weather-date");
                            mWeather_id = dataMap.getInt("weather-id");
                            mWeatherDesc = dataMap.getString("weather-desc");
                        }
                    });
        }

        @Override
        public void onDestroy() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            super.onDestroy();
        }


        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);

            if (visible) {
                registerReceiver();

                // Update time zone in case it changed while we weren't visible.
                mTime.clear(TimeZone.getDefault().getID());
                mTime.setToNow();
            } else {
                unregisterReceiver();
            }

            // Whether the timer should be running depends on whether we're visible (as well as
            // whether we're in ambient mode), so we may need to start or stop the timer.
            updateTimer();
        }

        private void registerReceiver() {
            if (mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = true;
            IntentFilter filter = new IntentFilter(Intent.ACTION_TIMEZONE_CHANGED);
            WatchFace.this.registerReceiver(mTimeZoneReceiver, filter);
        }

        private void unregisterReceiver() {
            if (!mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = false;
            WatchFace.this.unregisterReceiver(mTimeZoneReceiver);
        }

        @Override
        public void onApplyWindowInsets(WindowInsets insets) {
            super.onApplyWindowInsets(insets);

            // Load resources that have alternate values for round watches.
            Resources resources = WatchFace.this.getResources();
            boolean isRound = insets.isRound();
            mXOffset = resources.getDimension(isRound
                    ? R.dimen.digital_x_offset_round : R.dimen.digital_x_offset);
        }

        @Override
        public void onPropertiesChanged(Bundle properties) {
            super.onPropertiesChanged(properties);
            mLowBitAmbient = properties.getBoolean(PROPERTY_LOW_BIT_AMBIENT, false);
        }

        @Override
        public void onTimeTick() {
            super.onTimeTick();
            invalidate();
        }

        @Override
        public void onAmbientModeChanged(boolean inAmbientMode) {
            super.onAmbientModeChanged(inAmbientMode);
            if (mAmbient != inAmbientMode) {
                mAmbient = inAmbientMode;

                invalidate();
            }

            if (mAmbient) {
                face_weather_image.setVisibility(View.GONE);
                face_divider.setBackgroundColor(getResources().getColor(R.color.white));
            } else {
                face_weather_image.setVisibility(View.VISIBLE);
                face_divider.setBackgroundColor(getResources().getColor(R.color.divider));
            }

            // Whether the timer should be running depends on whether we're visible (as well as
            // whether we're in ambient mode), so we may need to start or stop the timer.
            updateTimer();

            Typeface font = Typeface.create("sans-serif-condensed",
                    inAmbientMode ? Typeface.NORMAL : Typeface.BOLD);
            ViewGroup group = (ViewGroup) mLayout;
            for (int i = group.getChildCount() - 1; i >= 0; i--) {
                if (group.getChildAt(i) instanceof TextView) {
                    ((TextView) group.getChildAt(i)).setTypeface(font);
                }
            }
        }

        /**
         * Layout-based approach to draw WatchFace
         *
         * @param canvas on which we draw
         * @param bounds not used
         */
        @Override
        public void onDraw(Canvas canvas, Rect bounds) {
            mTime.setToNow();

            String text = mAmbient
                    ? String.format("%d:%02d", mTime.hour, mTime.minute)
                    : String.format("%d:%02d:%02d", mTime.hour, mTime.minute, mTime.second);
            face_time.setText(text);

            face_high_temp.setText(mHigh_temp);
            face_low_temp.setText(mLow_temp);
            face_weather_desc.setText(mWeatherDesc);

            SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault());

            face_date.setText(shortenedDateFormat.format(mTime.toMillis(true)));
            if (mWeather_id != -1) {
                face_weather_image.setImageResource(Utility.getArtResourceForWeatherCondition(mWeather_id));
            }

            mLayout.measure(specW, specH);
            mLayout.layout(0, 0, mLayout.getMeasuredWidth(), mLayout.getMeasuredHeight());

            canvas.drawColor(Color.BLACK);

            mLayout.setBackgroundColor(mAmbient ? BACKGROUND_COLOR_AMBIENT : BACKGROUND_COLOR);

            mLayout.draw(canvas);
        }

        /**
         * Starts the {@link #mUpdateTimeHandler} timer if it should be running and isn't currently
         * or stops it if it shouldn't be running but currently is.
         */
        private void updateTimer() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            if (shouldTimerBeRunning()) {
                mUpdateTimeHandler.sendEmptyMessage(MSG_UPDATE_TIME);
            }
        }

        /**
         * Returns whether the {@link #mUpdateTimeHandler} timer should be running. The timer should
         * only run when we're visible and in interactive mode.
         */
        private boolean shouldTimerBeRunning() {
            return isVisible() && !isInAmbientMode();
        }

        /**
         * Handle updating the time periodically in interactive mode.
         */
        private void handleUpdateTimeMessage() {
            invalidate();
            if (shouldTimerBeRunning()) {
                long timeMs = System.currentTimeMillis();
                long delayMs = INTERACTIVE_UPDATE_RATE_MS
                        - (timeMs % INTERACTIVE_UPDATE_RATE_MS);
                mUpdateTimeHandler.sendEmptyMessageDelayed(MSG_UPDATE_TIME, delayMs);
            }
        }
    }
}
