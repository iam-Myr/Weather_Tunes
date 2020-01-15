package com.example.weathertunes;

import androidx.appcompat.app.AppCompatActivity;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.location.FusedLocationProviderClient;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setRequestedOrientation(SCREEN_ORIENTATION_PORTRAIT);


        //Maybe ger location without google
        Button songBtn = findViewById(R.id.songBtn);
        songBtn.setVisibility(View.GONE);

        FetchWeatherTask fetchWeather = new FetchWeatherTask(10.94688866, 21.76888663);
        String weatherString = null;

        try {
            weatherString = fetchWeather.execute().get();

        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        final String weather = weatherString;
        final MediaPlayer mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        songBtn.setVisibility(View.VISIBLE);

        songBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.reset();

                final String tag = generateTag(weather);

                Track track = null;
                FetchTrackTask fetchTrack = new FetchTrackTask(tag);
                try {
                    track = fetchTrack.execute().get();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                try {
                    mediaPlayer.setDataSource(track.getAudio_url());
                    mediaPlayer.prepare();// might take long! (for buffering, etc)
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mediaPlayer.start();
            }
        });
    }

    public String generateTag(String weather){

        //will be more complicated with randomizer tomorrow!!!!

        if (weather.equals("Clear")) return "happy";
        else if (weather.equals("Clouds")) return "sad";
        else if (weather.equals("Rain")) return "lofi";
        else if (weather.equals("Snow")) return"cold";
        else return "horror";

    }
}
