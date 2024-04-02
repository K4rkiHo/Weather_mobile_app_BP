package com.example.myapplication;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONObject;

import java.util.List;

public class JsonObjectAdapter extends RecyclerView.Adapter<JsonObjectAdapter.JsonObjectViewHolder> {

    private List<JsonObjectModel> dataList;

    public JsonObjectAdapter(List<JsonObjectModel> dataList) {
        this.dataList = dataList;
    }

    @NonNull
    @Override
    public JsonObjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_json_object, parent, false);
        return new JsonObjectViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull JsonObjectViewHolder holder, int position) {
        JsonObjectModel model = dataList.get(position);
        holder.keyTextView.setText(model.getKey());
        holder.valueTextView.setText(model.getValue());
        holder.minTextView.setText(model.getMin());
        holder.maxTextView.setText(model.getMax());
        holder.avgTextView.setText(model.getAvg());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start the new activity
                Intent intent = new Intent(v.getContext(), Weather_layout.class);
                intent.putExtra("weather", model.getKey());
                intent.putExtra("jsonObject", model.getDefaultValue());
                intent.putExtra("unit", model.getValue());
                intent.putExtra("convert_unit", model.getUnit());
                intent.putExtra("original_unit", model.getOriginalUnit());

                v.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public static class JsonObjectViewHolder extends RecyclerView.ViewHolder {
        TextView keyTextView;
        TextView valueTextView;

        TextView minTextView;
        TextView maxTextView;
        TextView avgTextView;

        public JsonObjectViewHolder(@NonNull View itemView) {
            super(itemView);
            keyTextView = itemView.findViewById(R.id.keyTextView);
            valueTextView = itemView.findViewById(R.id.valueTextView);
            minTextView = itemView.findViewById(R.id.minTextView);
            maxTextView = itemView.findViewById(R.id.maxTextView);
            avgTextView = itemView.findViewById(R.id.avgTextView);
        }
    }

}

