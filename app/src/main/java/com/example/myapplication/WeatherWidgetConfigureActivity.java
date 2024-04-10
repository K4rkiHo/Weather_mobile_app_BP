package com.example.myapplication;

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

import com.example.myapplication.databinding.WeatherWidgetConfigureBinding;

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
import java.util.Iterator;

/**
 * The configuration screen for the {@link WeatherWidget WeatherWidget} AppWidget.
 */
public class WeatherWidgetConfigureActivity extends Activity {

    private static final String PREFS_NAME = "com.example.myapplication.WeatherWidget";
    private static final String PREF_PREFIX_KEY = "appwidget_";
    private final Context context = WeatherWidgetConfigureActivity.this;
    int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    Button mAddButton;
    Spinner mAppWidgetSpinner;
    ArrayList<String> keysList = new ArrayList<>();
    View.OnClickListener mOnClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            String selectedValue = (String) mAppWidgetSpinner.getSelectedItem(); // Získání vybrané hodnoty z Spinneru
            String selectedKey = ""; // Inicializace proměnné pro uložení klíče

            try {
                // Načtení obsahu translate.json souboru pro překlady
                InputStream translateStream = context.getResources().openRawResource(R.raw.translate);
                BufferedReader reader = new BufferedReader(new InputStreamReader(translateStream));
                StringBuilder translateJsonString = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    translateJsonString.append(line);
                }
                reader.close();

                // Vytvoření JSONObject z načteného JSON řetězce
                JSONObject translations = new JSONObject(translateJsonString.toString());

                // Procházení překladů a hledání klíče, který odpovídá vybrané hodnotě
                Iterator<String> keys = translations.keys();
                while (keys.hasNext()) {
                    String key = keys.next();
                    String value = translations.getString(key);
                    if (value.equals(selectedValue)) {
                        selectedKey = key; // Nalezení klíče odpovídající vybrané hodnotě
                        break;
                    }
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }

            // Ostatní části vašeho onClickListeneru zůstávají nezměněné
            saveSelectedKeyPref(context, mAppWidgetId, selectedKey);
            //getDataFromApiByKey(context, selectedKey);

            Intent intent = new Intent(context, WeatherWidget.class);
            intent.setAction("com.example.myapplication.UPDATE_WIDGET");
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
            intent.putExtra("SELECTED_KEY", selectedKey);
            sendBroadcast(intent);

            Intent resultValue = new Intent();
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
            setResult(RESULT_OK, resultValue);
            finish();
        }
    };

    private WeatherWidgetConfigureBinding binding;

    public WeatherWidgetConfigureActivity() {
        super();
    }

    static void saveSelectedKeyPref(Context context, int appWidgetId, String selectedKey) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();
        prefs.putString(PREF_PREFIX_KEY + appWidgetId, selectedKey);
        prefs.apply();
    }

    static String loadSelectedKeyPref(Context context, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(PREF_PREFIX_KEY + appWidgetId, null);
    }

    static void deleteSelectedKeyPref(Context context, int appWidgetId) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();
        prefs.remove(PREF_PREFIX_KEY + appWidgetId);
        prefs.apply();
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.weather_widget_configure);

        mAppWidgetSpinner = findViewById(R.id.appwidget_spinner);
        mAddButton = findViewById(R.id.add_button);
        mAddButton.setOnClickListener(mOnClickListener);

        // Find the widget id from the intent.
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        // If this activity was started with an intent without an app widget ID, finish with an error.
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
            return;
        }

        // Get data from API and populate spinner
        getDataFromApi(getApplicationContext());
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
                InputStream translateStream = context.getResources().openRawResource(R.raw.translate);
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
