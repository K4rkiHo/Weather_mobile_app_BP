package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

public class About_app extends AppCompatActivity {
    private TextView version;
    private TextView author;
    private TextView supervisor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_app);

        version = findViewById(R.id.textViewVersion);
        author = findViewById(R.id.textViewAuthor);
        supervisor = findViewById(R.id.textViewSupervisor);

        version.setText("Version: " + 1.0);

        author.setText("Author: " + "Jan Karkoška");
        supervisor.setText("Supervisor: " + "Ing. Pavel Moravec, Ph.D");


    }
}