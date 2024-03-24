package com.example.myapplication;
import com.example.myapplication.R;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.LineData;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.navigation.NavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class DashBoard extends AppCompatActivity {

    DrawerLayout drawerLayout;
    ActionBarDrawerToggle drawerToggle;
    NavigationView navigationView;


    private RecyclerView recyclerView;

    private List<JsonObjectModel> dataList = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dash_board);
        recyclerView = findViewById(R.id.recyclerView);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Načtení pořadí z SharedPreferences
        SharedPreferencesManager.loadOrderFromSharedPreferences(this, dataList);

        getDataFromApi();

        // Inicializace RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Vytvoření a nastavení adaptéru
        JsonObjectAdapter adapter = new JsonObjectAdapter(dataList);
        recyclerView.setAdapter(adapter);

        // Inicializace ItemTouchHelper a připojení k RecyclerView
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_items, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.action_settings) {
            OpenSetting();
            System.out.println("Settings");
        }
        return super.onOptionsItemSelected(item);
    }


    private void OpenSetting(){
        Intent intent = new Intent(this, Settings.class);
        startActivity(intent);
    }

    private void processJsonResponse(String jsonResponse) {
        try {
            // Vytvoření JSON objektu z odpovědi
            JSONObject apiResponse = new JSONObject(jsonResponse);

            // Načtení obsahu translate.json souboru pro názvy klíčů
            InputStream translateStream = getResources().openRawResource(R.raw.translate);
            BufferedReader reader = new BufferedReader(new InputStreamReader(translateStream));
            StringBuilder translateJsonString = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                translateJsonString.append(line);
            }
            reader.close();
            JSONObject translations = new JSONObject(translateJsonString.toString());

            // Načtení obsahu units.json souboru pro jednotky
            InputStream unitsStream = getResources().openRawResource(R.raw.units);
            reader = new BufferedReader(new InputStreamReader(unitsStream));
            StringBuilder unitsJsonString = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                unitsJsonString.append(line);
            }
            reader.close();
            JSONObject units = new JSONObject(unitsJsonString.toString());

            // Procházení klíčů v JSON odpovědi z API
            Iterator<String> keys = apiResponse.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                // Vyfiltruj klíče, které nechcete zahrnout
                if (!key.equals("id") && !key.equals("time") && !key.equals("battery_bat") && !key.equals("battery_wh65")) {
                    // Získání českého překladu klíče z translate.json souboru
                    if (translations.has(key)) {
                        String czechKey = translations.getString(key);
                        // Získání jednotky z units.json souboru
                        String unit = units.optString(key, "");
                        // Získání hodnoty z API odpovědi
                        double value = apiResponse.optDouble(key, 0.0);
                        // Zaokrouhlení hodnoty na celé číslo
                        long roundedValue = Math.round(value);
                        // Vytvoření nového objektu JsonObjectModel a přidání do seznamu
                        dataList.add(new JsonObjectModel(czechKey, roundedValue + " " + unit, key));
                    }
                }
            }

            // Inicializace RecyclerView
            recyclerView.setLayoutManager(new LinearLayoutManager(this));

            // Vytvoření a nastavení adaptéru
            JsonObjectAdapter adapter = new JsonObjectAdapter(dataList);
            recyclerView.setAdapter(adapter);

            SharedPreferencesManager.saveOrderToSharedPreferences(DashBoard.this, dataList);
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    private void getDataFromApi() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                // Získání uložené IP adresy z SharedPreferences
                String ipAddress = SharedPreferencesManager.getIpAddressFromSharedPreferences(DashBoard.this);
                String apiUrl = "http://" + ipAddress + ":5000/api/data/last_data";

                // Odeslání požadavku na server
                try {
                    URL url = new URL(apiUrl);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");

                    // Přidání "Bearer token" do hlavičky požadavku
                    String accessToken = SharedPreferencesManager.getAccessTokenFromSharedPreferences(DashBoard.this);
                    connection.setRequestProperty("Authorization", "Bearer " + accessToken);

                    // Přečtení odpovědi od serveru
                    InputStream inputStream = connection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    // Zpracování odpovědi
                    final String jsonResponse = response.toString();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // Zde můžete provést zpracování odpovědi od serveru
                            //textView_test.setText(jsonResponse);
                            processJsonResponse(jsonResponse);
                        }
                    });

                    // Uzavření spojení
                    connection.disconnect();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.START | ItemTouchHelper.END, 0) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            int fromPosition = viewHolder.getAdapterPosition();
            int toPosition = target.getAdapterPosition();

            Collections.swap(dataList, fromPosition, toPosition);
            recyclerView.getAdapter().notifyItemMoved(fromPosition, toPosition);

            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            int position = viewHolder.getAdapterPosition();
            Toast.makeText(DashBoard.this, "Deleted", Toast.LENGTH_SHORT).show();
        }
    };
}
