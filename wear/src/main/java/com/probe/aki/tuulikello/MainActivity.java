package com.probe.aki.tuulikello;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.wearable.activity.WearableActivity;

import com.google.android.gms.wearable.DataClient;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;

public class MainActivity extends WearableActivity implements DataClient.OnDataChangedListener{

    private static final String OBSERVATION_KEY = "com.probe.aki.tuulikello.observationstation";
    private static final String FORECAST_KEY = "com.probe.aki.tuulikello.forecaststation";
    private static final String STATIONS_PATH = "/stations";

    private SimpleWindSpeedView windscreens;
    private SharedPreferences sharedPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        windscreens = (SimpleWindSpeedView)findViewById(R.id.windspeedview);
        windscreens.loadNewValues(sharedPrefs.getString("prefUserObservationStation", null),
                sharedPrefs.getString("prefUserForecastStation",null));

        // Enables Always-on
        setAmbientEnabled();
    }

    @Override
    public void onUpdateAmbient() {
        super.onUpdateAmbient();
        windscreens.loadNewValues(sharedPrefs.getString("prefUserObservationStation", null),
                sharedPrefs.getString("prefUserForecastStation",null));
    }

    @Override
    protected void onResume() {
        super.onResume();
        Wearable.getDataClient(this).addListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Wearable.getDataClient(this).removeListener(this);
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        for (DataEvent event : dataEvents) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                // DataItem changed
                DataItem item = event.getDataItem();
                if (item.getUri().getPath().compareTo(STATIONS_PATH) == 0) {
                    DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
                    SharedPreferences.Editor editor = sharedPrefs.edit();
                    editor.putString("prefUserObservationStation", dataMap.getString(OBSERVATION_KEY));
                    editor.putString("prefUserForecastStation", dataMap.getString(FORECAST_KEY));
                    editor.commit();
                    windscreens.loadNewValuesForce(dataMap.getString(OBSERVATION_KEY),dataMap.getString(FORECAST_KEY));
                }
            } else if (event.getType() == DataEvent.TYPE_DELETED) {
                // DataItem deleted
            }
        }
    }

}
