package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieDrawable;
import com.anychart.core.cartesian.series.Line;
import com.anychart.data.Mapping;
import com.anychart.data.Set;
import com.anychart.enums.Anchor;
import com.anychart.enums.MarkerType;
import com.github.mikephil.charting.data.Entry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class API_server extends AppCompatActivity {

    private TextView ip_add;
    private TextView status;
    private TextView info_api_test;
    private Button test_api;
    private ImageView statusImage;

    private CardView cardView_api_test;

    private RelativeLayout relativeLayout_api_test;

    private ProgressBar progressBar;

    private CardView cardView_api_test_2;
    private TextView info_api_test_2;

    int tap_count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_api_server);

        relativeLayout_api_test = findViewById(R.id.test_api_layout);
        cardView_api_test = findViewById(R.id.cardView_result);

        cardView_api_test_2 = findViewById(R.id.cardView_result_FInal);
        info_api_test_2 = findViewById(R.id.textView_result_final);

        progressBar = findViewById(R.id.progress_bar);

        ip_add = findViewById(R.id.ip_server);
        //status = findViewById(R.id.ser);
        info_api_test = findViewById(R.id.textView_result);
        test_api = findViewById(R.id.test_api);
        ip_add.setText("http://" + SharedPreferencesManager.getIpAddressFromSharedPreferences(this) + ":5000");
        statusImage = findViewById(R.id.status_image);

        statusImage.setOnClickListener(v -> {
            tap_count++;
            Toast toast = Toast.makeText(this, "Tap count: " + tap_count, Toast.LENGTH_SHORT);
            toast.show();
            if (tap_count == 5) {
                Toast.makeText(this, "Now you can test API server!", Toast.LENGTH_SHORT).show();

                relativeLayout_api_test.setVisibility(View.VISIBLE);

                tap_count = 0;
            }
        });

        getAPIStatus();

        test_api.setOnClickListener(v -> {
            cardView_api_test.setVisibility(View.VISIBLE);

            getAPITests();
        });

    }


    private void getAPITests() {

        progressBar.setVisibility(View.VISIBLE);
        new Thread(new Runnable() {
            @Override
            public void run() {
                // Získání uložené IP adresy z SharedPreferences
                String ipAddress = SharedPreferencesManager.getIpAddressFromSharedPreferences(API_server.this);
                String apiUrl = "http://" + ipAddress + ":5000/api/test/run_all_tests";

                // Odeslání požadavku na server
                try {
                    URL url = new URL(apiUrl);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");

                    // Přidání "Bearer token" do hlavičky požadavku
                    String accessToken = SharedPreferencesManager.getAccessTokenFromSharedPreferences(API_server.this);
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
                            processJsonResponseTestApi(jsonResponse);
                        }
                    });

                    // Uzavření spojení
                    connection.disconnect();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    // Skrytí ProgressBaru po dokončení operace
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setVisibility(View.GONE);
                            cardView_api_test_2.setVisibility(View.VISIBLE);
                        }
                    });
                }
            }
        }).start();
    }

    private void getAPIStatus() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                // Získání uložené IP adresy z SharedPreferences
                String ipAddress = SharedPreferencesManager.getIpAddressFromSharedPreferences(API_server.this);
                String apiUrl = "http://" + ipAddress + ":5000/api/is_valid";

                // Odeslání požadavku na server
                try {
                    URL url = new URL(apiUrl);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");

                    // Přidání "Bearer token" do hlavičky požadavku
                    String accessToken = SharedPreferencesManager.getAccessTokenFromSharedPreferences(API_server.this);
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
                            processJsonResponseStatusApi(jsonResponse);
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

    private void processJsonResponseTestApi(String jsonResponse) {
        try {
            JSONObject jsonObject = new JSONObject(jsonResponse);
            int numErrors = jsonObject.getInt("num_errors");
            int numFailures = jsonObject.getInt("num_failures");
            int numSkipped = jsonObject.getInt("num_skipped");
            int numTestsRun = jsonObject.getInt("num_tests_run");
            boolean testsPassed = jsonObject.getBoolean("tests_passed");



            // Sestavení zprávy
            String info = "Number of errors: " + numErrors + "\n" +
                    "Number of failures: " + numFailures + "\n" +
                    "Number of skipped tests: " + numSkipped + "\n" +
                    "Number of tests run: " + numTestsRun ;

            if (testsPassed) {
                info_api_test_2.setTextColor(Color.parseColor("#00FF00"));
            } else {
                info_api_test_2.setTextColor(Color.parseColor("#FF0000"));
            }

            info_api_test_2.setText(testsPassed ? "All tests passed!" : "Some tests failed!");

            // Nastavení zprávy do textového pole
            info_api_test.setText(info);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("JSON_ERROR", "Error processing JSON response", e);
            Toast.makeText(this, "Error processing JSON response", Toast.LENGTH_SHORT).show();
        }
    }

    private void processJsonResponseStatusApi(String jsonResponse) {
        try {
            JSONObject jsonObject = new JSONObject(jsonResponse);
            int valid_token = jsonObject.getInt("valid_token");

            if (valid_token == 1) {
                statusImage.setImageResource(R.drawable.green);
                test_api.setEnabled(true);
                test_api.setClickable(true);

            } else {
                statusImage.setImageResource(R.drawable.red);
                test_api.setEnabled(false);
                test_api.setClickable(false);
                test_api.setAlpha(0.5f);
            }


        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error processing JSON response", Toast.LENGTH_SHORT).show();
        }
    }
}