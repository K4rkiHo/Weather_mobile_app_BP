package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.charts.Cartesian;
import com.anychart.core.cartesian.series.Line;
import com.anychart.data.Mapping;
import com.anychart.data.Set;
import com.anychart.enums.Anchor;
import com.anychart.enums.MarkerType;
import com.anychart.enums.TooltipPositionMode;
import com.anychart.graphics.vector.Stroke;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class Weather_layout extends AppCompatActivity {

    private TextView textView_test;

    private String jsonString;
    private String unit;
    private String weather;
    private String convert_unit;

    private String original_unit;
    private LineChart lineChart;

    private TextView textViewAverage;
    private TextView textViewMin;
    private TextView textViewMax;

    private TextView textViewDate;
    private Button button;

    private Button calendar;

    private boolean dataForTodayCalled = false;


    List<DataEntry> seriesData = new ArrayList<>();

    Cartesian cartesian;
    AnyChartView anyChartView;

    private UnitConverter unitConverter = new UnitConverter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather_layout);

        textView_test = findViewById(R.id.textView);

        textViewAverage = findViewById(R.id.text_avg);
        textViewMin = findViewById(R.id.text_min);
        textViewMax = findViewById(R.id.text_max);

        textViewDate = findViewById(R.id.date);

        //lineChart = findViewById(R.id.lineChart);
        Intent intent = getIntent();
        // Získání předaných dat z intentu
        jsonString = intent.getStringExtra("jsonObject");
        unit = intent.getStringExtra("unit");
        weather = intent.getStringExtra("weather");
        convert_unit = intent.getStringExtra("convert_unit");
        original_unit = intent.getStringExtra("original_unit");

        System.out.println("JsonObject: " + jsonString);
        System.out.println("unit: " + unit);
        System.out.println("weather: " + weather);
        System.out.println("convert_unit: " + convert_unit);
        System.out.println("original_unit: " + original_unit);

        //String[] parts = unit.split(" ");
        //parts[1]
        String unit_ = unit;


        anyChartView = findViewById(R.id.any_chart_view);
        anyChartView.setProgressBar(findViewById(R.id.progress_bar));




        cartesian = AnyChart.line();

        cartesian.animation(true);


        cartesian.padding(10d, 10d, 10d, 10d);

        cartesian.crosshair().enabled(true);
        cartesian.crosshair()
                .yLabel(true)
                .yStroke((Stroke) null, null, null, (String) null, (String) null);

        cartesian.tooltip().positionMode(TooltipPositionMode.POINT);

        //cartesian.title(weather);

        cartesian.yAxis(0).title( convert_unit );
        cartesian.xAxis(0).labels().padding(5d, 5d, 5d, 5d);




        button = findViewById(R.id.button);
        button.setOnClickListener(v -> {
            Intent intent_ = new Intent(Weather_layout.this, History_data.class);
            intent_.putExtra("weather",weather);
            intent_.putExtra("jsonObject", jsonString);
            intent_.putExtra("unit", unit); // Přidání názvu karty
            intent_.putExtra("convert_unit", convert_unit);
            intent_.putExtra("original_unit", original_unit);
            startActivity(intent_);
        });



        textView_test.setText(weather);
        getDataForToday();
        /*
        if (!dataForTodayCalled) {
            getDataForToday();
            dataForTodayCalled = true; // Nastavit, že metoda byla zavolána
            //cartesian.removeAllSeries();
        }

         */

        calendar = findViewById(R.id.button_calendar);
        calendar.setOnClickListener(v -> {
            //cartesian.removeAllSeries();
            openDialog();
            anyChartView.setChart(cartesian);
        });

        Boolean basic =  SharedPreferencesManager.getBasicBackgroudFromSharedPreferences(this);
        if (basic) {
            ConstraintLayout constraintLayout = findViewById(R.id.constantLayout);
            constraintLayout.setBackgroundColor(Color.parseColor("#FFFFFF"));
        }


    }

    private void getDataForToday() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                // Získání uložené IP adresy z SharedPreferences
                String ipAddress = SharedPreferencesManager.getIpAddressFromSharedPreferences(Weather_layout.this);
                String apiUrl = "http://" + ipAddress + ":5000/api/data/aggregated/today";

                // Odeslání požadavku na server
                try {
                    URL url = new URL(apiUrl);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");

                    // Přidání "Bearer token" do hlavičky požadavku
                    String accessToken = SharedPreferencesManager.getAccessTokenFromSharedPreferences(Weather_layout.this);
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
            cartesian.removeAllSeries();
            JSONArray jsonArray = new JSONArray(jsonResponse);
            List<DataEntry> seriesData = new ArrayList<>();
            double sum = 0;
            double min = Double.MAX_VALUE;
            double max = Double.MIN_VALUE;
            String date = "";



            if (jsonArray.length() == 0) {
                // Pole jsonArray je prázdné, vypište chybu
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // Zde můžete zobrazit uživateli upozornění, že data nebyla možné načíst
                        Toast.makeText(Weather_layout.this, "Error loading data for today", Toast.LENGTH_SHORT).show();

                        /*
                        for (int i = 0; i < 24; i++) {
                            seriesData.add(new CustomWeatherDataEntry(String.valueOf(i) + "h", 0));
                        }
                        Set set = Set.instantiate();
                        set.data(seriesData);
                        Mapping series1Mapping = set.mapAs("{ x: 'x', value: 'value' }");
                        Line series1 = cartesian.line(series1Mapping);
                        series1.name(weather);
                        series1.hovered().markers().enabled(true);
                        series1.hovered().markers()
                                .type(MarkerType.CIRCLE)
                                .size(4d);
                        series1.tooltip()
                                .position("right")
                                .anchor(Anchor.LEFT_CENTER)
                                .offsetX(5d)
                                .offsetY(5d);

                        series1.color("#808080");

                        //cartesian.legend().enabled(true);
                        cartesian.legend().fontSize(18d);
                        cartesian.legend().padding(0d, 0d, 10d, 0d);


                        cartesian.xScroller(true);

                        anyChartView.setChart(cartesian);

                         */

                        textViewAverage.setText("Avg: ");
                        textViewMin.setText("Min: ");
                        textViewMax.setText("Max: ");

                        //cartesian.removeAllSeries();
                    }
                });
                return; // Ukončíme zpracování dat, protože nejsou k dispozici žádné položky
            }



            SimpleDateFormat inputDateFormat = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss z", Locale.US);
            SimpleDateFormat outputDateFormat = new SimpleDateFormat("yyyy-dd-MM", Locale.US);
            // Procházení všech objektů v poli
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);

                // Získání hodnoty z JSON objektu
                double value = jsonObject.getDouble(jsonString);
                date = jsonObject.getString("time");

                double convertedValue = unitConverter.convertValueToSavedUnit(value, original_unit, convert_unit);

                int hour = getHourFromTimeString(date);

                //System.out.println("HOUR: " + hour);
                if (i == hour) {
                    //System.out.println("HOUR2: " + hour);
                    seriesData.add(new CustomWeatherDataEntry(String.valueOf(i) + "h", Math.round(convertedValue)));
                } else {
                    seriesData.add(new CustomWeatherDataEntry(String.valueOf(i) + "h", 0));
                }

                //seriesData.add(new CustomWeatherDataEntry(String.valueOf(i) + "h", Math.round(convertedValue)));

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
            double average = sum / jsonArray.length();

            try {
                Date date_out = inputDateFormat.parse(date);
                String outputDateString = outputDateFormat.format(date_out);
                textViewDate.setText("Date: " + outputDateString);

            } catch (ParseException e) {
                e.printStackTrace();
            }

            //textViewDate.setText("Date: " + date);

            textViewAverage.setText("Avg: " + Math.round(unitConverter.convertValueToSavedUnit(average, original_unit, convert_unit )) + " " + convert_unit);

            textViewMin.setText("Min: " + Math.round(unitConverter.convertValueToSavedUnit(min,original_unit, convert_unit)) + " " + convert_unit);

            textViewMax.setText("Max: " + Math.round(unitConverter.convertValueToSavedUnit(max, original_unit, convert_unit)) + " " + convert_unit);
            //cartesian.removeAllSeries();
            //new data graph

            Set set = Set.instantiate();
            set.data(seriesData);
            Mapping series1Mapping = set.mapAs("{ x: 'x', value: 'value' }");
            Line series1 = cartesian.line(series1Mapping);
            series1.name(weather);
            series1.hovered().markers().enabled(true);
            series1.hovered().markers()
                    .type(MarkerType.CIRCLE)
                    .size(4d);
            series1.tooltip()
                    .position("right")
                    .anchor(Anchor.LEFT_CENTER)
                    .offsetX(5d)
                    .offsetY(5d);

            //cartesian.legend().enabled(true);
            cartesian.legend().fontSize(18d);
            cartesian.legend().padding(0d, 0d, 10d, 0d);

            cartesian.xScroller(true);

            anyChartView.setChart(cartesian);

        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error processing JSON response", Toast.LENGTH_SHORT).show();
        }
    }

    private static int getHourFromTimeString(String timeString) {
        try {
            SimpleDateFormat inputDateFormat = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH);
            inputDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
            Date date = inputDateFormat.parse(timeString);

            // Nastavíme časové pásmo na GMT+2
            SimpleDateFormat outputDateFormat = new SimpleDateFormat("HH");
            outputDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

            // Získáme hodinu z datumu
            return Integer.parseInt(outputDateFormat.format(date));
        } catch (ParseException e) {
            e.printStackTrace();
            // Pokud selže, vrátíme hodnotu -1 nebo něco jiného, co vám vyhovuje jako indikátor chyby.
            return -1;
        }
    }

    private void getDataBydate(String date) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                // Získání uložené IP adresy z SharedPreferences
                String ipAddress = SharedPreferencesManager.getIpAddressFromSharedPreferences(Weather_layout.this);
                String apiUrl = "http://" + ipAddress + ":5000/api/data/aggregated/" + date;

                // Odeslání požadavku na server
                try {
                    URL url = new URL(apiUrl);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");

                    // Přidání "Bearer token" do hlavičky požadavku
                    String accessToken = SharedPreferencesManager.getAccessTokenFromSharedPreferences(Weather_layout.this);
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
                            processJsonResponseByDate(jsonResponse);
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
    private void processJsonResponseByDate(String jsonResponse) {
        try {
            //cartesian.removeAllSeries();
            cartesian.removeAllSeries();
            anyChartView.setVisibility(View.VISIBLE);
            JSONArray jsonArray = new JSONArray(jsonResponse);
            List<DataEntry> seriesData = new ArrayList<>();
            List<Integer> hours = new ArrayList<>();
            hours.add(0);

            String date = "";
            double sum = 0;
            double min = Double.MAX_VALUE;
            double max = Double.MIN_VALUE;

            if (jsonArray.length() == 0) {
                // Pole jsonArray je prázdné, vypište chybu
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        anyChartView.setVisibility(View.INVISIBLE);
                        // Zde můžete zobrazit uživateli upozornění, že data nebyla možné načíst
                        Toast.makeText(Weather_layout.this, "Error loading data for the selected date", Toast.LENGTH_SHORT).show();

                        textViewAverage.setText("Avg: ");
                        textViewMin.setText("Min: ");
                        textViewMax.setText("Max: ");
                    }
                });
                return; // Ukončíme zpracování dat, protože nejsou k dispozici žádné položky
            }

            // Procházení všech objektů v poli
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);

                //System.out.println("JSON OBJECT: " + jsonObject.toString());

                // Získání hodnoty z JSON objektu
                double value = jsonObject.getDouble(jsonString);

                date = jsonObject.getString("time");

                // Zaokrouhlení hodnoty na jedno desetinné místo
                float roundedValue = (Math.round(value * 10.0f) / 10.0f);

                double convertedValue = unitConverter.convertValueToSavedUnit(value, original_unit, convert_unit);

                int hour = getHourFromTimeString(date);

                if (!hours.contains(hour)) {
                    hours.add(hour);
                    System.out.println("Added to hours!");
                    seriesData.add(new CustomWeatherDataEntry(String.valueOf(hour) + "h", Math.round(convertedValue)));
                }

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

            // Doplnění nulových hodnot pro chybějící hodiny
            for (int hour = 0; hour <= 23; hour++) {
                if (!hours.contains(hour)) {
                    seriesData.add(new CustomWeatherDataEntry(String.valueOf(hour) + "h", 0));
                }
            }

            double average = sum / jsonArray.length();

            textViewAverage.setText("Avg: " + Math.round(unitConverter.convertValueToSavedUnit(average, original_unit, convert_unit )) + " " + convert_unit);

            textViewMin.setText("Min: " + Math.round(unitConverter.convertValueToSavedUnit(min,original_unit, convert_unit)) + " " + convert_unit);

            textViewMax.setText("Max: " + Math.round(unitConverter.convertValueToSavedUnit(max, original_unit, convert_unit)) + " " + convert_unit);

            for (int x : hours)
            {
                System.out.println("HOURS: " + x);
            }

            //new data graph
            Set set = Set.instantiate();
            set.data(seriesData);
            Mapping series1Mapping = set.mapAs("{ x: 'x', value: 'value' }");
            Line series1 = cartesian.line(series1Mapping);
            series1.name(weather);
            series1.hovered().markers().enabled(true);
            series1.hovered().markers()
                    .type(MarkerType.CIRCLE)
                    .size(4d);
            series1.tooltip()
                    .position("right")
                    .anchor(Anchor.LEFT_CENTER)
                    .offsetX(5d)
                    .offsetY(5d);

            //cartesian.legend().enabled(true);
            cartesian.legend().fontSize(18d);
            cartesian.legend().padding(0d, 0d, 10d, 0d);

            cartesian.xScroller(true);

            hours.clear();

            //anyChartView.setChart(cartesian);



        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error processing JSON response", Toast.LENGTH_SHORT).show();
        }
    }

    static class CustomWeatherDataEntry extends ValueDataEntry {
        CustomWeatherDataEntry(String x, Number value) {
            super(x, value);
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    private void openDialog() {
        Locale.setDefault(Locale.ENGLISH);

        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                //Toast.makeText(Weather_layout.this, "Selected date: " + day + "." + (month + 1) + "." + year, Toast.LENGTH_SHORT).show();
                String date = year + "-" + (month + 1) + "-" + day;
                getDataBydate(date);
                textViewDate.setText("Date: " + date);
            }
        }, year, month, day);

        datePickerDialog.show();
    }
}
