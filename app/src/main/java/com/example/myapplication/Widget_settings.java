package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

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
import java.util.List;

public class Widget_settings extends Activity {
    private Spinner mAppWidgetSpinner;
    private Button mAddButton;
    private int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    ArrayList<String> keysList = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_widget_settings);
        // Initialize views
        mAppWidgetSpinner = findViewById(R.id.appwidget_spinner);
        mAddButton = findViewById(R.id.add_button);

        // Populate spinner with data
        List<String> options = new ArrayList<>();
        // Add your options here...
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, options);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mAppWidgetSpinner.setAdapter(adapter);

        // Get the widget ID from the intent
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        // Check if valid widget ID was received
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {

        }

        getDataFromApi(getApplicationContext());

        // Set OnClickListener for the "Set weather data" button
        mAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the selected value from the spinner
                String selectedValue = (String) mAppWidgetSpinner.getSelectedItem();

                // Save the selected value to SharedPreferences
                saveSelectedValuePref(Widget_settings.this, mAppWidgetId, selectedValue);

                // Return the result to the calling activity
                Intent resultValue = new Intent();
                resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
                setResult(RESULT_OK, resultValue);
                finish();
            }
        });
    }

    // Method to save the selected value to SharedPreferences
    private void saveSelectedValuePref(Context context, int appWidgetId, String selectedValue) {
        SharedPreferences.Editor prefs = context.getSharedPreferences("com.example.myapplication.WeatherWidget", Context.MODE_PRIVATE).edit();
        prefs.putString("selected_value_" + appWidgetId, selectedValue);
        prefs.apply();
    }

    // Method to load the selected value from SharedPreferences
    public static String loadSelectedValuePref(Context context, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences("com.example.myapplication.WeatherWidget", Context.MODE_PRIVATE);
        return prefs.getString("selected_value_" + appWidgetId, null);
    }

    // Method to delete the selected value from SharedPreferences
    public static void deleteSelectedValuePref(Context context, int appWidgetId) {
        SharedPreferences.Editor prefs = context.getSharedPreferences("com.example.myapplication.WeatherWidget", Context.MODE_PRIVATE).edit();
        prefs.remove("selected_value_" + appWidgetId);
        prefs.apply();
    }

    private void getDataFromApi(final Context context) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                // Získání uložené IP adresy z SharedPreferences
                String ipAddress = SharedPreferencesManager.getIpAddressFromSharedPreferences(context);
                String apiUrl = "http://" + ipAddress + ":5000/api/columns";

                // Odeslání požadavku na server
                try {
                    URL url = new URL(apiUrl);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");

                    // Přidání "Bearer token" do hlavičky požadavku
                    String accessToken = SharedPreferencesManager.getAccessTokenFromSharedPreferences(context);
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
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
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

    private void processJsonResponse(String jsonResponse) {
        try {
            // Create JSON object from the response
            JSONObject apiResponse = new JSONObject(jsonResponse);

            // Získání pole klíčů z JSON objektu
            JSONArray keysArray = apiResponse.getJSONArray("rows");

            // Naplnění listu klíčů
            keysList.clear();

            String translatedValue = "";
            try {
                // Načtení obsahu translate.json souboru pro názvy klíčů
                InputStream translateStream = getResources().openRawResource(R.raw.translate);
                BufferedReader reader = new BufferedReader(new InputStreamReader(translateStream));
                StringBuilder translateJsonString = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    translateJsonString.append(line);
                }
                reader.close();

                // Vytvoření JSONObject z načteného JSON řetězce
                JSONObject translations = new JSONObject(translateJsonString.toString());

                // Zde můžete provést další manipulace s načtenými překlady (translations)
                // Například získání konkrétního překladu pomocí klíče:

                for (int i = 0; i < keysArray.length(); i++) {
                    String key = keysArray.getString(i);
                    translatedValue = translations.getString(key);
                    if (!key.equals("id") && !key.equals("time") && !key.equals("battery_bat") && !key.equals("battery_wh65")) {
                        keysList.add(translatedValue);
                    }
                }

            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }

            // Populate spinner with keys
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, keysList);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            mAppWidgetSpinner.setAdapter(adapter);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


}