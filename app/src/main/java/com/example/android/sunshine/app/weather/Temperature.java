/*
 * MIT License
 *
 * Copyright (c) 2016. Dmytro Karataiev
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package com.example.android.sunshine.app.weather;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by karataev on 3/22/16.
 */
public class Temperature {

    @SerializedName("day")
    @Expose
    private Double day;
    @SerializedName("min")
    @Expose
    private Double min;
    @SerializedName("max")
    @Expose
    private Double max;
    @SerializedName("night")
    @Expose
    private Double night;
    @SerializedName("eve")
    @Expose
    private Double eve;
    @SerializedName("morn")
    @Expose
    private Double morn;

    /**
     *
     * @return
     * The day
     */
    public Double getDay() {
        return day;
    }

    /**
     *
     * @param day
     * The day
     */
    public void setDay(Double day) {
        this.day = day;
    }

    /**
     *
     * @return
     * The min
     */
    public Double getMin() {
        return min;
    }

    /**
     *
     * @param min
     * The min
     */
    public void setMin(Double min) {
        this.min = min;
    }

    /**
     *
     * @return
     * The max
     */
    public Double getMax() {
        return max;
    }

    /**
     *
     * @param max
     * The max
     */
    public void setMax(Double max) {
        this.max = max;
    }

    /**
     *
     * @return
     * The night
     */
    public Double getNight() {
        return night;
    }

    /**
     *
     * @param night
     * The night
     */
    public void setNight(Double night) {
        this.night = night;
    }

    /**
     *
     * @return
     * The eve
     */
    public Double getEve() {
        return eve;
    }

    /**
     *
     * @param eve
     * The eve
     */
    public void setEve(Double eve) {
        this.eve = eve;
    }

    /**
     *
     * @return
     * The morn
     */
    public Double getMorn() {
        return morn;
    }

    /**
     *
     * @param morn
     * The morn
     */
    public void setMorn(Double morn) {
        this.morn = morn;
    }

}