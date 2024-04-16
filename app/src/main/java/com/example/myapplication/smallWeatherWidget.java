package com.example.myapplication;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.RemoteViews;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link smallWeatherWidgetConfigureActivity smallWeatherWidgetConfigureActivity}
 */
public class smallWeatherWidget extends AppWidgetProvider {
    private static final String[] GROUP_KEYS = {"temp", "press", "wind", "rain", "solar", "hum"};
    public static String value_pub;
    public static String date_pub;
    public static String savedUnit;
    public static String selectedKey_;
    private static final UnitConverter unitConverter = new UnitConverter();

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId, String selectedKey, String value, String date) {
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.small_weather_widget);

        String username = SharedPreferencesManager.getUsernameFromSharedPreferences(context);
        String password = SharedPreferencesManager.getDecodedPasswordFromSharedPreferences(context);
        sendLoginRequest(context, username, password);


        boolean basicbackground = SharedPreferencesManager.getBasicBackgroudFromSharedPreferences(context);
        if (basicbackground)
        {
            views.setInt(R.id.widget_container_main, "setBackgroundResource", R.drawable.rounder_color_white_80);
            views.setInt(R.id.widget_container,  "setBackgroundResource", R.drawable.rounded_color_white);
        }
        else
        {
            views.setInt(R.id.widget_container_main, "setBackgroundResource", R.drawable.rounded_corners);
            views.setInt(R.id.widget_container, "setBackgroundResource", R.drawable.rounded_color_blue);
        }

        boolean isDarkMode = (context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
        if (isDarkMode)
        {
            views.setInt(R.id.widget_container_main, "setBackgroundResource",R.drawable.rounded_corner_black);
            views.setInt(R.id.widget_container, "setBackgroundResource", R.drawable.rounded_corner_grey);
            views.setTextColor(R.id.keyTextView, context.getResources().getColor(R.color.white));
            views.setTextColor(R.id.valueTextView, context.getResources().getColor(R.color.white));

        }
        else
        {
            views.setTextColor(R.id.keyTextView, context.getResources().getColor(R.color.black));
            views.setTextColor(R.id.valueTextView, context.getResources().getColor(R.color.black));
        }

        String translatedValue = "";
        String savedUnit_original = "";
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
            translatedValue = translations.getString(selectedKey); // Nahraďte "KEY" za skutečný klíč

        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

        try {
            // Načtení obsahu translate.json souboru pro názvy klíčů
            InputStream translateStream = context.getResources().openRawResource(R.raw.units);
            BufferedReader reader = new BufferedReader(new InputStreamReader(translateStream));
            StringBuilder translateJsonString = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                translateJsonString.append(line);
            }
            reader.close();

            // Vytvoření JSONObject z načteného JSON řetězce
            JSONObject unit = new JSONObject(translateJsonString.toString());

            // Zde můžete provést další manipulace s načtenými překlady (translations)
            // Například získání konkrétního překladu pomocí klíče:
            savedUnit_original = unit.getString(selectedKey); // Nahraďte "KEY" za skutečný klíč

        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

        views.setTextViewText(R.id.keyTextView, translatedValue);

        selectedKey_ = selectedKey;

        SharedPreferences sharedPreferences = context.getSharedPreferences("unit_settings", Context.MODE_PRIVATE);
        //String savedUnit = "";
        for (String groupKey : GROUP_KEYS) {
            if (selectedKey_.contains(groupKey)) {
                //System.out.println("VALUE: " + groupKey);
                savedUnit = sharedPreferences.getString("unit___" + groupKey.toLowerCase(), "");
                //System.out.println("Saved unit: " + savedUnit);
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

        Intent intent = new Intent(context, Weather_layout.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        intent.putExtra("weather", translatedValue);
        intent.putExtra("jsonObject", selectedKey);
        intent.putExtra("unit", savedUnit);
        intent.putExtra("convert_unit", savedUnit);
        intent.putExtra("original_unit", savedUnit_original);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, appWidgetId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.keyTextView, pendingIntent);

        views.setTextViewText(R.id.valueTextView, Math.round(unitConverter.convertValueToSavedUnit(Double.parseDouble(value), savedUnit_original, savedUnit)) + " " + savedUnit);
        views.setTextViewText(R.id.valuedateTextView, convertToClassicTime(date));
        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }


    public void onUpdate_small(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            String selectedKey = smallWeatherWidgetConfigureActivity.loadSelectedKeyPref(context, appWidgetId);
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.small_weather_widget);
            Intent updateIntent = new Intent(context, WeatherWidget.class);
            updateIntent.setAction("com.example.myapplication.UPDATE_WIDGET");
            updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            updateIntent.putExtra("SELECTED_KEY_small", selectedKey);
            updateIntent.putExtra("unit", savedUnit);

            PendingIntent updatePendingIntent = PendingIntent.getBroadcast(context, appWidgetId, updateIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            views.setOnClickPendingIntent(R.id.refreshButton_, updatePendingIntent);
            getDataFromApiByKey(context, selectedKey, appWidgetManager, appWidgetId);
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }
    public static String convertToClassicTime(String inputDate) {
        String outputDate = "";
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH);
            inputFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
            Date date = inputFormat.parse(inputDate);

            SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
            outputFormat.setTimeZone(TimeZone.getTimeZone("GMT")); // Nastavíme časovou zónu na GMT
            outputDate = outputFormat.format(date);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return outputDate;
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        // When the user deletes the widget, delete the preference associated with it.
        for (int appWidgetId : appWidgetIds) {
            smallWeatherWidgetConfigureActivity.deleteSelectedKeyPref(context, appWidgetId);
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
            //isButtonClicked = false;
            int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
            String selectedKey = intent.getStringExtra("SELECTED_KEY_small");
            if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID && selectedKey != null) {
                // Aktualizovat widget s vybraným klíčem
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                onUpdate_small(context, appWidgetManager, new int[]{appWidgetId});
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
            value_pub = value;
            date_pub = apiResponse.getString("time");

            // Aktualizace widgetu s novou hodnotou
            updateAppWidget(context, appWidgetManager, appWidgetId, selectedKey, value, date_pub);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private static void sendLoginRequest(final Context context, final String username, final String password) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                // Získání uložené IP adresy z SharedPreferences
                String ipAddress = SharedPreferencesManager.getIpAddressFromSharedPreferences(context);
                String apiUrl = "http://" + ipAddress + ":5000/api/login";

                // Vytvoření JSON objektu s přihlašovacími údaji
                JSONObject requestData = new JSONObject();
                try {
                    requestData.put("username", username);
                    requestData.put("password", password);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                // Odeslání požadavku na server
                try {
                    URL url = new URL(apiUrl);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-Type", "application/json");
                    connection.setDoOutput(true);

                    // Zapsání dat do výstupního proudu
                    DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
                    outputStream.writeBytes(requestData.toString());
                    outputStream.flush();
                    outputStream.close();

                    // Zpracování odpovědi
                    int responseCode = connection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        //System.out.println("Login successful!");
                        // Pokud je odpověď 200, zpracujeme access_token a uložíme ho do SharedPreferences
                        InputStream inputStream = connection.getInputStream();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }
                        reader.close();

                        // Zpracování odpovědi
                        JSONObject jsonResponse = new JSONObject(response.toString());
                        String accessToken = jsonResponse.getString("access_token");
                        SharedPreferencesManager.saveAccessTokenToSharedPreferences(context, accessToken);
                        SharedPreferencesManager.saveUsernameToSharedPreferences(context, username);
                        SharedPreferencesManager.saveEncodedPasswordToSharedPreferences(context, password);
                    }

                    // Uzavření spojení
                    connection.disconnect();
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}