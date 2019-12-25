package com.probe.aki.tuulikello;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.ArrayList;

public class ObservationSettingsPreference  extends DialogPreference{

    private static final String DEFAULT_VALUE = "";

    private String value = DEFAULT_VALUE;
    private ArrayList<String[]> dataArray;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> spinnerArray;
    private Spinner sItems;
    private EditText editName;
    private EditText editSidName;
    private boolean newEditStatus;
    private Spinner stationtypeSpinner;
    private EditText editStationPassword;


    public ObservationSettingsPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setDialogLayoutResource(R.xml.observation_settings);
        dataArray =  new ArrayList<String[]>();
        spinnerArray =  new ArrayList<String>();
        spinnerArray.add(context.getResources().getString(R.string.saved_observation_stations));
    }

    @Override
    protected void onBindDialogView(View view) {

        editName = (EditText) view.findViewById(R.id.editName);
        editSidName = (EditText) view.findViewById(R.id.editSidName);
        sItems = (Spinner) view.findViewById(R.id.tableSpinner);
        stationtypeSpinner = (Spinner) view.findViewById(R.id.stationtypeSpinner);
        editStationPassword = (EditText) view.findViewById(R.id.editStationPassword);

        adapter = new ArrayAdapter<String>(
                view.getContext(), android.R.layout.simple_spinner_item, spinnerArray);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sItems.setAdapter(adapter);
        newEditStatus = false;
        enableInputs(false);

        Button newButton = (Button) view.findViewById(R.id.newButton);
        newButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearInputField();
                enableInputs(true);
                newEditStatus = true;
            }
        });

        Button deleteButton = (Button) view.findViewById(R.id.deleteButton);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int index = (int)sItems.getSelectedItemPosition();
                if(index == 0)
                    return;
                dataArray.remove(index-1);
                spinnerArray.remove(index);
                adapter.notifyDataSetChanged();
                sItems.setSelection(0);
                clearInputField();
                enableInputs(false);
                newEditStatus = false;
            }
        });

        Button editButton = (Button) view.findViewById(R.id.editButton);
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(editName.getText().toString().isEmpty() || editName.getText().toString().indexOf(';') != -1
                        || editSidName.getText().toString().isEmpty()
                        || editStationPassword.getText().toString().indexOf(';') != -1)
                    return;
                int index = (int)sItems.getSelectedItemPosition();
                String tmp[] = new String[3];
                tmp[0] = editSidName.getText().toString();
                tmp[1] = stationtypeSpinner.getSelectedItem().equals("Fmi") ? "0" : "1";
                tmp[2] = editStationPassword.getText().toString();
                if(newEditStatus) {
                    dataArray.add(index,tmp);
                    spinnerArray.add(index+1,editName.getText().toString());
                    newEditStatus = false;
                } else {
                    if(index == 0)
                        return;
                    dataArray.set(index-1, tmp);
                    spinnerArray.set(index, editName.getText().toString());
                }
                adapter.notifyDataSetChanged();
                sItems.setSelection(0);
                clearInputField();
                enableInputs(false);
            }
        });

        sItems.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if(position == 0){
                    clearInputField();
                    enableInputs(false);
                } else {
                    String tmp[] = (String[]) dataArray.get(position-1);
                    editSidName.setText(tmp[0]);
                    if(tmp[1].equals("0")) {
                        stationtypeSpinner.setSelection(0);
                        editStationPassword.setEnabled(false);
                    } else {
                        stationtypeSpinner.setSelection(1);
                        editStationPassword.setText(tmp[2]);
                    }
                    editName.setText(spinnerArray.get(position));
                    enableInputs(true);
                }
                newEditStatus = false;
            }
            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });
        stationtypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if(position == 0){
                    editStationPassword.setEnabled(false);
                    editStationPassword.setText("");
                } else
                    editStationPassword.setEnabled(true);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });
        super.onBindDialogView(view);
    }

    private void clearInputField(){
        editName.setText("");
        editSidName.setText("");
        editStationPassword.setText("");
    }

    private void enableInputs(boolean set){
        editName.setEnabled(set);
        editSidName.setEnabled(set);
        stationtypeSpinner.setEnabled(set);
        if(!set)
            editStationPassword.setEnabled(false);
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getString(index);
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        if (restorePersistedValue) {
            value = getPersistedString(DEFAULT_VALUE);
        } else {
            value = (String)defaultValue;
            persistString(value);
        }
        if(!value.isEmpty()) {
            String[] row = value.split("\n");
            for (int i = 0; i < row.length; i++) {
                String column[] = row[i].split(";",-1);
                String tmp[] = new String[3];
                tmp[0] = column[1];
                tmp[1] = column[2];
                tmp[2] = column[3];
                dataArray.add(tmp);
                spinnerArray.add(column[0]);
            }
        }
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            value = "";
            for(int i=0;i<dataArray.size();i++){
                String tmp[] = dataArray.get(i);
                if(!value.isEmpty())
                    value += '\n';
                value += spinnerArray.get(i+1) +';'+ tmp[0] +';'+ tmp[1] +';'+ tmp[2];
            }
            persistString(value);
        }
    }
}
