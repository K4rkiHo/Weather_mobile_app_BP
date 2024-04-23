package com.example.myapplication;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import java.util.ArrayList;
import java.util.List;

public class SharedPreferencesManager {
    private static final String PREF_NAME = "MyPrefs";
    private static final String KEY_IP_ADDRESS = "ipAddress";
    private static final String ACCESS_TOKEN_KEY = "access_token";
    private static final String USERNAME = "username";
    private static final String KEY_STAY_LOGIN = "stay_login";
    private static final String BASIC_BACKGROUD = "basic_background";
    private static final String KEY_ENCODED_PASSWORD = "encoded_password";
    private static final String STATION_ID = "station_id";
    private static final String LIST_ORDER = "list_order";
    private static final String LOAD_LIST_ORDER_FROM_SP = "load_list_order_from_sp";

    // Uložení IP adresy do SharedPreferences
    public static void saveIpAddressToSharedPreferences(Context context, String ipAddress) {
        SharedPreferences.Editor editor = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit();
        editor.putString(KEY_IP_ADDRESS, ipAddress);
        editor.apply();
    }

    // Získání uložené IP adresy z SharedPreferences
    public static String getIpAddressFromSharedPreferences(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_IP_ADDRESS, "");
    }
    public static void saveAccessTokenToSharedPreferences(Context context, String accessToken) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(ACCESS_TOKEN_KEY, accessToken);
        editor.apply();
    }

    public static String getAccessTokenFromSharedPreferences(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(ACCESS_TOKEN_KEY, null);
    }
    public static void saveUsernameToSharedPreferences(Context context, String username) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(USERNAME, username);
        editor.apply();
    }

    public static String getUsernameFromSharedPreferences(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(USERNAME, null);
    }

    public static void saveStayLoginToSharedPreferences(Context context, boolean stayLogin) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_STAY_LOGIN, stayLogin);
        editor.apply();
    }

    public static boolean getStayLoginFromSharedPreferences(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(KEY_STAY_LOGIN, false);
    }

    public static void saveBasicBackgroudToSharedPreferences(Context context, boolean basicBackgroud) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(BASIC_BACKGROUD, basicBackgroud);
        editor.apply();
    }

    public static boolean getBasicBackgroudFromSharedPreferences(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(BASIC_BACKGROUD, false);
    }


    // Uložení zakódovaného hesla do SharedPreferences
    public static void saveEncodedPasswordToSharedPreferences(Context context, String password) {
        // Zakódování hesla pomocí Base64
        String encodedPassword = Base64.encodeToString(password.getBytes(), Base64.DEFAULT);

        // Uložení zakódovaného hesla do SharedPreferences
        SharedPreferences.Editor editor = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit();
        editor.putString(KEY_ENCODED_PASSWORD, encodedPassword);
        editor.apply();
    }

    // Získání dekódovaného hesla z SharedPreferences
    public static String getDecodedPasswordFromSharedPreferences(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String encodedPassword = prefs.getString(KEY_ENCODED_PASSWORD, "");

        // Dekódování hesla pomocí Base64
        byte[] decodedBytes = Base64.decode(encodedPassword, Base64.DEFAULT);
        return new String(decodedBytes);
    }

    public static void saveStationIdToSharedPreferences(Context context, String stationId) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(STATION_ID, stationId);
        editor.apply();
    }

    public static void saveOrderToSharedPreferences(Context context, List<JsonObjectModel> dataList) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        StringBuilder orderBuilder = new StringBuilder();
        for (JsonObjectModel item : dataList) {
            orderBuilder.append(item.getId()).append(",");
        }
        editor.putString(LIST_ORDER, orderBuilder.toString());
        editor.apply();
    }
    public static void loadOrderFromSharedPreferences(Context context, List<JsonObjectModel> dataList) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String order = sharedPreferences.getString(LIST_ORDER, "");
        String[] orderArray = order.split(",");

        List<JsonObjectModel> orderedList = new ArrayList<>();

        // Naplnění nového seznamu podle uloženého pořadí
        for (String key : orderArray) {
            for (JsonObjectModel item : dataList) {
                if (item.getId().equals(key)) {
                    orderedList.add(item);
                    break;
                }
            }
        }
        dataList.clear();
        dataList.addAll(orderedList);
    }

    public static void setLoadListOrderFromSp(Context context, boolean loadListOrderFromSp) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(LOAD_LIST_ORDER_FROM_SP, loadListOrderFromSp);
        editor.apply();
    }

    public static boolean getLoadListOrderFromSp(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(LOAD_LIST_ORDER_FROM_SP, false);
    }



}
