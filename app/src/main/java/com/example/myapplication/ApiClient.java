package com.example.myapplication;

import android.content.Context;
import android.content.SharedPreferences;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class ApiClient {
    private Context context;
    private String baseUrl;
    private RequestQueue requestQueue; // Deklarace proměnné requestQueue

    public ApiClient(Context context) {
        this.context = context;
        this.baseUrl = SharedPreferencesManager.getIpAddressFromSharedPreferences(context);
    }

    private RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            // Vytvoření nové fronty požadavků
            requestQueue = Volley.newRequestQueue(context.getApplicationContext());
        }
        return requestQueue;
    }

    public String getCurrentIpAddress() {
        return SharedPreferencesManager.getIpAddressFromSharedPreferences(context);
    }
    public String getBaseUrl() {
        return "http://" + getCurrentIpAddress() + ":5000";
    }
    public String createUrl(String path) {
        return getBaseUrl() + path;
    }

    public static void TestApi(final String url_test, final VolleyCallbackAPITest callback) {
        String url = url_test;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        callback.onSuccess(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        callback.onError(error);
                    }
                }) {
        };
        Volley.newRequestQueue(AppContext.getInstance()).add(jsonObjectRequest);
    }
    // Metoda pro získání tokenu s použitím BASE_URL
    public void getToken(final String username, final String password, final VolleyCallbackToken callback) {
        String url = createUrl("/api/login");

        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("username", username);
            requestBody.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, requestBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String token = response.getString("access_token");
                            SharedPreferences preferences = context.getSharedPreferences("Api_Token_file", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putString("token", token);
                            editor.apply();
                            callback.onSuccess(token);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        callback.onError(error);
                    }
                });

        getRequestQueue().add(jsonObjectRequest); // Zde používáme metodu getRequestQueue() pro získání fronty požadavků
    }




    public interface VolleyCallbackWeatherData {
        void onSuccess(JSONObject weatherData);

        void onError(VolleyError error);
    }

    public interface VolleyCallbackAPITest{
        void onSuccess(JSONObject result);

        void onError(VolleyError error);
    }

    public interface VolleyCallbackColumns {
        void onSuccess(List<String> columnsList);

        void onError(VolleyError error);
    }

    // Rozhraní pro zpětné volání (callback) pro zpracování odpovědi z API
    public interface VolleyCallback {
        void onSuccess(JSONObject result) throws JSONException;

        void onError(VolleyError error);
    }

    public interface VolleyCallbackToken {
        void onSuccess(String token);

        void onError(VolleyError error);
    }

    public interface VolleyCallbackStatus {
        void onSuccess(String status);

        void onError(VolleyError error);
    }
    public interface VolleyCallbackDataList {
        void onSuccess(List<JSONObject> dataList);

        void onError(VolleyError error);
    }

}
