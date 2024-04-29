package com.example.myapplication;

import static android.content.Context.MODE_PRIVATE;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.RemoteViews;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link WeatherWidgetConfigureActivity WeatherWidgetConfigureActivity}
 */
public class WeatherWidget extends AppWidgetProvider {
    private static final String[] GROUP_KEYS = {"temp", "press", "wind", "rain", "solar", "hum"};
    public static String selectedKey_;
    public static String savedUnit;
    public static String value_pub;

    public static String date_pub;
    private static final UnitConverter unitConverter = new UnitConverter();
    private final List<MinMaxAngValues> minMaxAngValues_arr = new ArrayList<>();
    //private static boolean isButtonClicked = false;
    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId, String selectedKey, String value, double min, double max, double avg, String date) {
        // Získání RemoteViews pro widget
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.weather_widget);
        // Nastavení textu na základě vybraného klíče a hodnoty

        views.setTextViewText(R.id.valueTextView, value);

        boolean basicbackground = SharedPreferencesManager.getBasicBackgroudFromSharedPreferences(context);
        if (basicbackground) {
            views.setInt(R.id.widget_container_main, "setBackgroundResource", R.drawable.rounder_color_white_80);
            views.setInt(R.id.widget_container, "setBackgroundResource", R.drawable.rounded_color_white);
            views.setInt(R.id.extraInfoLayout, "setBackgroundResource", R.drawable.rounded_color_white);
            views.setTextColor(R.id.valuedateTextView, context.getResources().getColor(R.color.black));
        } else {
            views.setInt(R.id.widget_container_main, "setBackgroundResource", R.drawable.rounded_corners);
            views.setInt(R.id.widget_container, "setBackgroundResource", R.drawable.rounded_color_blue);
            views.setInt(R.id.extraInfoLayout, "setBackgroundResource", R.drawable.rounded_corner_violet);
            views.setTextColor(R.id.valuedateTextView, context.getResources().getColor(R.color.black));
        }

        boolean isDarkMode = (context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
        if (isDarkMode) {
            views.setInt(R.id.widget_container_main, "setBackgroundResource", R.drawable.rounded_corner_black);
            views.setInt(R.id.widget_container, "setBackgroundResource", R.drawable.rounded_corner_grey);
            views.setInt(R.id.extraInfoLayout, "setBackgroundResource", R.drawable.rounded_corner_grey);
            views.setTextColor(R.id.keyTextView, context.getResources().getColor(R.color.white));
            views.setTextColor(R.id.valueTextView, context.getResources().getColor(R.color.white));
            views.setTextColor(R.id.valuedateTextView, context.getResources().getColor(R.color.white));

        }
        else
        {
            views.setTextColor(R.id.keyTextView, context.getResources().getColor(R.color.black));
            views.setTextColor(R.id.valueTextView, context.getResources().getColor(R.color.black));
        }

        if (isDarkMode && !basicbackground)
        {
            views.setInt(R.id.widget_container_main, "setBackgroundResource", R.drawable.rounded_corner_black);
            views.setInt(R.id.widget_container, "setBackgroundResource", R.drawable.rounded_corner_violet);
            views.setInt(R.id.extraInfoLayout, "setBackgroundResource", R.drawable.rounded_corner_grey);
            views.setTextColor(R.id.keyTextView, context.getResources().getColor(R.color.white));
            views.setTextColor(R.id.valueTextView, context.getResources().getColor(R.color.white));
            views.setTextColor(R.id.valuedateTextView, context.getResources().getColor(R.color.white));
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
                savedUnit = sharedPreferences.getString("unit___" + groupKey.toLowerCase(), "");
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
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        intent.putExtra("weather", translatedValue);
        intent.putExtra("jsonObject", selectedKey);
        intent.putExtra("unit", savedUnit);
        intent.putExtra("convert_unit", savedUnit);
        intent.putExtra("original_unit", savedUnit_original);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, appWidgetId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.keyTextView, pendingIntent);


        PendingIntent pendingIntentvalue = PendingIntent.getActivity(context, appWidgetId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.valueTextView, pendingIntentvalue);

        double convertedValue = unitConverter.convertValueToSavedUnit(Double.parseDouble(value), savedUnit_original, savedUnit);
        double roundedValue = Math.round(convertedValue * 10.0) / 10.0;


        views.setTextViewText(R.id.valueTextView, roundedValue + " " + savedUnit);

        double convertedValueavg = unitConverter.convertValueToSavedUnit(avg, savedUnit_original, savedUnit);
        double roundedValueavg = Math.round(convertedValueavg * 10.0) / 10.0;

        double convertedValueMin = unitConverter.convertValueToSavedUnit(min, savedUnit_original, savedUnit);
        double convertedValueMax = unitConverter.convertValueToSavedUnit(max, savedUnit_original, savedUnit);

        double roundedValuemin = Math.round(convertedValueMin * 10.0) / 10.0;
        double roundedValuemax = Math.round(convertedValueMax * 10.0) / 10.0;



        views.setTextViewText(R.id.minTextView, "min: " + roundedValuemin + " " + savedUnit);
        views.setTextViewText(R.id.maxTextView, "max: " + roundedValuemax + " " + savedUnit);
        views.setTextViewText(R.id.avgTextView, "avg: " + roundedValueavg + " " + savedUnit);

        views.setTextViewText(R.id.valuedateTextView, convertToClassicTime(date));

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
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



                boolean isConnected = isConnectedToInternet(context);
                if (!isConnected) {
                    // Pokud není připojení k internetu, změňte barvu widgetu
                    updateWidgetColorForNoInternet(context, appWidgetId);
                } else {
                    String username = SharedPreferencesManager.getUsernameFromSharedPreferences(context);
                    String password = SharedPreferencesManager.getDecodedPasswordFromSharedPreferences(context);
                    sendLoginRequest(context, username, password);

                    // Přidání OnClickListener pro tlačítko refreshButton
                    RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.weather_widget);
                    Intent updateIntent = new Intent(context, WeatherWidget.class);
                    updateIntent.setAction("com.example.myapplication.UPDATE_WIDGET");
                    updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
                    updateIntent.putExtra("SELECTED_KEY", selectedKey);
                    updateIntent.putExtra("unit", savedUnit);
                    PendingIntent updatePendingIntent = PendingIntent.getBroadcast(context, appWidgetId, updateIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                    views.setOnClickPendingIntent(R.id.refreshButton, updatePendingIntent);

                    // Aktualizace layoutu widgetu
                    //appWidgetManager.updateAppWidget(appWidgetId, views);

                    // Získání dat pro zobrazení
                    getDataFromApiByKey(context, selectedKey, appWidgetManager, appWidgetId);
                    getDataFromApiByKeyAVG(context, selectedKey, appWidgetManager, appWidgetId);

                    //setWidgetUpdateAlarm(context);

                    appWidgetManager.updateAppWidget(appWidgetId, views);
                }
        }
    }

    // Funkce pro kontrolu připojení k internetu
    private boolean isConnectedToInternet(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    // Funkce pro aktualizaci barvy widgetu, pokud není připojení k internetu
    private void updateWidgetColorForNoInternet(Context context, int appWidgetId) {
        // Získání RemoteViews pro widget
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.weather_widget);

        // Změna barvy pozadí na červenou
        views.setInt(R.id.widget_container_main, "setBackgroundResource",R.drawable.rounded_corner_black);
        views.setInt(R.id.widget_container, "setBackgroundResource", R.drawable.rounded_corner_grey);
        views.setInt(R.id.extraInfoLayout, "setBackgroundResource",R.drawable.rounded_corner_grey);
        views.setTextColor(R.id.keyTextView, context.getResources().getColor(R.color.white));
        views.setTextColor(R.id.valueTextView, context.getResources().getColor(R.color.white));
        views.setTextViewText(R.id.valueTextView, "No data available");
        views.setTextViewText(R.id.keyTextView, "Connection lost");
        views.setTextViewText(R.id.minTextView, "");
        views.setTextViewText(R.id.maxTextView, "");
        views.setTextViewText(R.id.avgTextView, "");
        views.setTextViewText(R.id.valuedateTextView, "");

        // Instrukce pro aktualizaci widgetu
        AppWidgetManager.getInstance(context).updateAppWidget(appWidgetId, views);
    }

    private void setWidgetUpdateAlarm(Context context) {
        // Nastavit Intent pro spuštění Broadcast receiveru
        Intent alarmIntent = new Intent(context, WeatherWidget.class);
        alarmIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, PendingIntent.FLAG_MUTABLE);

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
            value_pub = value;
            double min = 0;
            double max = 0;
            double avg = 0;
            date_pub = apiResponse.getString("time");

            // Aktualizace widgetu s novou hodnotou
            updateAppWidget(context, appWidgetManager, appWidgetId, selectedKey, value, min, max, avg, date_pub);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void getDataFromApiByKeyAVG(final Context context, final String selectedKey,
                                     final AppWidgetManager appWidgetManager, final int appWidgetId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                // Získání uložené IP adresy z SharedPreferences
                String ipAddress = SharedPreferencesManager.getIpAddressFromSharedPreferences(context);
                String apiUrl = "http://" + ipAddress + ":5000/api/data/meteostation/today";

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
                            processJsonResponseByKeyAVG(context, jsonResponse, selectedKey, appWidgetManager, appWidgetId, value_pub);
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

    private void processJsonResponseByKeyAVG(Context context, String jsonResponse,
                                          String selectedKey, AppWidgetManager appWidgetManager, int appWidgetId, String value_) {
        try {
            // Create JSON object from the response
            JSONArray jsonArray = new JSONArray(jsonResponse);

            if (jsonArray.length() != 0) {
                double sum = 0;
                double min = Double.MAX_VALUE;
                double max = 0;
                double avg = 0;

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    double value = jsonObject.getDouble(selectedKey);
                    sum += value;
                    if (value < min) {
                        min = value;
                    }
                    if (value > max) {
                        max = value;
                    }
                }

                avg = sum / jsonArray.length();
                DecimalFormat df = new DecimalFormat("#.##");
                df.setDecimalSeparatorAlwaysShown(false);
                df.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.US));
                avg = Double.parseDouble(df.format(avg));

                // Aktualizace widgetu s novou hodnotou
                updateAppWidget(context, appWidgetManager, appWidgetId, selectedKey, value_, min, max, avg, date_pub);
            }

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