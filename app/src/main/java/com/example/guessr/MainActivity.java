package com.example.guessr;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    EditText text;
    ImageView img;
    TextView score;
    String roomName;
    JSONObject currentObj;

    GetRequest request;
    Runnable runnable;
    long startTime = System.currentTimeMillis();
    private Handler handler = new Handler();

    @Override
    protected void onResume() {
        //start handler as activity become visible

        handler.postDelayed(runnable = new Runnable() {
            public void run() {
                try {
                    setImage();
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
                handler.postDelayed(runnable, 1000);
            }
        }, 1000);

        super.onResume();
        text.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        assert imm != null;
        imm.showSoftInput(text, InputMethodManager.SHOW_IMPLICIT);
    }

    private void setImage() throws IOException, JSONException {
        request = new GetRequest.Builder()
                .baseUrl("http://10.0.0.207:5005/")
                .roomName(roomName)
                .build();
        currentObj = request.jsonObjectRequest("state").getJSONObject("currentTrack");
        img.setImageBitmap(BitmapFactory.decodeStream(new URL(currentObj.getString("absoluteAlbumArtUri")).openConnection().getInputStream()));

    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        setContentView(R.layout.activity_main);
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);

        text = findViewById(R.id.editText);
        img = findViewById(R.id.imageView);
        score = findViewById(R.id.scoreView);

        text.requestFocus();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        assert imm != null;
        imm.showSoftInput(text, InputMethodManager.SHOW_IMPLICIT);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            roomName = extras.getString("roomName");
            request = new GetRequest.Builder()
                    .baseUrl("http://10.0.0.207:5005/")
                    .roomName(roomName)
                    .build();
        }
        try {
            currentObj = request.jsonObjectRequest("state").getJSONObject("currentTrack");
            img.setImageBitmap(BitmapFactory.decodeStream(new URL(currentObj.getString("albumArtUri")).openConnection().getInputStream()));
        } catch (Exception e) {
            e.printStackTrace();
        }

        final Button button = findViewById(R.id.submitButton);
        button.setVisibility(View.GONE);

        text.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (text.getText().length() > 0)
                    button.setVisibility(View.VISIBLE);
                else
                    button.setVisibility(View.INVISIBLE);
            }
        });

        text.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int key, KeyEvent keyEvent) {
                if ((keyEvent.getAction() == KeyEvent.ACTION_DOWN) && (key == KeyEvent.KEYCODE_ENTER)) {
                    submission();
                    return true;
                }
                return false;
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("DefaultLocale")
            public void onClick(View v) {
                submission();
            }
        });
    }

    @SuppressLint("DefaultLocale")
    public void submission() {
        try {
            String submittedTitle = text.getText().toString().toLowerCase();
            JSONObject currentObj = request.jsonObjectRequest("state").getJSONObject("currentTrack");
            String songTitle = currentObj.getString("title").toLowerCase();
            text.setText("");

            int x = StringUtils.getLevenshteinDistance(submittedTitle, songTitle);
            //if (x < 3 || songTitle.contains(submittedTitle)) {
            if (x <= 3) {
                updateScore();
                request.simpleRequest("next");

            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Game Over")
                        .setMessage(String.format("You took %d seconds\nThe song name was %s\n",
                                (int) ((System.currentTimeMillis() - startTime) / 1000),
                                currentObj.getString("title")))
                        .setCancelable(false)
                        .setPositiveButton("End Game", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                endGame();
                            }
                        })
                        .setNeutralButton("Copy to share your score", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                String shareText = String.format("Can you beat my high score of %d on Sonos Guessr?",
                                        Integer.parseInt(score.getText().toString().split(" ")[1]));
                                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                                ClipData clip = ClipData.newPlainText("copied text", shareText);
                                assert clipboard != null;
                                clipboard.setPrimaryClip(clip);
                                endGame();
                            }
                        });
                builder.show();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateScore() {
        String scoreText = score.getText().toString();
        String[] scoreArr = scoreText.split(" ");
        scoreArr[1] = String.valueOf(Integer.parseInt(scoreArr[1]) + 1);
        score.setText(String.join(" ", scoreArr));
    }

    private void endGame() {
        try {
            request.simpleRequest("pause");
        } catch (IOException e) {
            e.printStackTrace();
        }
        Intent intent = new Intent(MainActivity.this, StartScreen.class);
        startActivity(intent);
    }
}
