package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private EditText usernameEditText;
    private EditText passwordEditText;
    private TextView errorTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializace prvků layoutu
        usernameEditText = findViewById(R.id.username);
        passwordEditText = findViewById(R.id.password);
        errorTextView = findViewById(R.id.errorTextView);

        // Pokud je "Stay login" zapnutý, přejděte rovnou na dashboard layout
        if (SharedPreferencesManager.getStayLoginFromSharedPreferences(this)) {
            String username = SharedPreferencesManager.getUsernameFromSharedPreferences(this);
            String password = SharedPreferencesManager.getDecodedPasswordFromSharedPreferences(this);
            sendLoginRequest(MainActivity.this, username, password);
            //openDashboardActivity();
        }

        // Nastavení posluchače pro tlačítko Login
        Button loginButton = findViewById(R.id.loginButton);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Získání jména a hesla z EditText prvků
                String username = usernameEditText.getText().toString();
                String password = passwordEditText.getText().toString();

                sendLoginRequest(MainActivity.this, username, password);
            }
        });

        TextView signupText = findViewById(R.id.signupText);
        signupText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSignUpActivity();
            }
        });
    }



    private void sendLoginRequest(final Context context, final String username, final String password) {
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

                        // Přechod na další aktivitu
                        openDashboardActivity();
                    } else {
                        // Zde můžete zpracovat různé kódy odpovědi (např. 404, 500 atd.)
                        // Prozatím zde nebudeme zpracovávat jiné kódy než 200
                        showErrorDialog();
                    }

                    // Uzavření spojení
                    connection.disconnect();
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void openSignUpActivity() {
        Intent intent = new Intent(this, SignUP.class);
        Bundle bundle = ActivityOptions.makeSceneTransitionAnimation(this).toBundle();
        startActivity(intent, bundle);
    }

    public void openDashboardActivity() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(MainActivity.this, DashBoard.class);
                Bundle bundle = ActivityOptions.makeSceneTransitionAnimation(MainActivity.this).toBundle();
                startActivity(intent, bundle);
            }
        });
    }


    private void showErrorDialog() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Error");
                builder.setMessage("Invalid station_id or user already exists");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.show();
            }
        });
    }
}