package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Weather_layout extends AppCompatActivity {

    private TextView textView_test;

    private String jsonString;
    private String unit;
    private String weather;
    private LineChart lineChart;

    private TextView textViewAverage;
    private TextView textViewMin;
    private TextView textViewMax;
    private Button button;


    List<DataEntry> seriesData = new ArrayList<>();

    Cartesian cartesian;
    AnyChartView anyChartView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather_layout);

        textView_test = findViewById(R.id.textView);

        textViewAverage = findViewById(R.id.text_avg);
        textViewMin = findViewById(R.id.text_min);
        textViewMax = findViewById(R.id.text_max);

        //lineChart = findViewById(R.id.lineChart);
        Intent intent = getIntent();
        // Získání předaných dat z intentu
        jsonString = intent.getStringExtra("jsonObject");
        unit = intent.getStringExtra("unit");
        weather = intent.getStringExtra("weather");

        String[] parts = unit.split(" ");
        String unit_ = parts[1];


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

        cartesian.title(weather);

        cartesian.yAxis(0).title( unit_ );
        cartesian.xAxis(0).labels().padding(5d, 5d, 5d, 5d);


        button = findViewById(R.id.button);
        button.setOnClickListener(v -> {
            Intent intent_ = new Intent(Weather_layout.this, History_data.class);
            intent_.putExtra("weather",weather);
            intent_.putExtra("jsonObject", jsonString);
            intent_.putExtra("unit", unit); // Přidání názvu karty
            startActivity(intent_);
        });

        textView_test.setText(weather);

        getDataForToday();


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
            JSONArray jsonArray = new JSONArray(jsonResponse);

            // Inicializace ArrayListu pro uchování dat pro graf
            ArrayList<Entry> entries = new ArrayList<>();
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

                seriesData.add(new CustomWeatherDataEntry(String.valueOf(i) + "h", roundedValue));

                // Přidání nového bodu do seznamu bodů pro graf
                entries.add(new Entry(i, roundedValue));


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

            cartesian.legend().enabled(true);
            cartesian.legend().fontSize(18d);
            cartesian.legend().padding(0d, 0d, 10d, 0d);

            cartesian.xScroller(true);

            anyChartView.setChart(cartesian);

            /*
            // Vytvoření datové sady pro graf
            LineDataSet dataSet = new LineDataSet(entries, weather);
            dataSet.setColor(getResources().getColor(R.color.colorPrimary));
            dataSet.setCircleColor(getResources().getColor(R.color.colorPrimary));
            dataSet.setValueTextColor(getResources().getColor(R.color.colorPrimaryDark));
            dataSet.setLineWidth(2f);
            dataSet.setCircleRadius(4f);
            dataSet.setDrawCircleHole(false);
            dataSet.setValueTextSize(12f);

            // Vytvoření datového objektu pro graf
            ArrayList<ILineDataSet> dataSets = new ArrayList<>();
            dataSets.add(dataSet);

            // Vytvoření datové struktury pro graf
            LineData lineData = new LineData(dataSets);

            // Nastavení dat do grafu
            lineChart.setData(lineData);

            // Nastavení formátu popisu osy X
            XAxis xAxis = lineChart.getXAxis();
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setGranularity(1f);
            xAxis.setValueFormatter(new ValueFormatter() {
                @Override
                public String getAxisLabel(float value, AxisBase axis) {
                    DecimalFormat decimalFormat = new DecimalFormat("#.#");
                    return decimalFormat.format(value);
                }
            });

            // Popis osy Y
            YAxis leftAxis = lineChart.getAxisLeft();
            leftAxis.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    DecimalFormat df = new DecimalFormat("#.#"); // formátování na jeden desetinný bod
                    return df.format(value);
                }
            });

            // Zakázání popisu osy Y na pravé straně
            lineChart.getAxisRight().setEnabled(false);

            // Nastavení popisu grafu
            Description description = new Description();
            description.setText("");
            lineChart.setDescription(description);

            // Aktualizace grafu
            lineChart.invalidate();


            String[] parts = unit.split(" ");
            String unit_ = parts[1];
            textViewAverage.setText("Average: " + String.format("%.1f", average) + " " + unit_ + "\n" + "Min: " + String.format("%.1f", min) + " " + unit_ + "\n" + "Max: " + String.format("%.1f", max) + " " + unit_ );
            //textViewMin.setText("Min: " + String.format("%.1f", min) + " " + unit);
            //textViewMax.setText("Max: " + String.format("%.1f", max) + " " + unit);
            //dej mi všechny hodnoty do textViewAverage

             */

        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error processing JSON response", Toast.LENGTH_SHORT).show();
        }
    }

    private class CustomWeatherDataEntry extends ValueDataEntry {
        CustomWeatherDataEntry(String x, Number value) {
            super(x, value);
        }

    }
}
