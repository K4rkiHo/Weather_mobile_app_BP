package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

public class Settings extends AppCompatActivity {
    private Switch stayLoginSwitch;
    private TextView name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);


        name = findViewById(R.id.account);
        name.setText(SharedPreferencesManager.getUsernameFromSharedPreferences(this));

        stayLoginSwitch = findViewById(R.id.stay_login_switch);

        // Nastavení aktuálního stavu "Stay login" na základě uložené hodnoty
        stayLoginSwitch.setChecked(SharedPreferencesManager.getStayLoginFromSharedPreferences(this));

        // Přidání posluchače pro změnu hodnoty spínače
        stayLoginSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Uložení nového stavu "Stay login" do SharedPreferences po změně hodnoty spínače
                SharedPreferencesManager.saveStayLoginToSharedPreferences(Settings.this, isChecked);
            }
        });

    }

    public void onBackArrowClicked(View view) {
        Intent intent = new Intent(this, Units_settings.class);
        startActivity(intent);
    }

    public void onBackArrowClicked_API(View view) {
        Intent intent = new Intent(this, API_server.class);
        startActivity(intent);
    }
}