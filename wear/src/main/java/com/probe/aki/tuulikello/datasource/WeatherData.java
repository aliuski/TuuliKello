package com.probe.aki.tuulikello.datasource;

import java.io.Serializable;
import java.util.Date;

public class WeatherData implements Serializable {
    protected Date step[] = null;
    protected double windspeed[] = null;
    protected int winddirection[] = null;
    protected Date updated;

    public WeatherData() {
    }

    public Date[] getStep() {
        return step;
    }

    public double[] getWindspeed() {
        return windspeed;
    }

    public int[] getWinddirection() {
        return winddirection;
    }

    public boolean isUpdated(int minute) {
        Date d = new java.util.Date();
        if (updated == null || (d.getTime() > updated.getTime() + minute * 60000)) {
            return false;
        }
        return true;
    }
}
