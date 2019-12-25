package com.probe.aki.tuulikello;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wearable.DataClient;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private static final String OBSERVATION_KEY = "com.probe.aki.tuulikello.observationstation";
    private static final String FORECAST_KEY = "com.probe.aki.tuulikello.forecaststation";
    private static final String STATIONS_PATH = "/stations";
    private DataClient dataClient;

    private SharedPreferences.OnSharedPreferenceChangeListener prefListener;
    private SharedPreferences sharedPrefs;
    private String prefUserObservationStation;

    private Spinner tableSpinner1;
    private Spinner tableSpinner2;
    private Spinner tableSpinner3;
    private Spinner tableSpinner4;
    private Spinner forecatSpinner;

    private ArrayAdapter<String> adapter1;
    private ArrayAdapter<String> adapter2;
    private ArrayAdapter<String> adapter3;
    private ArrayAdapter<String> adapter4;
    private ArrayAdapter<String> adapterf;
    private ArrayList<String> spinnerArray1;
    private ArrayList<String> spinnerArray;
    private ArrayList<String> forecatArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dataClient = Wearable.getDataClient(this);

        tableSpinner1 = (Spinner) findViewById(R.id.tableSpinner1);
        tableSpinner2 = (Spinner) findViewById(R.id.tableSpinner2);
        tableSpinner3 = (Spinner) findViewById(R.id.tableSpinner3);
        tableSpinner4 = (Spinner) findViewById(R.id.tableSpinner4);
        forecatSpinner = (Spinner) findViewById(R.id.forecatSpinner);

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                createMenu();
            }
        };
        prefs.registerOnSharedPreferenceChangeListener(prefListener);

        createMenu();

        Button send = (Button) findViewById(R.id.sendButton);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(prefUserObservationStation == null)
                    return;
                String item1 = (String)tableSpinner1.getSelectedItem();
                String item2 = (String)tableSpinner2.getSelectedItem();
                String item3 = (String)tableSpinner3.getSelectedItem();
                String item4 = (String)tableSpinner4.getSelectedItem();

                if((!item2.isEmpty() && (item2.equals(item1) || item2.equals(item3))) ||
                    (!item3.isEmpty() && (item3.equals(item1) || item3.equals(item4))) ||
                    (!item4.isEmpty() && (item4.equals(item1) || item4.equals(item2)))) {
                        return;
                }

                String exportStation = "";
                String selected = findStation(item1);
                if(selected != null)
                    exportStation = selected;
                selected = findStation(item2);
                if(selected != null) {
                    if(!exportStation.isEmpty())
                        exportStation += "\n";
                    exportStation += selected;
                }
                selected = findStation(item3);
                if(selected != null) {
                    if(!exportStation.isEmpty())
                        exportStation += "\n";
                    exportStation += selected;
                }
                selected = findStation(item4);
                if(selected != null) {
                    if(!exportStation.isEmpty())
                        exportStation += "\n";
                    exportStation += selected;
                }

                String forecast = (String)forecatSpinner.getSelectedItem();
                SharedPreferences.Editor editor = sharedPrefs.edit();
                editor.putString("selectedStation", item1+";"+item2+";"+item3+";"+item4);
                editor.putString("selectedForecastStation", forecast);
                editor.commit();

                if(!exportStation.isEmpty() && !forecast.isEmpty())
                    sendStations(exportStation,forecast);
            }
        });
    }

    private void createMenu(){
        prefUserObservationStation = sharedPrefs.getString("prefUserObservationStation", null);
        String prefUserForecastStation = sharedPrefs.getString("prefUserForecastStation", null);
        String selectedStation = sharedPrefs.getString("selectedStation", null);
        String selectedForecastStation = sharedPrefs.getString("selectedForecastStation", null);

        spinnerArray1 =  new ArrayList<String>();
        spinnerArray =  new ArrayList<String>();
        spinnerArray.add("");
        if(prefUserObservationStation != null) {
            String[] row = prefUserObservationStation.split("\n");
            for (int i = 0; i < row.length; i++) {
                String column[] = row[i].split(";");
                spinnerArray1.add(column[0]);
                spinnerArray.add(column[0]);
            }
        }

        adapter1 = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item, spinnerArray1);
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        tableSpinner1.setAdapter(adapter1);

        adapter2 = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item, spinnerArray);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        tableSpinner2.setAdapter(adapter2);

        adapter3 = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item, spinnerArray);
        adapter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        tableSpinner3.setAdapter(adapter3);

        adapter4 = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item, spinnerArray);
        adapter4.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        tableSpinner4.setAdapter(adapter4);

        if(selectedStation != null) {
            String spinnerSelection[] = selectedStation.split(";",-1);
            int pos = getIndex(spinnerSelection[0]);
            if(pos != -1)
                tableSpinner1.setSelection(pos);
            pos = getIndex(spinnerSelection[1]);
            if(pos != -1)
                tableSpinner2.setSelection(pos + 1);
            pos = getIndex(spinnerSelection[2]);
            if(pos != -1)
                tableSpinner3.setSelection(pos + 1);
            pos = getIndex(spinnerSelection[3]);
            if(pos != -1)
                tableSpinner4.setSelection(pos + 1);
        }

        forecatArray =  new ArrayList<String>();
        if(prefUserForecastStation != null) {
            String[] row = prefUserForecastStation.split("\n");
            for (int i = 0; i < row.length; i++)
                forecatArray.add(row[i]);
        }

        adapterf = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item, forecatArray);
        adapterf.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        forecatSpinner.setAdapter(adapterf);

        if(selectedForecastStation != null) {
            String[] row = prefUserForecastStation.split("\n");
            for (int i = 0; i < row.length; i++) {
                if (selectedForecastStation.equals(row[i]))
                    forecatSpinner.setSelection(i);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent intent = new Intent();
            intent.setClass(MainActivity.this, SetPreferenceActivity.class);
            startActivityForResult(intent, 0);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private String findStation(String stationname){
        String[] row = prefUserObservationStation.split("\n");
        for (int i = 0; i < row.length; i++) {
            String column[] = row[i].split(";",-1);
            if(stationname.equals(column[0]))
                return column[1]+";"+column[2]+";"+column[3];
        }
        return null;
    }

    private int getIndex(String stationname){
        String[] row = prefUserObservationStation.split("\n");
        for (int i = 0; i < row.length; i++) {
            String column[] = row[i].split(";");
            if(stationname.equals(column[0]))
                return i;
        }
        return -1;
    }

    private void sendStations(String observation, String forecast) {
        PutDataMapRequest putDataMapReq = PutDataMapRequest.create(STATIONS_PATH);
        putDataMapReq.getDataMap().putString(OBSERVATION_KEY,observation);
        putDataMapReq.getDataMap().putString(FORECAST_KEY,forecast);
        PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
        putDataReq.setUrgent();
        Task<DataItem> putDataTask = dataClient.putDataItem(putDataReq);
        putDataTask.addOnSuccessListener(
                new OnSuccessListener<DataItem>() {
                    @Override
                    public void onSuccess(DataItem dataItem) {
                        Toast.makeText(MainActivity.this, R.string.send_complete, Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
