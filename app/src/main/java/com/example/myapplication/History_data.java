package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

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

public class History_data extends AppCompatActivity {
    private LineChart lineChart;
    String[] historyData = {"daily", "weekly", "monthly"};
    AutoCompleteTextView autoCompleteTextView;
    ArrayAdapter<String> adapter;
    private String jsonString;
    private String unit;
    private String weather;
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_data);
        lineChart = findViewById(R.id.lineChart);
        textView = findViewById(R.id.textView);

        // Získání předaných dat z intentu
        Intent intent = getIntent();
        jsonString = intent.getStringExtra("jsonObject");
        unit = intent.getStringExtra("unit");
        weather = intent.getStringExtra("weather");

        textView.setText(weather);

        autoCompleteTextView = findViewById(R.id.autoCompleteTextView);
        adapter = new ArrayAdapter<String>(this, R.layout.history_item, historyData);

        autoCompleteTextView.setAdapter(adapter);
        autoCompleteTextView.setThreshold(1);
        autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selected = parent.getItemAtPosition(position).toString();
                getDataFrom(selected);
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
            ArrayList<String> labels = new ArrayList<>();
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


                for (String item : historyData)
                {
                    if (item.equals(item_selected))
                    {
                        if (item == "daily")
                        {
                            String timestamp = jsonObject.getString("week_start");
                            labels.add(timestamp);
                        }
                        else if (item == "weekly")
                        {
                            String timestamp = jsonObject.getString("week_start");
                            labels.add(timestamp);
                        }
                        else if (item == "monthly")
                        {
                            String timestamp = jsonObject.getString("next_month_start");
                            labels.add(timestamp);
                        }
                    }
                }



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
                    int index = (int) value;
                    if (index >= 0 && index < labels.size()) {
                        return labels.get(index); // Vracíme popisek z ArrayListu labels
                    } else {
                        return ""; // Pokud index není v rozsahu, vrátíme prázdný řetězec
                    }
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
            //textViewAverage.setText("Average: " + String.format("%.1f", average) + " " + unit_ + "\n" + "Min: " + String.format("%.1f", min) + " " + unit_ + "\n" + "Max: " + String.format("%.1f", max) + " " + unit_ );
            //textViewMin.setText("Min: " + String.format("%.1f", min) + " " + unit);
            //textViewMax.setText("Max: " + String.format("%.1f", max) + " " + unit);
            //dej mi všechny hodnoty do textViewAverage

        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error processing JSON response", Toast.LENGTH_SHORT).show();
        }
    }
}