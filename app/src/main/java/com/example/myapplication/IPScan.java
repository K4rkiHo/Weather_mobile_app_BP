package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieDrawable;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class IPScan extends AppCompatActivity {

    LottieAnimationView lottieAnimationView;
    ListView listView;
    ArrayList<String> ipAddressesList;
    ArrayAdapter<String> adapter;
    Button refreshButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ipscan);

        listView = findViewById(R.id.listviewip);
        refreshButton = findViewById(R.id.refreshButton_);

        lottieAnimationView = findViewById(R.id.lottie);
        lottieAnimationView.setRepeatCount(LottieDrawable.INFINITE);

        // Spuštění animace
        lottieAnimationView.playAnimation();

        // Inicializace listu pro ukládání IP adres
        ipAddressesList = new ArrayList<>();

        // Inicializace adaptéru pro ListView s použitím vlastního layoutu
        adapter = new CustomListAdapter(this, ipAddressesList);

        // Přiřazení adaptéru k ListView
        listView.setAdapter(adapter);

        //checkAndOpenMainActivity();

        String ipaddress = SharedPreferencesManager.getIpAddressFromSharedPreferences(this);
        if (!ipaddress.isEmpty()) {
            // Pokud je token již uložen, přeskočit inicializaci seznamu IP adres
            // a provádění HTTP požadavků
            //getIpAdress();
            checkAndOpenMainActivity();
            //return;
        }

        /*
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                getIpAdress();
            }
        }, 5000);

         */
        getIpAdress();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Získání IP adresy z vybrané položky
                String selectedIpAddress = (String) parent.getItemAtPosition(position);

                // Otevření nové aktivity s přenosem vybrané IP adresy
                saveIpAddressToSharedPreferences(selectedIpAddress);
                openDetailActivity(selectedIpAddress);
            }
        });

        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshList();
            }
        });

    }

    private void refreshList() {
        // Clear the IP addresses list
        ipAddressesList.clear();

        // Notify the adapter about the data change
        adapter.notifyDataSetChanged();

        // Show animation
        //lottieAnimationView.setVisibility(View.VISIBLE);
        //lottieAnimationView.playAnimation();

        // Fade in animation
        lottieAnimationView.setAlpha(0f);
        lottieAnimationView.setVisibility(View.VISIBLE);
        lottieAnimationView.animate()
                .alpha(1f)
                .setDuration(1000) // Duration in milliseconds
                .start();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                getIpAdress();
            }
        }, 5000);
    }

    private void getIpAdress()
    {
        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        // Zkontrolovat, zda WiFi je zapnutá
        if (wifiManager.isWifiEnabled()) {
            // Získání informací o Wi-Fi
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();

            // Získání IP adresy v číselné formě
            int ipAddress = wifiInfo.getIpAddress();

            // Převod číselné IP adresy na řetězec
            String ipString = String.format(
                    "%d.%d.%d.",
                    (ipAddress & 0xff),
                    (ipAddress >> 8 & 0xff),
                    (ipAddress >> 16 & 0xff),
                    (ipAddress >> 24 & 0xff)
            );
            ExecutorService executor = Executors.newFixedThreadPool(50); // Počet vláken můžete upravit podle potřeby
            for (int i = 1; i <= 255; i++) {
                final int finalI = i;
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        String ip = ipString + finalI;
                        String apiUrl = "http://" + ip + ":5000/api/is_valid";
                        makeHttpRequest(apiUrl);
                    }
                });
            }
        }
    }

    private void makeHttpRequest(String apiUrl) {
        try {
            URL url = new URL(apiUrl);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");

            // Zde můžete přidat další nastavení HTTP požadavku

            int responseCode = urlConnection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Zpracování úspěšné odpovědi
                BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                String result = response.toString();

                // Přidání úspěšné IP adresy do seznamu na hlavním vlákně UI
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        addIpAddressToList(apiUrl);
                    }
                });
            }
        } catch (IOException e) {
            // Zpracování chyby HTTP požadavku
            Log.e("IPScan", "Error making HTTP request: " + e.getMessage());
        }
    }

    private void addIpAddressToList(String url) {
        try {
            URL parsedUrl = new URL(url);
            String ipAddress = parsedUrl.getHost(); // Získání pouze IP adresy z URL
            if (!ipAddressesList.contains(ipAddress)) {
                // Přidání nové IP adresy do seznamu
                ipAddressesList.add(ipAddress);

                // Aktualizace adaptéru
                adapter.notifyDataSetChanged();

                // Skrytí animace
                lottieAnimationView.setVisibility(View.GONE);
            } else {
                // Aktualizace existující IP adresy v seznamu
                int position = ipAddressesList.indexOf(ipAddress);
                ipAddressesList.set(position, ipAddress);
            }
        } catch (MalformedURLException e) {
            Log.e("IPScan", "Malformed URL: " + url, e);
        }
    }

    private void openDetailActivity(String ipAddress) {
        SharedPreferencesManager.saveIpAddressToSharedPreferences(this, ipAddress);
        Intent intent = new Intent(this, MainActivity.class);
        Bundle bundle = ActivityOptions.makeSceneTransitionAnimation(this).toBundle();
        startActivity(intent, bundle);
        finish();
    }

    private void checkAndOpenMainActivity() {
        // Získání uložené hodnoty IP adresy z SharedPreferences
        String savedIpAddress = SharedPreferencesManager.getIpAddressFromSharedPreferences(this);

        if (!savedIpAddress.isEmpty()) {
            // Otevřít MainActivity, pokud je uložená hodnota IP adresy
            openDetailActivity(savedIpAddress);
        }
    }
    private void saveIpAddressToSharedPreferences(String ipAddress) {
        SharedPreferencesManager.saveIpAddressToSharedPreferences(this, ipAddress);
    }
}