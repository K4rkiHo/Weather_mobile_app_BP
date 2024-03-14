package com.example.myapplication;

import android.content.Context;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import java.util.List;

public class UnitsAdapter extends ArrayAdapter<UnitItem> {
    private int resourceLayout;
    private Context mContext;
    private SparseBooleanArray switchStates = new SparseBooleanArray();

    public UnitsAdapter(Context context, int resource, List<UnitItem> items) {
        super(context, resource, items);
        this.resourceLayout = resource;
        this.mContext = context;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View itemView = convertView;
        if (itemView == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            itemView = inflater.inflate(resourceLayout, parent, false);
        }

        final UnitItem item = getItem(position);

        //TextView unitsNameTextView = itemView.findViewById(R.id.units_name);
        final TextView unitsTextView = itemView.findViewById(R.id.units);
        final Switch switchButton = itemView.findViewById(R.id.unitSwitch);

        //unitsNameTextView.setText(item.getUnitsName());
        unitsTextView.setText(item.getUnits());
        switchButton.setChecked(item.getSwitchState());

        // Nastavení posluchače pro změnu stavu přepínače
        switchButton.setOnCheckedChangeListener(null); // Nejprve odebereme existující posluchač
        switchButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                item.setSwitchState(isChecked); // Uložení stavu přepínače do UnitItem
                String unit = isChecked ? convertUnit(item.getUnits()) : item.getUnits(); // Získání konvertované jednotky
                unitsTextView.setText(unit); // Nastavení textu s novou jednotkou
            }
        });

        return itemView;
    }

    // Metoda pro konverzi jednotek
    private String convertUnit(String unit) {
        switch (unit) {
            case "°F":
                return "°C";
            case "in/hr":
                return "mm/hr";
            case "in":
                return "mm";
            case "mph":
                return "km/h";
            case "inHg":
                return "hPa";
            case "W/m^2":
                return "Lux";
            default:
                return unit;
        }
    }

}
