package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class Units_settings extends AppCompatActivity {
    private ListView listView;
    private List<UnitItem> unitItemList = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_units_settings);

        listView = findViewById(R.id.listviewunits);

        loadUnitsFromJson();
        UnitsAdapter adapter = new UnitsAdapter(this, R.layout.item_unit, unitItemList);
        listView.setAdapter(adapter);
    }
    private void loadUnitsFromJson() {
        try {
            StringBuilder stringBuilder = new StringBuilder();
            InputStream translateStream = getResources().openRawResource(R.raw.units);
            BufferedReader reader = new BufferedReader(new InputStreamReader(translateStream));
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
            reader.close();

            JSONObject jsonObject = new JSONObject(stringBuilder.toString());
            Set<String> addedUnits = new HashSet<>();
            Iterator<String> keys = jsonObject.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                String value = jsonObject.getString(key);
                boolean switchState = false;
                if (!value.isEmpty() && !addedUnits.contains(value)) {
                    unitItemList.add(new UnitItem(key, value, switchState));
                    addedUnits.add(value);
                }
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }
}
