package com.example.myapplication;
import com.anychart.core.cartesian.series.Line;
import com.anychart.data.Mapping;
import com.anychart.data.Set;
import com.anychart.enums.Anchor;
import com.anychart.enums.MarkerType;
import com.example.myapplication.R;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
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
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class DashBoard extends AppCompatActivity {
    private RecyclerView recyclerView;
    private List<JsonObjectModel> dataList = new ArrayList<>();

    private final List<MinMaxAngValues> minMaxAngValues_arr = new ArrayList<>();
    private static final String[] GROUP_KEYS = {"temp", "press", "wind", "rain", "solar", "hum"};
    private Iterator<String> keys;

    private UnitConverter unitConverter = new UnitConverter();
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dash_board);



        Boolean basic =  SharedPreferencesManager.getBasicBackgroudFromSharedPreferences(this);
        if (basic) {
            DrawerLayout drawerLayout = findViewById(R.id.drawerLayout_);
            drawerLayout.setBackgroundColor(Color.parseColor("#FFFFFF"));
        }



        recyclerView = findViewById(R.id.recyclerView);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getDataFromApiTest();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                getDataForToday();
                getDataFromApi();
            }
        }, 1000);
        getDataForToday();

        // Inicializace RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Vytvoření a nastavení adaptéru
        JsonObjectAdapter adapter = new JsonObjectAdapter(dataList);
        recyclerView.setAdapter(adapter);

        // Inicializace ItemTouchHelper a připojení k RecyclerView
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                for (MinMaxAngValues m : minMaxAngValues_arr) {
                    Log.e("Key", m.getStringValue());
                }
            }
        }, 1000);

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
        }
        return super.onOptionsItemSelected(item);
    }


    private void OpenSetting(){
        Intent intent = new Intent(this, Settings.class);
        startActivity(intent);
        finish();
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

            // Načtení uložených jednotek z SharedPreferences
            SharedPreferences sharedPreferences = getSharedPreferences("unit_settings", MODE_PRIVATE);

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
                        // Získání uložené jednotky z SharedPreferences
                        String savedUnit = "";
                        for (String groupKey : GROUP_KEYS) {
                            if (key.contains(groupKey)) {
                                System.out.println("VALUE: " + groupKey);
                                savedUnit = sharedPreferences.getString("unit___" + groupKey.toLowerCase(), "");
                                System.out.println("Saved unit: " + savedUnit);
                                if (key.contains("hum"))
                                {
                                    savedUnit = "%";
                                }
                                if (key.contains("angle"))
                                {
                                    savedUnit = "°";
                                }
                                if (key.contains("UV"))
                                {
                                    savedUnit = "index";
                                }
                            }
                        }
                        // Převod hodnoty na uloženou jednotku
                        double value = apiResponse.optDouble(key, 0.0);
                        double convertedValue = unitConverter.convertValueToSavedUnit(value, unit, savedUnit);
                        // Zaokrouhlení hodnoty na celé číslo

                        DecimalFormat decimalFormat = new DecimalFormat("#.##");
                        double roundedValue = Math.round(convertedValue * 10.0) / 10.0;
                        String formattedValue = decimalFormat.format(roundedValue);

                        //long roundedValue = Math.round(convertedValue);
                        // Vytvoření nového objektu JsonObjectModel a přidání do seznamu
                        for (int i = 0; i < minMaxAngValues_arr.size(); i++) {
                            if (minMaxAngValues_arr.get(i).getStringValue().equals(key)) {
                                /*
                                if (apiResponse.length() > 0) {
                                    dataList.add(new JsonObjectModel(czechKey + " (" + savedUnit + ")", savedUnit, key, "min: " + savedUnit, "max: " + savedUnit, "avg: " + savedUnit, savedUnit, unit));
                                }

                                 */

                                dataList.add(new JsonObjectModel(i + "", czechKey + " (" + savedUnit + ")", formattedValue + " " + savedUnit, key, "min: " + decimalFormat.format(unitConverter.convertValueToSavedUnit(minMaxAngValues_arr.get(i).getMinValue(), unit, savedUnit)) + " " + savedUnit, "max: " + decimalFormat.format(unitConverter.convertValueToSavedUnit(minMaxAngValues_arr.get(i).getMaxValue(), unit, savedUnit)) + " " + savedUnit, "avg: " + decimalFormat.format(unitConverter.convertValueToSavedUnit(minMaxAngValues_arr.get(i).getAvgValue(), unit, savedUnit)) + " " + savedUnit, savedUnit, unit));

                            } else {
                                Log.e("Error", "Key not found");
                            }
                        }
                    }
                }
            }
            //SharedPreferencesManager.saveOrderToSharedPreferences(DashBoard.this, dataList);
            SharedPreferencesManager.loadOrderFromSharedPreferences(DashBoard.this, dataList);
            // Inicializace RecyclerView
            recyclerView.setLayoutManager(new LinearLayoutManager(this));

            // Vytvoření a nastavení adaptéru
            JsonObjectAdapter adapter = new JsonObjectAdapter(dataList);
            recyclerView.setAdapter(adapter);
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }


    // Metoda pro převod hodnoty na uloženou jednotku




    private void processJsonResponseTest(String jsonResponse) {
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
            keys = apiResponse.keys();
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }


    private void getDataForToday() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                // Získání uložené IP adresy z SharedPreferences
                String ipAddress = SharedPreferencesManager.getIpAddressFromSharedPreferences(DashBoard.this);
                String apiUrl = "http://" + ipAddress + ":5000/api/data/aggregated/today";

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
                            processJsonResponseMinMaxAngValues(jsonResponse);
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
    private void processJsonResponseMinMaxAngValues(String jsonResponse) {
        try {
            JSONArray jsonArray = new JSONArray(jsonResponse);

            // Inicializace ArrayListu pro uchování dat pro graf
            double sum = 0;
            double min = Double.MAX_VALUE;
            double max = Double.MIN_VALUE;
                while (keys.hasNext()) {
                    String key = keys.next();
                    if (!key.equals("id") && !key.equals("time") && !key.equals("battery_bat") && !key.equals("battery_wh65")) {
                        double average = 0;
                        sum = 0;
                        min = Double.MAX_VALUE;
                        max = 0;
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            // Získání hodnoty z JSON objektu
                            double value = jsonObject.getDouble(key);

                            //Log.d("JsonObjectModel : ", jsonObject + " ");

                            // Zaokrouhlení hodnoty na jedno desetinné místo
                            float roundedValue = (Math.round(value * 10.0f) / 10.0f);
                            // Přidání nového bodu do seznamu bodů pro graf
                            // Aktualizace sumy pro výpočet průměru
                            sum += value;

                            // Aktualizace minimální a maximální hodnoty
                            if (value < min) {
                                min = value;
                            }
                            if (value > max) {
                                max = value;
                            }
                        }

                        average = sum / jsonArray.length();

                        DecimalFormat df = new DecimalFormat("#.##");
                        df.setDecimalSeparatorAlwaysShown(false);
                        df.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.US));
                        average = Double.parseDouble(df.format(average));

                        MinMaxAngValues m = new MinMaxAngValues(key, min, max, average);
                        minMaxAngValues_arr.add(m);
                        Log.d("TEST : ", m.getStringValue());
                    }
                }
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error processing JSON response", Toast.LENGTH_SHORT).show();
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

    private void getDataFromApiTest() {
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
                            processJsonResponseTest(jsonResponse);
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
            SharedPreferencesManager.saveOrderToSharedPreferences(DashBoard.this, dataList);
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
