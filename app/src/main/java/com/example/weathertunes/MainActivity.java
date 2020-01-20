package com.example.weathertunes;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;


public class MainActivity extends AppCompatActivity implements FetchWeatherTask.OnTaskCompleted, FetchTrackTask.OnTaskCompleted {

    private static final int REQUEST_CODE_ACCESS_FINE_LOCATION = 1;
    private static boolean ACCESS_FINE_LOCATION_GRANTED = false;
    private FusedLocationProviderClient fusedLocationClient;
    private static boolean WEATHER_FETCHED = false;
    private static boolean MEDIAPLAYER_STARTED = false;
    private static boolean SONG_REQUESTED = false;
    private static String[] weather;
    private static Track currentTrack;
    private Intent intent;
    private static SQLiteDatabase db;
    public static MediaPlayer mediaPlayer;

    public static List<String> ClearTags = new ArrayList<>(Arrays.asList("happy", "brazil", "cute", "swing", "upbeat", "guitar", "trumpet"));
    public static List<String> RainTags = new ArrayList<>(Arrays.asList("sad", "cafe", "jazz", "funk", "ballad", "tango", "lofi", "violin", "piano", "romantic"));
    public static List<String> CloudsTags = new ArrayList<>(Arrays.asList("sad", "storm", "melancholy", "grey", "lofi", "space", "indie", "ambient", "newage"));
    public static List<String> MistTags = new ArrayList<>(Arrays.asList("horror", "scary", "dark", "metal", "classic"));
    public static List<String> SnowTags = new ArrayList<>(Arrays.asList("christmas", "bells", "winter"));
    public static List<String> FogTags = new ArrayList<>(Arrays.asList("scary"));

    private TextView weatherTxt;
    private Button pauseBtn;
    private Button nextBtn;
    private Button addToFavBtn;
    private TextView playingTxt;
    private ImageView songImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d("Main Activity", "started successfully");

        //Get activity items
        setRequestedOrientation(SCREEN_ORIENTATION_PORTRAIT);
        nextBtn = findViewById(R.id.nextBtn);
        nextBtn.setEnabled(false);
        addToFavBtn = findViewById(R.id.addToFavBtn);
        addToFavBtn.setEnabled(false);
        pauseBtn = findViewById(R.id.pauseBtn);
        pauseBtn.setEnabled(false);
        weatherTxt = findViewById(R.id.weatherTxt);
        playingTxt =  findViewById(R.id.playingTxt);
        songImg = findViewById(R.id.songImg);

        intent = new Intent(MainActivity.this,FavouritesActivity.class);

        //Change image to loading
        songImg.setImageResource(getResources().getIdentifier("drawable/loading", null, this.getPackageName()));

