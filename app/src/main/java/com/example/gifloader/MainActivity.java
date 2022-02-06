package com.example.gifloader;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

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
    private int mCurrentPosition = 0;
    private String mJsonString = "";
    private Button mBackButton;
    private Button mNextButton;
    private ImageView mGif;
    private TextView mText;
    private LinearLayout mErrorPage;
    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mNextButton = findViewById(R.id.next_button);
        mBackButton = findViewById(R.id.back_button);
        mGif = findViewById(R.id.gif);
        mText = findViewById(R.id.text);
        mErrorPage = findViewById(R.id.error_page);
        mProgressBar = findViewById(R.id.progress_bar);

        setButtons(false, false);
        try {
            loadGif();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void next(View view) throws InterruptedException {
        ++mCurrentPosition;
        setButtons(false, false);
        if (mCurrentPosition == urls.size()) {
            loadGif();
        } else {
            String gifAddress = urls.get(mCurrentPosition);
            String description = descriptions.get(mCurrentPosition);
            setImage(gifAddress, description);
        }
    }

    public void back(View view) {
        setButtons(false, false);
        if (mCurrentPosition >= 1) {
            --mCurrentPosition;
            setButtons(true, false);
            String gifAddress = urls.get(mCurrentPosition);
            String description = descriptions.get(mCurrentPosition);
            setImage(gifAddress, description);
        }
    }

    public void loadGif() throws InterruptedException {
        mJsonString = "";
        getJSON();
        if (mJsonString.equals("")) {
            setErrorPage();
            return;
        }
        try {
            String gifAddress = getGifAddress();
            String description = getDescription();
            urls.add(gifAddress);
            descriptions.add(description);
            setImage(gifAddress, description);
        } catch (Exception e) {
            setErrorPage();
        }
    }

    public void loadGif(View view) throws InterruptedException {
        loadGif();
    }

    private void setButtons(boolean isBackButton, boolean isNextButton) {
        mBackButton.setEnabled(isBackButton);
        mNextButton.setEnabled(isNextButton);
    }

    private void setImage(String gifAddress, String description) {
        mErrorPage.setVisibility(View.INVISIBLE);
        mGif.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(ProgressBar.VISIBLE);
        mText.setText(description);
        setButtons(false, false);

        Glide.with(this)
                .asGif()
                .load(gifAddress)
                .listener(new RequestListener<GifDrawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<GifDrawable> target, boolean isFirstResource) {
                        setErrorPage();
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GifDrawable resource, Object model, Target<GifDrawable> target, DataSource dataSource, boolean isFirstResource) {
                        mProgressBar.setVisibility(ProgressBar.INVISIBLE);
                        setButtons(mCurrentPosition > 0, true);
                        return false;
                    }
                })
                .placeholder(new ColorDrawable(Color.WHITE))
                .into(mGif);
    }

    private void setErrorPage() {
        setButtons(mCurrentPosition > 0, mCurrentPosition + 1 < urls.size());
        mGif.setVisibility(View.INVISIBLE);
        mProgressBar.setVisibility(View.INVISIBLE);
        mErrorPage.setVisibility(View.VISIBLE);
        mText.setText("");
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
        mJsonString = stringBuilder.toString();
    }

    private String getGifAddress() throws org.json.JSONException {
        JSONObject json = new JSONObject(mJsonString);
        return json.get("gifURL").toString();
    }

    private String getDescription() {
        String description = "";
        try {
            JSONObject json = new JSONObject(mJsonString);
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