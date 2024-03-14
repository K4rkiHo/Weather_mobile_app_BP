package com.example.myapplication;// CustomListAdapter.java
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

public class CustomListAdapter extends ArrayAdapter<String> {

    private Context context;
    private ArrayList<String> ipAddresses;

    public CustomListAdapter(@NonNull Context context, ArrayList<String> ipAddresses) {
        super(context, R.layout.item_list, ipAddresses);
        this.context = context;
        this.ipAddresses = ipAddresses;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.item_list, parent, false);

        ImageView imageView = rowView.findViewById(R.id.listImage);
        TextView textView = rowView.findViewById(R.id.listName);

        // Nastavení hodnot pro jednotlivé položky seznamu
        // (Zde je pouze příklad, měli byste upravit podle vašich potřeb)
        imageView.setImageResource(R.drawable.weather);
        textView.setText(ipAddresses.get(position));

        return rowView;
    }

    // Metoda pro aktualizaci dat v adaptéru
    public void updateData(ArrayList<String> newData) {
        clear();
        addAll(newData);
        notifyDataSetChanged();
    }
}
