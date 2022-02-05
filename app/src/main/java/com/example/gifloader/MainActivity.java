package com.example.gifloader;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private final List<String> urls = new ArrayList<>();
    private final List<String> descriptions = new ArrayList<>();
    private int currentPosition = -1;
    private String jsonString = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button button = findViewById(R.id.nextButton);
        try {
            next(button);
        } catch (IOException | InterruptedException | JSONException e) {
            e.printStackTrace();
        }
    }

    private void setImage(String gifAddress, String description) {
        ImageView gif = findViewById(R.id.gif);
        TextView text = findViewById(R.id.text);
        text.setText(description);
        Glide.with(this)
                .asGif()
                .load(gifAddress)
                .placeholder(new ColorDrawable(Color.WHITE))
                .error(new ColorDrawable(Color.WHITE))
                .into(gif);
    }

    public void next(View view) throws IOException, InterruptedException, JSONException {
        String gifAddress;
        String description;
        ++currentPosition;
        if (currentPosition == urls.size()) {
            getJSON();
            gifAddress = getGifAddress();
            description = getDescription();
            urls.add(gifAddress);
            descriptions.add(description);
        } else {
            gifAddress = urls.get(currentPosition);
            description = descriptions.get(currentPosition);
        }
        setImage(gifAddress, description);
    }

    public void back(View view) {
        if (currentPosition >= 1) {
            --currentPosition;
            String gifAddress = urls.get(currentPosition);
            String description = descriptions.get(currentPosition);
            setImage(gifAddress, description);
        }
    }

    private void LoadJSON() throws IOException {
        URL url = new URL("https://developerslife.ru/random?json=true");
        InputStream inputStream = url.openStream();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            stringBuilder.append(line).append("\n");
        }
        bufferedReader.close();
        jsonString = stringBuilder.toString();
    }

    private String getGifAddress() throws org.json.JSONException {
        JSONObject json = new JSONObject(jsonString);
        return json.get("gifURL").toString();
    }

    private String getDescription() {
        String description = "";
        try {
            JSONObject json = new JSONObject(jsonString);
            description = json.get("description").toString();
        } catch (Exception ignored) {
        }
        return description;
    }

    private void getJSON() throws InterruptedException {
        Runnable task = () -> {
            try {
                LoadJSON();
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
        Thread thread = new Thread(task);
        thread.start();
        thread.join();
    }
}