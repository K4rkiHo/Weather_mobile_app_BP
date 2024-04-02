package com.example.myapplication;

import static android.content.Context.MODE_PRIVATE;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link WeatherWidgetConfigureActivity WeatherWidgetConfigureActivity}
 */
public class WeatherWidget extends AppWidgetProvider {
    private static final String[] GROUP_KEYS = {"temp", "press", "wind", "rain", "solar", "hum"};

    public static String selectedKey_;
    public static String savedUnit;

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId, String selectedKey, String value) {
        // Získání RemoteViews pro widget
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.weather_widget);
        // Nastavení textu na základě vybraného klíče a hodnoty
        views.setTextViewText(R.id.keyTextView, selectedKey);
        views.setTextViewText(R.id.valueTextView, value);

        selectedKey_ = selectedKey;

        SharedPreferences sharedPreferences = context.getSharedPreferences("unit_settings", Context.MODE_PRIVATE);
        savedUnit = "";
        for (String groupKey : GROUP_KEYS) {
            if (selectedKey.contains(groupKey)) {
                System.out.println("VALUE: " + groupKey);
                savedUnit = sharedPreferences.getString("unit___" + groupKey.toLowerCase(), "");
                System.out.println("Saved unit: " + savedUnit);
                if (selectedKey.contains("hum"))
                {
                    savedUnit = "%";
                }
                if (selectedKey.contains("angle"))
                {
                    savedUnit = "°";
                }
                if (selectedKey.contains("UV"))
                {
                    savedUnit = "index";
                }
            }
        }



        // Vytvoření Intentu pro spuštění po kliknutí na text ve widgetu

        Intent intent = new Intent(context, Weather_layout.class);
        intent.putExtra("weather", selectedKey);
        intent.putExtra("jsonObject", selectedKey);
        intent.putExtra("unit", savedUnit);
        intent.putExtra("convert_unit", savedUnit);
        intent.putExtra("original_unit", savedUnit);

// Přidání identifikátoru widgetu jako extra do Intentu
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, appWidgetId, intent, PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.keyTextView, pendingIntent);

        views.setTextViewText(R.id.valueTextView, value + " " + savedUnit);

        System.out.println("VALUE: " + value);
        System.out.println("savedUnit: " + savedUnit);
        System.out.println("selectedKey: " + selectedKey);
        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    public void onTextClick(View view) {
        // Spustit novou aktivitu
        Context context = view.getContext();
        Intent intent = new Intent(context, Weather_layout.class);
        intent.putExtra("weather", selectedKey_);
        intent.putExtra("jsonObject", selectedKey_);
        intent.putExtra("unit", savedUnit);
        intent.putExtra("convert_unit", savedUnit);
        intent.putExtra("original_unit", savedUnit);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // Spustit aktualizaci pro každý widget ID v seznamu appWidgetIds
        for (int appWidgetId : appWidgetIds) {
            // Načíst vybraný klíč pro daný widget ID
            String selectedKey = WeatherWidgetConfigureActivity.loadSelectedKeyPref(context, appWidgetId);


            getDataFromApiByKey(context, selectedKey, appWidgetManager, appWidgetId);
        }

        // Nastavit opakující se aktualizaci pomocí AlarmManageru
        setWidgetUpdateAlarm(context);
    }

    private void setWidgetUpdateAlarm(Context context) {
        // Nastavit Intent pro spuštění Broadcast receiveru
        Intent alarmIntent = new Intent(context, WeatherWidget.class);
        alarmIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Nastavit opakující se aktualizaci každých 10 minut
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        long intervalMillis = 10 * 60 * 1000; // 10 minut v milisekundách
        alarmManager.setInexactRepeating(AlarmManager.RTC, System.currentTimeMillis(), intervalMillis, pendingIntent);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        // When the user deletes the widget, delete the preference associated with it.
        for (int appWidgetId : appWidgetIds) {
            WeatherWidgetConfigureActivity.deleteSelectedKeyPref(context, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (intent.getAction() != null && intent.getAction().equals("com.example.myapplication.UPDATE_WIDGET")) {
            int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
            String selectedKey = intent.getStringExtra("SELECTED_KEY");
            if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID && selectedKey != null) {
                // Aktualizovat widget s vybraným klíčem
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                onUpdate(context, appWidgetManager, new int[]{appWidgetId});
            }
        }
    }


    private void getDataFromApiByKey(final Context context, final String selectedKey,
                                     final AppWidgetManager appWidgetManager, final int appWidgetId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                // Získání uložené IP adresy z SharedPreferences
                String ipAddress = SharedPreferencesManager.getIpAddressFromSharedPreferences(context);
                String apiUrl = "http://" + ipAddress + ":5000/api/data/last_data";

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
                            processJsonResponseByKey(context, jsonResponse, selectedKey, appWidgetManager, appWidgetId);
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

    private void processJsonResponseByKey(Context context, String jsonResponse,
                                          String selectedKey, AppWidgetManager appWidgetManager, int appWidgetId) {
        try {
            // Create JSON object from the response
            JSONObject apiResponse = new JSONObject(jsonResponse);

            // Získání hodnoty na základě vybraného klíče
            String value = apiResponse.getString(selectedKey);

            // Aktualizace widgetu s novou hodnotou
            updateAppWidget(context, appWidgetManager, appWidgetId, selectedKey, value);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}