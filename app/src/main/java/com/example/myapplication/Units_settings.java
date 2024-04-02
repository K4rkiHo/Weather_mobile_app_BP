package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class Units_settings extends AppCompatActivity {

    private SharedPreferences sharedPreferences;
    private static final String[] GROUP_KEYS = {"temp", "press", "wind", "rain", "solar", "hum"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_units_settings);

        // Inicializace SharedPreferences
        sharedPreferences = getSharedPreferences("unit_settings", MODE_PRIVATE);

        // Načtení uloženého stavu radio tlačítek a nastavení jejich stavu
        loadRadioButtonsState();

        // Nastavení posluchačů pro změnu stavu radio tlačítek
        RadioGroup.OnCheckedChangeListener radioGroupListener = new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                saveRadioButtonsState();
                handleUnitConversion(group, checkedId);
            }
        };

        // Přiřazení posluchačů k radio tlačítkům
        for (String groupKey : GROUP_KEYS) {
            RadioGroup radioGroup = findViewById(getResources().getIdentifier("radioGroup_" + groupKey.toLowerCase(), "id", getPackageName()));
            radioGroup.setOnCheckedChangeListener(radioGroupListener);
        }

        // Testování výběru tlačítek a jejich jednotek
        //testSelectedUnits();
    }

    // Uloží stav vybraných radio tlačítek do SharedPreferences
    private void saveRadioButtonsState() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        for (String groupKey : GROUP_KEYS) {
            int checkedId = ((RadioGroup) findViewById(getResources().getIdentifier("radioGroup_" + groupKey.toLowerCase(), "id", getPackageName()))).getCheckedRadioButtonId();
            editor.putInt("radioBtn" + groupKey, checkedId);
        }
        editor.apply();
    }

    // Načte uložený stav radio tlačítek a nastaví jejich stav
    private void loadRadioButtonsState() {
        for (String groupKey : GROUP_KEYS) {
            int checkedId = sharedPreferences.getInt("radioBtn" + groupKey, getResources().getIdentifier("radioBtn1_" + groupKey.toLowerCase(), "id", getPackageName()));
            ((RadioGroup) findViewById(getResources().getIdentifier("radioGroup_" + groupKey.toLowerCase(), "id", getPackageName()))).check(checkedId);
        }
    }

    // Uloží vybrané jednotky do SharedPreferences
    private void handleUnitConversion(RadioGroup group, int checkedId) {
        RadioButton checkedRadioButton = findViewById(checkedId);
        String selectedUnit = checkedRadioButton.getText().toString();
        String groupKey = getGroupKey(group.getId());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("unit__" + groupKey.toLowerCase(), selectedUnit);
        editor.apply();

        System.out.println("Selected " + groupKey + " unit: " + selectedUnit);
    }

    // Získá klíč skupiny podle ID radio tlačítka
    private String getGroupKey(int radioGroupId) {
        String idString = getResources().getResourceEntryName(radioGroupId);
        return idString.substring(10); // Oříznutí prefixu "radioGroup_"
    }

    // Testování výběru tlačítek a jejich jednotek
    private void testSelectedUnits() {
        for (String groupKey : GROUP_KEYS) {
            int checkedId = sharedPreferences.getInt("radioBtn" + groupKey, getResources().getIdentifier("radioBtn1_" + groupKey.toLowerCase(), "id", getPackageName()));
            RadioButton radioButton = findViewById(checkedId);
            String selectedUnit = sharedPreferences.getString("unit__" + groupKey.toLowerCase(), "");
            String selectedButton = radioButton.getText().toString();
            System.out.println("Selected " + groupKey + " button: " + selectedButton);
            System.out.println("Selected " + groupKey + " unit: " + selectedUnit);
        }
    }
}
