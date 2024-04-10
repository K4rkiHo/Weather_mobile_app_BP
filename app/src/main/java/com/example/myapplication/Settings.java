package com.example.myapplication;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;

public class Settings extends AppCompatActivity {
    private Switch stayLoginSwitch;
    ImageView imageView;
    FloatingActionButton floatingActionButton;
    private Switch pictureSwitch;
    private TextView name;
    private String imagePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        //Objects.requireNonNull(getSupportActionBar()).setBackgroundDrawable(getDrawable(R.drawable.ic_account));

        imageView = findViewById(R.id.imageview_acc);

        name = findViewById(R.id.account);
        name.setText(SharedPreferencesManager.getUsernameFromSharedPreferences(this));

        stayLoginSwitch = findViewById(R.id.stay_login_switch);
        pictureSwitch = findViewById(R.id.picture_layout_switch);

        // Nastavení aktuálního stavu "Stay login" na základě uložené hodnoty
        stayLoginSwitch.setChecked(SharedPreferencesManager.getStayLoginFromSharedPreferences(this));
        pictureSwitch.setChecked(SharedPreferencesManager.getBasicBackgroudFromSharedPreferences(this));

        // Přidání posluchače pro změnu hodnoty spínače
        stayLoginSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Uložení nového stavu "Stay login" do SharedPreferences po změně hodnoty spínače
                SharedPreferencesManager.saveStayLoginToSharedPreferences(Settings.this, isChecked);
            }
        });

        pictureSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Uložení nového stavu "Stay login" do SharedPreferences po změně hodnoty spínače
                SharedPreferencesManager.saveBasicBackgroudToSharedPreferences(Settings.this, isChecked);
            }
        });

    }
    @Override
    public void onBackPressed() {
        // Zavolání aktivity DashBoard při stisku tlačítka "zpět"
        super.onBackPressed();
        Intent intent = new Intent(this, DashBoard.class);
        Bundle bundle = ActivityOptions.makeSceneTransitionAnimation(this).toBundle();
        startActivity(intent, bundle);

        // Ukončení aktuální aktivity nastavení
        finish();
    }

    public void onBackArrowClicked(View view) {
        Intent intent = new Intent(this, Units_settings.class);
        startActivity(intent);
    }

    public void onBackArrowClicked_API(View view) {
        Intent intent = new Intent(this, API_server.class);
        startActivity(intent);
    }

    public void onBackArrowClicked_About(View view) {
        Intent intent = new Intent(this, About_app.class);
        startActivity(intent);
    }
}