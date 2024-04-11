package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
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
import com.anychart.graphics.vector.Stroke;
import com.github.mikephil.charting.charts.LineChart;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.MaterialDatePicker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class History_data extends AppCompatActivity {
    private LineChart lineChart;
    String[] historyData = {"daily", "weekly", "monthly"};
    AutoCompleteTextView autoCompleteTextView;
    ArrayAdapter<String> adapter;
    private String jsonString;
    private String unit;
    private String weather;
    TextView textView;
    private TextView textViewAverage;
    private TextView textViewMin;
    private TextView textViewMax;
    private String convert_unit;

    private String original_unit;

    //private final List<DataEntry> seriesData = new ArrayList<>();
    private Cartesian cartesian;
    private AnyChartView anyChartView;

    private UnitConverter unitConverter = new UnitConverter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_data);
        //lineChart = findViewById(R.id.lineChart);
        textView = findViewById(R.id.textView);

        textViewAverage = findViewById(R.id.text_avg);
        textViewMin = findViewById(R.id.text_min);
        textViewMax = findViewById(R.id.text_max);

        Boolean basic =  SharedPreferencesManager.getBasicBackgroudFromSharedPreferences(this);
        if (basic) {
            ConstraintLayout constraintLayout = findViewById(R.id.constantLayout_history);
            constraintLayout.setBackgroundColor(Color.parseColor("#FFFFFF"));
        }


        // Získání předaných dat z intentu
        Intent intent = getIntent();
        jsonString = intent.getStringExtra("jsonObject");
        unit = intent.getStringExtra("unit");
        weather = intent.getStringExtra("weather");
        convert_unit = intent.getStringExtra("convert_unit");
        original_unit = intent.getStringExtra("original_unit");

        textView.setText(weather);

        autoCompleteTextView = findViewById(R.id.autoCompleteTextView);
        adapter = new ArrayAdapter<String>(this, R.layout.history_item, historyData);

        autoCompleteTextView.setAdapter(adapter);
        autoCompleteTextView.setThreshold(1);

        anyChartView = findViewById(R.id.any_chart_view);
        anyChartView.setProgressBar(findViewById(R.id.progress_bar));

        cartesian = AnyChart.line();

        cartesian.animation(true);

        cartesian.padding(10d, 10d, 10d, 10d);

        cartesian.crosshair().enabled(true);
        cartesian.crosshair()
                .yLabel(true)
                .yStroke((Stroke) null, null, null, (String) null, (String) null);

        //cartesian.tooltip().positionMode(TooltipPositionMode.POINT);

        //cartesian.title(weather);

        cartesian.yAxis(0).title( convert_unit );
        cartesian.xAxis(0).labels().padding(5d, 5d, 5d, 5d);

        autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selected = parent.getItemAtPosition(position).toString();
                //seriesData.clear();

                getDataFrom(selected);
                anyChartView.setChart(cartesian);


                //Toast.makeText(History_data.this, selected, Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void getDataFrom(String selected) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                // Získání uložené IP adresy z SharedPreferences
                String ipAddress = SharedPreferencesManager.getIpAddressFromSharedPreferences(History_data.this);
                String apiUrl = "http://" + ipAddress + ":5000/api/data/" + selected;

                // Odeslání požadavku na server
                try {
                    URL url = new URL(apiUrl);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");

                    // Přidání "Bearer token" do hlavičky požadavku
                    String accessToken = SharedPreferencesManager.getAccessTokenFromSharedPreferences(History_data.this);
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
                            processJsonResponse(jsonResponse, selected);
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
    private void processJsonResponse(String jsonResponse, String item_selected) {

        try {
            JSONArray jsonArray = new JSONArray(jsonResponse);
            List<DataEntry> seriesData = new ArrayList<>();






            //List<DataEntry> seriesData_weather = new ArrayList<>();

            //seriesData.clear();
            double sum = 0;
            double min = Double.MAX_VALUE;
            double max = Double.MIN_VALUE;
            // Procházení všech objektů v poli
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);

                // Získání hodnoty z JSON objektu
                double value = jsonObject.getDouble(jsonString);

                // Zaokrouhlení hodnoty na jedno desetinné místo
                float roundedValue = (Math.round(value * 10.0f) / 10.0f);
                double convertedValue = unitConverter.convertValueToSavedUnit(value, original_unit, convert_unit);

                //seriesData.add(new CustomWeatherDataEntry(String.valueOf(i) + "h", Math.round(convertedValue)));

                for (String item : historyData)
                {
                    if (item.equals(item_selected))
                    {
                        if (item == "daily")
                        {
                            String timestamp = jsonObject.getString("week_start");
                            SimpleDateFormat originalFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US); // Přidání Locale.US, pokud používáte anglický formát
                            Date date = originalFormat.parse(timestamp);

                            SimpleDateFormat targetFormat = new SimpleDateFormat("dd.MM.yyyy");
                            String formattedDate = targetFormat.format(date);
                            seriesData.add(new CustomWeatherDataEntry(formattedDate, Math.round(convertedValue)));

                            //labels.add(timestamp);
                        }
                        else if (item == "weekly")
                        {
                            String timestamp = jsonObject.getString("week_start");
                            SimpleDateFormat originalFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
                            Date date = originalFormat.parse(timestamp);

                            // Nastavení kalendáře na získané datum
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTime(date);

                            // Nastavení na první den týdne
                            calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
                            Date firstDayOfWeek = calendar.getTime();

                            // Nastavení na poslední den týdne
                            calendar.add(Calendar.DAY_OF_YEAR, 6); // Přidání 6 dní pro získání posledního dne týdne
                            Date lastDayOfWeek = calendar.getTime();

                            // Formátování prvního a posledního dne týdne
                            SimpleDateFormat targetFormat = new SimpleDateFormat("dd.MM.yyyy");
                            String firstDayFormatted = targetFormat.format(firstDayOfWeek);
                            String lastDayFormatted = targetFormat.format(lastDayOfWeek);

                            // Vytvoření řetězce s rozmezím týdne
                            String weekRange = firstDayFormatted + " - " + lastDayFormatted;

                            // Přidání do datové struktury
                            seriesData.add(new CustomWeatherDataEntry(weekRange, Math.round(convertedValue)));
                        }
                        else if (item == "monthly")
                        {
                            // Získání hodnoty začátku následujícího měsíce ze získaných dat z API
                            String timestamp = jsonObject.getString("next_month_start");
                            SimpleDateFormat originalFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
                            Date date = originalFormat.parse(timestamp);

                            // Nastavení kalendáře na získané datum
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTime(date);

                            // Získání názvu měsíce
                            SimpleDateFormat targetFormat = new SimpleDateFormat("MMMM yyyy", Locale.US);
                            String monthName = targetFormat.format(date);

                            // Přidání názvu měsíce do datové struktury
                            seriesData.add(new CustomWeatherDataEntry(monthName, Math.round(convertedValue))); // Přidání názvu měsíce
                        }
                    }
                }



                // Přidání nového bodu do seznamu bodů pro graf
                //entries.add(new Entry(i, roundedValue));
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

            textViewAverage.setText("Avg: " + Math.round(unitConverter.convertValueToSavedUnit(average, original_unit, convert_unit)) + " " + convert_unit);
            textViewMin.setText("Min: " + Math.round(unitConverter.convertValueToSavedUnit(min, original_unit, convert_unit)) + " " + convert_unit);
            textViewMax.setText("Max: " + Math.round(unitConverter.convertValueToSavedUnit(max, original_unit, convert_unit)) + " " + convert_unit);

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

            seriesData.clear();

        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error processing JSON response", Toast.LENGTH_SHORT).show();
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    static class CustomWeatherDataEntry extends ValueDataEntry {
        CustomWeatherDataEntry(String x, Number value) {
            super(x, value);
        }
    }
}