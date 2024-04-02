package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class SignUP extends AppCompatActivity {

    private EditText usernameEditText;
    private EditText passwordEditText;
    private EditText stationEditText;
    private Button registerButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // Inicializace prvků layoutu
        usernameEditText = findViewById(R.id.username);
        passwordEditText = findViewById(R.id.password);
        stationEditText = findViewById(R.id.station_id);
        registerButton = findViewById(R.id.registerButton);

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Získání jména a hesla z EditText prvků
                String username = usernameEditText.getText().toString();
                String password = passwordEditText.getText().toString();
                String station_id = stationEditText.getText().toString();

                sendRegisterRequest(SignUP.this, username, password, station_id);
            }
        });
        finish();
    }


    private void sendRegisterRequest(final Context context, final String username, final String password, final String station_id) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                // Získání uložené IP adresy z SharedPreferences
                String ipAddress = SharedPreferencesManager.getIpAddressFromSharedPreferences(context);
                String apiUrl = "http://" + ipAddress + ":5000/api/register";

                // Vytvoření JSON objektu s přihlašovacími údaji
                JSONObject requestData = new JSONObject();
                try {
                    requestData.put("username", username);
                    requestData.put("password", password);
                    requestData.put("code", station_id);
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
                    if (responseCode == HttpURLConnection.HTTP_CREATED) {
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
                        SharedPreferencesManager.saveStationIdToSharedPreferences(context, station_id);

                        // Přechod na další aktivitu
                        openDashboardActivity();
                    } else {
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
    private void showErrorDialog() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(SignUP.this);
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
    public void openDashboardActivity() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SignUP.this, DashBoard.class);
                Bundle bundle = ActivityOptions.makeSceneTransitionAnimation(SignUP.this).toBundle();
                startActivity(intent, bundle);
                finish();
            }
        });
    }
}