        int hasReadLocationPermission = ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION);
        Log.d("MYR", "HAS Read Location Permissions "+hasReadLocationPermission);

        if(hasReadLocationPermission == PackageManager.PERMISSION_GRANTED){
            Log.d("MYR", "Permission Granted");
            ACCESS_FINE_LOCATION_GRANTED = true;
        }
        else{
            Log.d("MYR", "Requesting permission");
            ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION}, REQUEST_CODE_ACCESS_FINE_LOCATION);
        }

        if(ACCESS_FINE_LOCATION_GRANTED) getLocation();
        else {
            Toast.makeText(getApplicationContext(),
                    "You must provide permission to access location!", Toast.LENGTH_LONG).show();
        }

        //Create Media player
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if(WEATHER_FETCHED) {
                    fetchTrack(weather[1]);
                }
            }
        });

        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer player) {
                playingTxt.setText(currentTrack.getName());
                Picasso.with(getApplicationContext()).load(currentTrack.getImage()).into(songImg);
                pauseBtn.setText("PAUSE");
                player.start();
                MEDIAPLAYER_STARTED = true;
                addToFavBtn.setEnabled(true);
                pauseBtn.setEnabled(true);
                nextBtn.setEnabled(true);
            }


        });

        mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                Toast.makeText(getApplicationContext(), "An error occured", Toast.LENGTH_SHORT).show();
                mediaPlayer.reset();
                return false;
            }
        });

        //Create or open DB
        db = getBaseContext().openOrCreateDatabase(
                "favourites-db.db",
                Context.MODE_PRIVATE,
                null);

        db.execSQL("CREATE TABLE IF NOT EXISTS \"Favourites\" (\n" +
                "    \"id\"    INTEGER NOT NULL,\n" +
                "    \"name\"    TEXT,\n" +
                "    \"duration\"    INTEGER,\n" +
                "    \"artist_id\"    NUMERIC,\n" +
                "    \"artist_name\"    BLOB,\n" +
                "    \"album_name\"    INTEGER,\n" +
                "    \"album_id\"    INTEGER,\n" +
                "    \"album_image\"    TEXT,\n" +
                "    \"audio_url\"    TEXT NOT NULL,\n" +
                "    PRIMARY KEY(\"id\")\n" +
                ");");


        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                songImg.setImageResource(getResources().getIdentifier("drawable/loading", null, "com.example.weathertunes"));
                if(WEATHER_FETCHED) {
                    mediaPlayer.reset();
                    fetchTrack(weather[1]);
                }
            }
        });

        addToFavBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentTrack != null){
                    int id = currentTrack.getId();
                    String name = currentTrack.getName();
                    int duration = currentTrack.getDuration();
                    int artist_id = currentTrack.getArtist_id();
                    String artist_name = currentTrack.getArtist_name();
                    String album_name = currentTrack.getAlbum_name();
                    int album_id = currentTrack.getAlbum_id();
                    String album_image = currentTrack.getImage();
                    String audio_url = currentTrack.getAudio_url();

                    db.execSQL("INSERT OR IGNORE INTO Favourites(id,name,duration,artist_id,artist_name,album_name,album_id,album_image,audio_url)" +
                            "VALUES ('"+id+"','"+name+"','"+duration+"', '"+artist_id+"','"+artist_name+"','"+album_name+"','"+album_id+"','"+album_image+"','"+audio_url+"')");
                    Toast.makeText(getApplicationContext(), "Added to favourites!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        pauseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mediaPlayer.isPlaying()){
                    mediaPlayer.pause();
                    pauseBtn.setText("PLAY");
                } else {
                    pauseBtn.setText("PAUSE");
                    mediaPlayer.start();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mediaPlayer.release();
        mediaPlayer = null;
        finishAndRemoveTask();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1){
            if (resultCode == RESULT_OK && data != null){
                if (MEDIAPLAYER_STARTED){
                    this.currentTrack = data.getParcelableExtra("track");
                    playTrack(currentTrack.getAudio_url());
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mymenu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    // handle button activities
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        intent.putExtra("SONG_REQUESTED", SONG_REQUESTED);
        startActivityForResult(intent, 1);
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onWeatherFetchCompleted(String[] result) {
        if(result != null) {
            weather = result;
            if (weather[0].equals("")) weather[0] = "At sea";
            weatherTxt.setText(weather[0] + " - " + weather[1]);
            WEATHER_FETCHED = true;
        }
        else {
            Toast.makeText(getApplicationContext(), "Couldn't find weather", Toast.LENGTH_SHORT).show();
        }

        if(WEATHER_FETCHED) {
            fetchTrack(weather[1]);
        }
    }

    @Override
    public void onTrackFetchCompleted(Track result) {
        currentTrack = result;
            if (currentTrack != null) {
                playTrack(currentTrack.getAudio_url());
            }
            else {
                Toast.makeText(getApplicationContext(), "Couldn't fetch song", Toast.LENGTH_SHORT).show();
                pauseBtn.setEnabled(true);
                nextBtn.setEnabled(true);
                addToFavBtn.setEnabled(true);
                playingTxt.setText("");
            }
    }

    public void playTrack(String url) {
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(url);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mediaPlayer.prepareAsync();

    }

    public void getLocation() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        double latitude;
                        double longitude;
                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude =location.getLongitude();
                        }
                        else {
                            latitude = new Random().nextInt(90 + 90) - 90;
                            longitude = new Random().nextInt(180 + 180) - 180;
                            Log.d("MYR","Coords: " + latitude +" and "+ longitude);
                            Toast.makeText(getApplicationContext(),
                                    "Couldn't find location! You are now in a random place in the world....", Toast.LENGTH_LONG).show();
                        }
                        FetchWeatherTask fetchWeather = new FetchWeatherTask(latitude, longitude, MainActivity.this );
                        fetchWeather.execute();
                    }
                });
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    Log.d("MYR", "Permission Granted!");
                    ACCESS_FINE_LOCATION_GRANTED = true;
                    getLocation();
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Log.d("MYR", "Permission refused");
                }
                return;
            }
        }
    }

    public void fetchTrack(String weather){

        pauseBtn.setEnabled(false);
        nextBtn.setEnabled(false);
        addToFavBtn.setEnabled(false);

        String tag = generateTag(weather);

        FetchTrackTask fetchTrack = new FetchTrackTask(tag, this);
        fetchTrack.execute();
    }

    public String generateTag(String weather){ //THE SAUCE
        List<String> tagList;

        switch (weather) {
            case "Clear":
                tagList = ClearTags;
                break;
            case "Clouds":
                tagList = CloudsTags;
                break;
            case "Rain":
                tagList = RainTags;
                break;
            case "Snow":
                tagList = SnowTags;
                break;
            case "Mist":
                tagList = MistTags;
                break;
            case "Fog":
                tagList = FogTags;
                break;
            default:
                return "song";

        }
        Collections.shuffle(tagList);
        Log.d("MYR","TAG: " +tagList.get(0)+"+"+tagList.get(1)+"+"+ tagList.get(2));
        return tagList.get(0)+"+"+tagList.get(1)+"+"+ tagList.get(2);
    }
}