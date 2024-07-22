package com.example.guessr;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;


public class StartScreen extends AppCompatActivity {

    Spinner spinner;
    ArrayList<String> roomNames = new ArrayList<>();
    GetRequest request;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        setContentView(R.layout.activity_start_screen);
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        spinner = findViewById(R.id.spinner);

        try {
            request = new GetRequest.Builder().baseUrl("http://10.0.0.207:5005/").build();
            JSONArray newObj = request.jsonArrayRequest("zones");
            for (int i = 0; i < newObj.length(); i++) {
                JSONObject x = newObj.getJSONObject(i);
                roomNames.add(x.getJSONObject("coordinator").getString("roomName"));
            }
            ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>
                    (this, android.R.layout.simple_spinner_item,
                            roomNames);
            spinnerArrayAdapter.setDropDownViewResource(android.R.layout
                    .simple_spinner_dropdown_item);
            spinner.setAdapter(spinnerArrayAdapter);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Button startBtn = findViewById(R.id.startButton);
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    String selectedRoom = roomNames.get((int) spinner.getSelectedItemId());
                    request = new GetRequest.Builder()
                            .baseUrl("http://10.0.0.207:5005/")
                            .roomName(selectedRoom)
                            .build();
                    request.simpleRequest("spotify/now/spotify:user:spotify:playlist:6fFRso1l7ATjP3jOvNW8Mf");
                    //request.simpleRequest("musicsearch/spotify/playlist/hot+hits+usa");
                    Intent intent = new Intent(StartScreen.this, MainActivity.class);
                    intent.putExtra("roomName", selectedRoom);
                    startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
