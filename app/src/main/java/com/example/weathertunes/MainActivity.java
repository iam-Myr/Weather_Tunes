package com.example.weathertunes;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.Random;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;

public class MainActivity extends AppCompatActivity implements FetchWeatherTask.OnTaskCompleted, FetchTrackTask.OnTaskCompleted {

    private static final int REQUEST_CODE_ACCESS_FINE_LOCATION = 1;
    private static boolean ACCESS_FINE_LOCATION_GRANTED = false;
    private static boolean WEATHER_FETCHED = false;
    private static String[] weather;
    private static Track track;
    public static SQLiteDatabase db;
    private static MediaPlayer mediaPlayer;

    public static String[] ClearTags = {"happy", "brazil", "cute", "electric", "energy"};
    public static String[] RainTags = {"sad", "cafe", "jazz", "funk", "ballad", "tango", "lofi"};
    public static String[] CloudsTags = {"rap", "sad", "storm"};
    public static String[] MistTags = {"horror", "scary", "dark", "metal"};
    public static String[] SnowTags = {"christmas", "bells", "winter"};
    public static String[] FogTags = {"scary"};

    private TextView weatherTxt;
    private Button pauseBtn;
    private Button songBtn;
    private Button favouritesBtn;
    private Button addToFavBtn;
    private TextView playingTxt;
    private ImageView songImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Get activity items
        setRequestedOrientation(SCREEN_ORIENTATION_PORTRAIT);
        songBtn = findViewById(R.id.skipBtn);
        favouritesBtn = findViewById(R.id.favouritesBtn);
        addToFavBtn = findViewById(R.id.addToFavBtn);
        this.weatherTxt = findViewById(R.id.weatherTxt);
        playingTxt =  findViewById(R.id.playingTxt);
        songImg = findViewById(R.id.songImg);
        pauseBtn = findViewById(R.id.pauseBtn);

        //Get location
        Location location =  getLocation();
        double longitude;
        double latitude;
        if(location == null){
            latitude = 34.3666; //33.74900;
            longitude = 25.9507;//84.38798;
            Toast.makeText(getApplicationContext(), "Couldn't find location! You are now in Atlanta", Toast.LENGTH_SHORT).show();
        }
        else {
            longitude = location.getLongitude();
            latitude = location.getLatitude();
        }

        //Get weather
        FetchWeatherTask fetchWeather = new FetchWeatherTask(longitude, latitude, this );
        fetchWeather.execute();

        //Create Media player
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if(WEATHER_FETCHED) {
                    mediaPlayer.reset();
                    fetchTrack(weather[1]);
                }
            }
        });

        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer player) {
                playingTxt.setText("Now Playing: " + track.getName());
                Picasso.with(getApplicationContext()).load(track.getAlbum_image()).into(songImg);
                pauseBtn.setText("PAUSE");
                player.start();
            }


        });

        mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
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

        //Make buttons visible


        songBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Picasso.with(getApplicationContext()).load(loadingUrl).into(songImg);
                if(WEATHER_FETCHED) {
                    mediaPlayer.reset();
                    fetchTrack(weather[1]);
                }
            }
        });

        addToFavBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (track != null){
                    int id = track.getId();
                    String name = track.getName();
                    int duration = track.getDuration();
                    int artist_id = track.getArtist_id();
                    String artist_name = track.getArtist_name();
                    String album_name = track.getAlbum_name();
                    int album_id = track.getAlbum_id();
                    String album_image = track.getAlbum_image();
                    String audio_url = track.getAudio_url();

                    db.execSQL("INSERT OR IGNORE INTO Favourites(id,name,duration,artist_id,artist_name,album_name,album_id,album_image,audio_url)" +
                            "VALUES ('"+id+"','"+name+"','"+duration+"', '"+artist_id+"','"+artist_name+"','"+album_name+"','"+album_id+"','"+album_image+"','"+audio_url+"')");
                    Toast.makeText(getApplicationContext(), "Added to favourites!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        favouritesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,FavouritesActivity.class);
                intent.putExtra("weather", weather[1]);

                startActivity(intent);
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
    }

    @Override
    public void onWeatherFetchCompleted(String[] result) {
        if(result != null) {
            weather = result;
            weatherTxt.setText(weather[0] + " - " + weather[1]);
            WEATHER_FETCHED = true;
        }
        else {
            Toast.makeText(getApplicationContext(), "Couldn't fetch weather", Toast.LENGTH_SHORT).show();
        }

        if(WEATHER_FETCHED) {
            fetchTrack(weather[1]);
        }
    }

    @Override
    public void onTrackFetchCompleted(Track result) {
        track = result;

        try {
            if (track != null) {
                mediaPlayer.setDataSource(track.getAudio_url());
                mediaPlayer.prepareAsync();
            }
            else Toast.makeText(getApplicationContext(), "Couldn't fetch song", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Location getLocation() {
        int hasReadLocationPermission = ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION);
        Log.d("MYR", "HAS Read Location Permissions " + hasReadLocationPermission);

        if (hasReadLocationPermission == PackageManager.PERMISSION_GRANTED) {
            Log.d("MYR", "Permission Granted");
            ACCESS_FINE_LOCATION_GRANTED = true;
        } else {
            Log.d("MYR", "Requesting permission");
            ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION}, REQUEST_CODE_ACCESS_FINE_LOCATION);
        }

        if (ACCESS_FINE_LOCATION_GRANTED) {
            LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            if(lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                return lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }
            else {
                Toast.makeText(getApplicationContext(), "GPS must be enabled!", Toast.LENGTH_SHORT).show();
            }
        }
        return null;
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
                    // contacts-related task you need to do.
                    Log.d("MYR", "Permission Granted!");
                    ACCESS_FINE_LOCATION_GRANTED = true;
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Log.d("MYR", "Permission refused");
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }

    public void fetchTrack(String weather){
        String tag = generateTag(weather);

        FetchTrackTask fetchTrack = new FetchTrackTask(tag, this);
        fetchTrack.execute();
    }

    public String generateTag(String weather){
        Random rand = new Random();

        switch (weather) {
            case "Clear":
                return ClearTags[rand.nextInt(ClearTags.length)];
            case "Clouds":
                return CloudsTags[rand.nextInt(CloudsTags.length)];
            case "Rain":
                return RainTags[rand.nextInt(RainTags.length)];
            case "Snow":
                return SnowTags[rand.nextInt(SnowTags.length)];
            case "Mist":
                return MistTags[rand.nextInt(MistTags.length)];
            case "Fog":
                return FogTags[rand.nextInt(FogTags.length)];
            default:
                return "random";

        }
    }

}