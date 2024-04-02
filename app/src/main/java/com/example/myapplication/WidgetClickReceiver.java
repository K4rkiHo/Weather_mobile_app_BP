package com.example.myapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class WidgetClickReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // Zde zpracujte kliknutí na widget
        // Například spusťte novou aktivitu
        Intent newIntent = new Intent(context, Weather_layout.class);
        newIntent.putExtra("weather", intent.getStringExtra("weather"));
        newIntent.putExtra("jsonObject", intent.getStringExtra("jsonObject"));
        newIntent.putExtra("unit", intent.getStringExtra("unit"));
        newIntent.putExtra("convert_unit", intent.getStringExtra("convert_unit"));
        newIntent.putExtra("original_unit", intent.getStringExtra("original_unit"));
        newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(newIntent);
    }
}
